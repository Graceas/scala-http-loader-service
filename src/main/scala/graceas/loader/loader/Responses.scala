package graceas.loader.loader

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import graceas.loader.loader.LoaderOptions.LoaderOptions

case class Responses(
  responses: Seq[Response],
  @JsonScalaEnumeration(classOf[LoaderOptionsTypeReference])
  options:   Map[LoaderOptions, AnyVal]
)
