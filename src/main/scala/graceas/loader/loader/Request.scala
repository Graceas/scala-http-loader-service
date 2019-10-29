package graceas.loader.loader

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader

import scala.collection.immutable

case class Request(
  url:      String,
  method:   String,
  headers:  immutable.Seq[RawHeader],
  entity:   Option[RequestEntity],
  protocol: String = HttpProtocols.`HTTP/1.1`.value,
  options:  Map[String, AnyVal]
) {
  def toHttpRequest: HttpRequest = {
    HttpRequest(
      HttpMethods.getForKey(method).getOrElse(throw new Exception(s"HttpMethod for key $method is not defined")),
      Uri(url),
      headers,
      entity.getOrElse(HttpEntity.Empty),
      HttpProtocols.getForKey(
        if (protocol == null) HttpProtocols.`HTTP/1.1`.value else protocol
      ).getOrElse(throw new Exception(s"Protocol for key $protocol is not defined")),
    )
  }
}
