package graceas.loader.loader

case class Requests(
  requests: Seq[Request],
  options:  Map[String, AnyVal]
)
