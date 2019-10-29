package graceas.loader.loader

case class Responses(
  responses: Seq[Response],
  options:   Map[String, AnyVal]
)
