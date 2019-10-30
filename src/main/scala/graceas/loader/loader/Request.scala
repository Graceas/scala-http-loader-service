package graceas.loader.loader

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import graceas.loader.loader.LoaderOptions.LoaderOptions

import scala.collection.immutable

case class Request(
  url:      String,
  method:   String,
  headers:  immutable.Seq[RawHeader],
  entity:   Option[RequestEntity],
  protocol: String = HttpProtocols.`HTTP/1.1`.value,
  @JsonScalaEnumeration(classOf[LoaderOptionsTypeReference])
  options:  Map[LoaderOptions, AnyVal]
) {
  def toHttpRequest: HttpRequest = {
    HttpRequest(
      method   = HttpMethods.getForKey(method).getOrElse(throw new Exception(s"HttpMethod for key $method is not defined")),
      uri      = Uri(url),
      headers  = headers,
      entity   = entity.getOrElse(HttpEntity.Empty),
      protocol = HttpProtocols.getForKey(
        if (protocol == null) HttpProtocols.`HTTP/1.1`.value else protocol
      ).getOrElse(throw new Exception(s"Protocol for key $protocol is not defined")),
    )
  }
}
