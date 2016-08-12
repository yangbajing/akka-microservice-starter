package microservice.starter.routes

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
@Singleton
class ApiRoute @Inject()(helloRoute: HelloRoute,
                         emailRoute: EmailReoute) {

  def apply(pathname: String = "api") =
    pathPrefix(pathname) {
      path("health_check") {
        (get | head) {
          complete(HttpResponse(entity = HttpEntity.Empty))
        }
      } ~
        helloRoute() ~
        emailRoute()
    }

}
