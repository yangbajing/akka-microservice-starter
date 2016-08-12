package microservice.starter.routes

import javax.inject.Inject

import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
class ApiV1Route @Inject()(helloRoute: HelloRoute,
                           emailRoute: EmailReoute) {

  def apply(pathname: String = "apiv1") =
    pathPrefix(pathname) {
        emailRoute()
    }
}
