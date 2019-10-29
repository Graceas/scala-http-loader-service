package graceas.loader.route

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{HttpRequest, MediaTypes}
import akka.http.scaladsl.server.directives.{LogEntry, LoggingMagnet}
import akka.http.scaladsl.server.{Directives, Route, RouteResult, StandardRoute}
import akka.event.Logging.{InfoLevel, WarningLevel}
import akka.stream.Materializer
import graceas.loader.loader.{Loader, Request, Requests}
import graceas.loader.route.support.{CorsSupport, SecurityDirectives}
import graceas.loader.utils.{HandleErrorsDirectives, HttpResponses, JsonMarshallers, Logging}
import scaldi.Injectable

import scala.concurrent.ExecutionContext

class HttpRouter()(implicit system: ActorSystem, materializer: Materializer, apiDispatcher: ExecutionContext)
    extends Directives
      with CorsSupport
      with Injectable
      with Logging
      with HttpResponses
      with JsonMarshallers
      with HandleErrorsDirectives
      with SecurityDirectives
{

  private val loader: Loader = new Loader()

  val routes: Route =
    logging {
      pathPrefix("healthcheck") {
        get {
          complete("OK")
        }
      } ~
      pathPrefix("version") {
        get {
          complete("0.3.1")
        }
      } ~
      pathPrefix("api") {
        pathPrefix("v1") {
//          corsHandler {
            extract(_.request.headers) { headers =>
              handleErrors(DefaultErrorFormatter, headers) {
                path("execute") {
                  post {
                    entity(as[Request]) { request =>
                      println("start")
                      println(request)
                      println("end")
                      complete(loader.executeRequest(request).map(response => okResponse(response)))
                    }
                  }
                } ~
                path("execute-requests") {
                  post {
                    entity(as[Requests]) { requests =>
                      println(requests)
                      complete(loader.executeRequests(requests).map(response => okResponse(response)))
                    }
                  }
                }
              }
            }
//          }
        }
      }
    }

  private def logging = logRequestResult(LoggingMagnet(log => accessLogger(log, System.currentTimeMillis)(_)))

  private def accessLogger(log: LoggingAdapter, start: Long)(req: HttpRequest)(res: Any): Unit = {
    val entry = res match {
      case RouteResult.Complete(resp) =>
        LogEntry(
          s"${req.method.value} ${req.uri.toRelative} "
            +
            s"""${
              req.entity.contentType.mediaType match {
                case MediaTypes.`application/json` => ""
                case mt if mt.mainType == "none" => ""
                case mt => s"$mt "
              }
            }"""
            + s"<--- ${resp.status} ${System.currentTimeMillis - start} ms",
          if (resp.status.isSuccess || resp.status.intValue == 404) InfoLevel else WarningLevel
        )

      case RouteResult.Rejected(reason) =>
        LogEntry(
          s"${req.method.value} ${req.uri.toRelative} ${req.entity} <--- rejected: ${reason.mkString(",")} ${System.currentTimeMillis - start} ms",
          WarningLevel
        )
    }

    entry.logTo(log)
  }
}