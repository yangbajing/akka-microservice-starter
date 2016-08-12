package microservice.starter.utils

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import play.api.libs.json._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-12.
  */
trait PlayJsonSupport {
  /**
    * HTTP entity => `A`
    *
    * @param reads reader for `A`
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def playJsonUnmarshaller[A](implicit reads: Reads[A]): FromEntityUnmarshaller[A] = {
    def read(json: JsValue) = reads.reads(json).recoverTotal(error => throw new IllegalArgumentException(JsError.toJson(error).toString))
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset((data, charset) => read(Json.parse(data.decodeString(charset.nioCharset.name))))
  }

  /**
    * `A` => HTTP entity
    *
    * @param writes  writer for `A`
    * @param printer pretty printer function
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def playJsonMarshaller[A](implicit writes: Writes[A], printer: JsValue => String = Json.prettyPrint): ToEntityMarshaller[A] =
  Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer).compose(writes.writes)
}

object PlayJsonSupport extends PlayJsonSupport
