package microservice.starter.utils

import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-08-12.
  */
object JacksonSupport extends JacksonSupport {
  val defaultObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
}

trait JacksonSupport {
  import JacksonSupport._

  /**
    * HTTP entity => `A`
    *
    * @param objectMapper
    * @tparam A
    * @return
    */
  implicit def jacksonUnmarshaller[A](
                                       implicit
                                       ct: ClassTag[A],
                                       objectMapper: ObjectMapper = defaultObjectMapper
                                     ): FromEntityUnmarshaller[A] = {
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset((data, charset) => {
        val x: A = objectMapper.readValue(
          data.decodeString(charset.nioCharset.name), ct.runtimeClass
        ).asInstanceOf[A]
        x
      })
  }

  /**
    * `A` => HTTP entity
    *
    * @param objectMapper
    * @tparam Object
    * @return
    */
  implicit def jacksonToEntityMarshaller[Object](
                                                  implicit
                                                  objectMapper: ObjectMapper = defaultObjectMapper
                                                ): ToEntityMarshaller[Object] = {
    Jackson.marshaller[Object](objectMapper)
  }
}
