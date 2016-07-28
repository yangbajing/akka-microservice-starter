package microservice.starter.routes

import javax.inject.Singleton

import akka.http.scaladsl.server.Directives._
import microservice.starter.utils.JsonSupport._
import microservice.starter.utils.TimeUtils
import org.json4s.JsonAST.JObject

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
@Singleton
class HelloRoute {

  def apply(pathname: String = "hello") = {

    pathPrefix(pathname) {
      path("say") {
        post {
          entity(as[JObject]) { say =>
            import org.json4s.JsonDSL._
            val msg = ("receive" -> "收到用户消息") ~~ ("currentTime" -> TimeUtils.now())
            val result = say merge msg
            complete(result)
          }
        }
      }
    }
  }

}
