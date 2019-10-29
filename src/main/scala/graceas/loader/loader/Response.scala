package graceas.loader.loader

case class Response(
  url:        String,
  method:     String,
  headers:    Map[String, String],
  entityPath: String,
  entity:     Option[String],
  request:    Request
)
