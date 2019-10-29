package graceas.loader.utils

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directive0, Directives, UnsupportedRequestContentTypeRejection}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import scaldi.Injectable
import graceas.loader.GraceasLoaderModule._

import scala.reflect.ClassTag


trait JsonMarshallers extends Directives with Injectable {

  private val jackson = inject[ObjectMapper with ScalaObjectMapper]

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
        .mapWithCharset {
          case (ByteString.empty, _) => throw Unmarshaller.NoContentException
          case (data, charset)       => data.decodeString(charset.nioCharset.name)
        }

  implicit protected def unmarshaller[A](implicit ct: ClassTag[A]): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => jackson.readValue(data, ct.runtimeClass).asInstanceOf[A])

  implicit protected val JsonMarshaller: ToEntityMarshaller[Any] =
    Marshaller.opaque[Any, MessageEntity] { m =>
      HttpEntity.Strict(ContentTypes.`application/json`, ByteString(jackson.writeValueAsBytes(m)))
    }

  def contentType[T](contentType: String): Directive0 =
    extract(_.request.entity) flatMap {
      case e if e.contentType.value equalsIgnoreCase contentType =>
        pass
      case _ =>
        reject(UnsupportedRequestContentTypeRejection(Set(MediaType.custom(contentType, binary = false))))
    }

}
