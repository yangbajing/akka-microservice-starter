package microservice.starter.domain

import microservice.starter.domain.EmailFormat.EmailFormat
import play.api.libs.json.Json

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
case class EmailMessage(account: String,
                        nickname: String,
                        subject: String,
                        content: String,
                        tos: Seq[String],
                        charset: Option[String] = None,
                        format: Option[EmailFormat] = None)

object EmailMessage {
  implicit val jsonFormats = Json.format[EmailMessage]
}
