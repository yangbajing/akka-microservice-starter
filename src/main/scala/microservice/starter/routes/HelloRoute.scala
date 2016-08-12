package microservice.starter.routes

import javax.inject.Singleton

import akka.http.scaladsl.server.Directives._
import microservice.starter.utils.TimeUtils
import play.api.libs.json.{JsObject, Json}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
@Singleton
class HelloRoute {

  def apply(pathname: String = "hello") = {
    import microservice.starter.utils.PlayJsonSupport._
    pathPrefix(pathname) {
      path("say") {
        post {
          entity(as[JsObject]) { say =>
            val msg = Json.obj("receive" -> "收到用户消息", "currentTime" -> TimeUtils.now())
            val result = say ++ msg
            complete(result)
          }
        }
      }
    }
  }

}
