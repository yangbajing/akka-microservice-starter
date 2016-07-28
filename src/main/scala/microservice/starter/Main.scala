package microservice.starter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import microservice.starter.routes.ApiRoute
import microservice.starter.utils.SettingComponent

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    implicit val system = AppServer.injector.getInstance(classOf[ActorSystem])
    implicit val dispatcher = AppServer.injector.getInstance(classOf[ExecutionContextExecutor])
    implicit val mat = AppServer.injector.getInstance(classOf[Materializer])
    val setting = AppServer.injector.getInstance(classOf[SettingComponent])

    val apiRoute = AppServer.injector.getInstance(classOf[ApiRoute])

    Http()
      .bindAndHandle(apiRoute(), setting.server.http.interface, setting.server.http.port)
      .onComplete {
        case Success(binding) =>
          logger.info(s"binding: $binding")
        case Failure(e) =>
          e.printStackTrace()
          AppServer.stop()
      }
  }

}
