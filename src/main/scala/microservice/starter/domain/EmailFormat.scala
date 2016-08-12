package microservice.starter.domain

import play.api.libs.json._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-03.
  */
object EmailFormat extends Enumeration {
  type EmailFormat = Value

  val TEXT = Value

  val HTML = Value

  implicit val jsonFormats = new Format[EmailFormat] {
    override def writes(o: EmailFormat): JsValue = JsString(o.toString)

    override def reads(json: JsValue): JsResult[EmailFormat] = JsSuccess(withName(json.as[String]))
  }
}
