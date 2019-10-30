package graceas.loader.loader

case class Response(
  url:        String,
  method:     String,
  headers:    Map[String, String],
  entityName: String,
  entity:     Option[String],
  request:    Option[Request]
)
