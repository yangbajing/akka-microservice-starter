package microservice.starter.routes

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.server.Directives._
import microservice.starter.domain.EmailMessage
import microservice.starter.services.EmailService

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
@Singleton
class EmailReoute @Inject()(emailService: EmailService) {


  def apply(pathname: String = "email") =
    pathPrefix(pathname) {
      path("send") {
        post {
          import microservice.starter.utils.PlayJsonSupport._
          entity(as[EmailMessage]) { emailMessage =>
            complete {
              emailService.sendEmail(emailMessage)
            }
          }
        } ~
          get {
            parameters('limit.as[Int], 'page.as[Long]) { (limit, page) =>
              complete {
                s"limit: $limit, page: $page"
              }
            }
          }
      }
    }

}
