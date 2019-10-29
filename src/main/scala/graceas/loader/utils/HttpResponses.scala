package graceas.loader.utils

import akka.http.scaladsl.model._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import scaldi.Injectable
import graceas.loader.GraceasLoaderModule._

import scala.concurrent.{ExecutionContext, Future}


trait HttpResponses extends Injectable {

  private val jackson = inject[ObjectMapper with ScalaObjectMapper]

  def okResponse[T](future: Future[T])(implicit executor: ExecutionContext): Future[HttpResponse] = {
    future.map { value: T =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(contentType = ContentTypes.`application/json`, jackson.writeValueAsString(value)))
    }
  }

  def okResponse[T](value: T): HttpResponse = {
    HttpResponse(StatusCodes.OK, entity = HttpEntity(contentType = ContentTypes.`application/json`, jackson.writeValueAsString(value)))
  }

  def errorResponse(code: StatusCode, errorType: String, errorMessage: String): HttpResponse = {
    HttpResponse(code, entity = jackson.writeValueAsString(Map(
      "error"   -> errorType,
      "message" -> errorMessage
    )))
  }

  def errorResponseWithCode(code: StatusCode, errorType: String, errorMessage: String, errorInternalCode: String): HttpResponse = {
    HttpResponse(code, entity = jackson.writeValueAsString(Map(
      "error"   -> errorType,
      "code"    -> errorInternalCode,
      "message" -> errorMessage
    )))
  }

  def errorResponseWithData(code: StatusCode, errorType: String, errorMessage: String, data: Any): HttpResponse = {
    HttpResponse(code, entity = jackson.writeValueAsString(Map(
      "error"   -> errorType,
      "message" -> errorMessage,
      "data"    -> data
    )))
  }

  def errorResponseWithDataAndCode(code: StatusCode, errorType: String, errorMessage: String, data: Any, errorInternalCode: String): HttpResponse = {
    HttpResponse(code, entity = jackson.writeValueAsString(Map(
      "error"   -> errorType,
      "code"    -> errorInternalCode,
      "message" -> errorMessage,
      "data"    -> data
    )))
  }

  def errorResponse[T](code: StatusCode): HttpResponse = {
    HttpResponse(code)
  }

}
