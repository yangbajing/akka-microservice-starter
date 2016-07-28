package microservice.starter.utils.http

import java.io.InputStream
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util

import io.netty.handler.codec.http.HttpHeaders
import org.asynchttpclient.Response
import org.asynchttpclient.cookie.Cookie
import org.asynchttpclient.uri.Uri

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-06-02.
  */
trait HttpClientResponse extends Response {
  def costMillis: Long
}

object HttpClientResponse {
  def apply(response: Response, _costMillis: Long): HttpClientResponse = new HttpClientResponse {
    override val costMillis: Long = _costMillis

    override def getLocalAddress: SocketAddress = response.getLocalAddress

    override def getRemoteAddress: SocketAddress = response.getRemoteAddress

    override def getResponseBody(charset: Charset): String = response.getResponseBody(charset)

    override def getResponseBodyAsByteBuffer: ByteBuffer = response.getResponseBodyAsByteBuffer

    override def getStatusCode: Int = response.getStatusCode

    override def getResponseBodyAsBytes: Array[Byte] = response.getResponseBodyAsBytes

    override def getResponseBodyAsStream: InputStream = response.getResponseBodyAsStream

    override def isRedirected: Boolean = response.isRedirected

    override def getCookies: util.List[Cookie] = response.getCookies

    override def hasResponseBody: Boolean = response.hasResponseBody

    override def getStatusText: String = response.getStatusText

    override def getHeaders: HttpHeaders = response.getHeaders

    override def getHeaders(name: String): util.List[String] = response.getHeaders(name)

    override def getHeader(name: String): String = response.getHeader(name)

    override def hasResponseHeaders: Boolean = response.hasResponseHeaders

    override def getResponseBody: String = response.getResponseBody

    override def getContentType: String = response.getContentType

    override def hasResponseStatus: Boolean = response.hasResponseStatus

    override def getUri: Uri = response.getUri
  }

}
