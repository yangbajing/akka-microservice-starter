package microservice.starter.utils

import java.lang.reflect.InvocationTargetException
import java.time.LocalDateTime

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import org.json4s.JsonAST.JString
import org.json4s._
import org.json4s.jackson.Serialization

import scala.language.implicitConversions

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-07-28.
  */
trait JsonSupport {

  val serialization = Serialization
  implicit val formats: Formats = DefaultFormats + new LocalDateTimeSerializer()

  implicit def localDateTime2jvalue(ldt: LocalDateTime): JString = JString(ldt.format(TimeUtils.formatterDateTime))

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def json4sUnmarshaller[A: Manifest]: FromEntityUnmarshaller[A] = {
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset { (data, charset) =>
        try serialization.read(data.decodeString(charset.nioCharset.name))
        catch {
          case MappingException("unknown error", ite: InvocationTargetException) => throw ite.getCause
        }
      }
  }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode, must be upper bounded by `AnyRef`
    * @return marshaller for any `A` value
    */
  implicit def json4sMarshaller[A <: AnyRef](implicit shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] = {
    Marshaller.StringMarshaller.wrap(`application/json`)(shouldWritePretty match {
      case ShouldWritePretty.False => serialization.write[A]
      case _ => serialization.writePretty[A]
    })
  }
}

object JsonSupport extends JsonSupport

sealed abstract class ShouldWritePretty

object ShouldWritePretty {

  object True extends ShouldWritePretty

  object False extends ShouldWritePretty

}

class LocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format =>
  ( {
    case JString(s) => LocalDateTime.parse(s, TimeUtils.formatterDateTime)
    case JNull => null
  }, {
    case d: LocalDateTime => JString(TimeUtils.formatterDateTime.format(d))
  })
)