package microservice.starter.utils.http

import java.io.{IOException, InputStream}
import java.net.ConnectException
import java.nio.charset.{Charset, StandardCharsets}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{TimeUnit, TimeoutException}

import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import org.asynchttpclient._
import org.asynchttpclient.cookie.Cookie
import org.asynchttpclient.proxy.ProxyServer
import org.asynchttpclient.request.body.multipart.Part
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{Formats, JValue}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * HttpClient
  * Created by yangjing on 15-11-3.
  */
class HttpClient private(val client: AsyncHttpClient,
                         private var _proxyServers: Vector[ProxyServer]) extends AutoCloseable with StrictLogging {
  hcSelf =>

  private var curIdx = 0


  def close() = client.close()

  def proxyServers = _proxyServers

  def setProxyServers(proxies: Vector[ProxyServer]): Unit = synchronized {
    _proxyServers = proxies
  }

  def clearProxyServers(): Unit = synchronized {
    logger.info("clear proxy servers: " + proxyServers)
    _proxyServers = Vector()
  }

  def addProxyServer(proxy: ProxyServer): Unit = synchronized {
    logger.info("add proxy server: " + proxy)
    _proxyServers = _proxyServers.filterNot(v => HttpClient.equalsProxyServer(v, proxy)) :+ proxy
  }

  def removeProxyServer(proxy: ProxyServer): Unit = synchronized {
    logger.warn("remove proxy server: " + proxy)
    _proxyServers = _proxyServers.filterNot(v => HttpClient.equalsProxyServer(v, proxy))
  }

  def get(url: String) = {
    new HttpClientBuilder(client.prepareGet(url))
  }

  def post(url: String) = {
    new HttpClientBuilder(client.preparePost(url))
  }

  def delete(url: String) = {
    new HttpClientBuilder(client.prepareDelete(url))
  }

  def put(url: String) = {
    new HttpClientBuilder(client.preparePut(url))
  }

  private def setProxy(builder: HttpClientBuilder): HttpClientBuilder = synchronized {
    if (_proxyServers.isEmpty) {
      builder
    } else {
      if (curIdx < _proxyServers.size) {
        val ps = _proxyServers(curIdx)
        curIdx += 1
        builder.setProxy(ps)
      } else {
        val ps = _proxyServers(0)
        curIdx = 1
        builder.setProxy(ps)
      }
    }
  }

  class HttpClientBuilder(builder: BoundRequestBuilder) extends LazyLogging {
    var _proxy: ProxyServer = _

    def queryParam(params: (String, String)*) = {
      params.foreach { case (name, value) => builder.addQueryParam(name, value) }
      this
    }

    def header(headers: (String, String)*) = {
      headers.foreach { case (name, value) => builder.addHeader(name, value) }
      this
    }

    def cookie(cookie: Cookie) = {
      builder.addCookie(cookie)
      this
    }

    def part(part: Part) = {
      builder.addBodyPart(part)
      this
    }

    def addFormParam(params: (String, String)*) = {
      params.foreach { case (key, value) => builder.addFormParam(key, value) }
      this
    }

    def setBody(json: JValue)(implicit jsonFormat: Formats): HttpClientBuilder = {
      header("Content-Type" -> "application/json;charset=utf-8")
      setBody(Serialization.write(json))
    }

    def setBody(s: String): HttpClientBuilder = {
      builder.setBody(s)
      this
    }

    def setBody(bytes: Array[Byte]): HttpClientBuilder = {
      builder.setBody(bytes)
      this
    }

    def setBody(in: InputStream): HttpClientBuilder = {
      builder.setBody(in)
      this
    }

    def setRequestTimeout(millis: Int) = {
      builder.setRequestTimeout(millis)
      this
    }

    def setProxy(proxyServer: ProxyServer) = {
      logger.debug(s"setProxy($proxyServer)")
      _proxy = proxyServer
      builder.setProxyServer(_proxy)
      this
    }

    def setFollowRedirects(followRedirect: Boolean) = {
      builder.setFollowRedirect(followRedirect)
      this
    }

    def executeJson(statusCodes: Int*)(implicit ec: ExecutionContext, jsonFormat: Formats, charset: Charset = StandardCharsets.UTF_8): Future[JValue] =
      execute().flatMap { resp =>
        if (statusCodes.contains(resp.getStatusCode) || statusCodes.isEmpty) {
          Future.successful(JsonMethods.parse(resp.getResponseBody(charset)))
        } else
          Future.failed(new RuntimeException(s"needs status code: ${statusCodes.mkString(",")}, but receive ${resp.getStatusCode}"))
      }

    def execute(): Future[HttpClientResponse] = {
      import ExecutionContext.Implicits.global
      val promise = Promise[HttpClientResponse]()
      hcSelf.setProxy(this)
      val reqRespNumber = HttpClient.httpReqRespNumber.incrementAndGet()
      val request = builder.build()
      logger.trace(
        s"""> HttpClient Request $reqRespNumber
            |> ${request.getMethod} ${request.getUrl}""".stripMargin)

      val beginNano = System.nanoTime()
      @volatile var endNano = 0L

      try {
        builder.execute(new AsyncCompletionHandler[Unit] {
          override def onCompleted(response: Response): Unit = {
            endNano = System.nanoTime()
            promise.success(HttpClientResponse(response, TimeUnit.NANOSECONDS.toMillis(endNano - beginNano)))
          }

          override def onThrowable(t: Throwable): Unit = {
            endNano = System.nanoTime()
            promise.failure(t)
            t match {
              case e: IOException if _proxy != null && e.getLocalizedMessage.contains("Remotely closed") =>
                hcSelf.removeProxyServer(_proxy)
                logger.warn(s"代理服务 ${_proxy} 不可用，已删除。$e")
              case e@(_: ConnectException | _: TimeoutException) if _proxy != null /*if _proxy != null && e.getLocalizedMessage.contains("connection timed out:")*/ =>
                hcSelf.removeProxyServer(_proxy)
                logger.warn(s"代理服务 ${_proxy} 不可用，已删除。$e")
            }
          }
        })
      } catch {
        case e: Throwable =>
          endNano = System.nanoTime()
          promise.failure(e)
      }
      val future = promise.future

      future.onComplete { tryValue =>
        logger.trace(endExecuteFormat(TimeUnit.NANOSECONDS.toMillis(endNano - beginNano), reqRespNumber, tryValue))
      }

      future
    }

    private def endExecuteFormat(costMillis: Long, reqRespNumber: Int, tryValue: Try[Response]) = {
      val b = new StringBuilder(s"< HttpClient Response $reqRespNumber\n")
      //      tryValue.foreach { resp =>
      //        b.append(s"< ${resp.getStatusCode} ${resp.getStatusText}\n")
      //        val iter = resp.getHeaders.iterator()
      //        while (iter.hasNext) {
      //          val entry = iter.next()
      //          entry.getValue.asScala.foreach(value =>
      //            b.append(s"< '${entry.getKey}: $value'\n")
      //          )
      //        }
      //      }
      b.append(s"< --times ${costMillis}ms")
      b.toString()
    }
  }

}

object HttpClient {
  val httpReqRespNumber = new AtomicInteger(0)

  def apply(config: AsyncHttpClientConfig,
            proxies: Vector[ProxyServer]): HttpClient = {
    new HttpClient(new DefaultAsyncHttpClient(config), proxies)
  }

  def apply(): HttpClient = {
    apply(Vector())
  }

  def apply(proxies: Vector[ProxyServer]): HttpClient = {
    val builder = new DefaultAsyncHttpClientConfig.Builder()
    builder.setMaxConnections(8192)
    builder.setMaxConnectionsPerHost(8192)
    builder.setConnectTimeout(30 * 1000)
    builder.setPooledConnectionIdleTimeout(30 * 1000)
    builder.setRequestTimeout(90 * 1000)
    //    builder.setAllowPoolingConnections(true)
    builder.setFollowRedirect(true)
    val config = builder.build()
    apply(config, proxies)
  }

  def createProxyServer(proxy: String) = {
    proxy match {
      case s if s.startsWith("http://") =>
        val arr = s.substring("http://".length).split(':')
        new ProxyServer.Builder(arr(0), arr(1).toInt).build()
      case s if s.startsWith("https://") =>
        val arr = s.substring("https://".length).split(':')
        new ProxyServer.Builder(arr(0), arr(1).toInt).build()
      case _ =>
        val arr = proxy.split(':')
        new ProxyServer.Builder(arr(0), arr(1).toInt).build()
    }
  }

  def find302Location(client: HttpClient, url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[String] = {
    val promise = Promise[String]()

    def findLocation() = client.get(url).header(headers: _*).setFollowRedirects(false).execute().map(_.getHeader("Location"))

    findLocation().onComplete {
      case Success(location) => promise.success(location)
      case Failure(e) =>
        findLocation().onComplete {
          case Success(location) => promise.success(location)
          case Failure(t) => promise.failure(t)
        }
    }

    promise.future
  }

  def equalsProxyServer(left: ProxyServer, right: ProxyServer) = {
    // TODO
    left.getHost == right.getHost &&
      left.getPort == right.getPort
    //      left.getProtocol == right.getProtocol &&
    //      left.getPrincipal == right.getPrincipal &&
    //      left.getPassword == right.getPassword
  }

}