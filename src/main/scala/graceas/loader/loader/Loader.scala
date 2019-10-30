package graceas.loader.loader

import java.io.File
import java.nio.file.Path
import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.QueueOfferResult.Enqueued
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source, SourceQueueWithComplete}
import graceas.loader.core
import graceas.loader.helper.FileHelper
import graceas.loader.loader.LoaderOptions.LoaderOptions

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

class Loader()(implicit system: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) {

  val connection: Flow[(HttpRequest, Promise[HttpResponse]), (Try[HttpResponse], Promise[HttpResponse]), NotUsed] = Http().superPool[Promise[HttpResponse]](settings = ConnectionPoolSettings(system).withMaxConnections(512))
  val requestQueue: SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])] = Source.queue[(HttpRequest, Promise[HttpResponse])](2000, OverflowStrategy.dropNew)
    .via(connection)
    .toMat(Sink.foreach({
      case (Success(resp), p) => p.success(resp)
      case (Failure(e), p)    => p.failure(e)
    }))(Keep.left)
    .run

  FileHelper.createDir(FileHelper.tempDir())

  def executeRequest(request: Request): Future[Response] = {
    val promise = Promise[HttpResponse]
    requestQueue.offer(request.toHttpRequest -> promise).flatMap {
      case Enqueued => promise.future
      case _ => Future.failed(new RuntimeException())
    }.flatMap(response => {

      val name = FileHelper.uniqueFileName()
      val path = s"${FileHelper.tempDir()}$name"

      if (getOption[Boolean](request.options, LoaderOptions.RETURN_CONTENT, false)) {
        // strict entity to variable
        response.entity.toStrict(core.timeout).map(content => {
          Response(
            url        = request.url,
            method     = request.method,
            headers    = response.headers.map(header => (header.name(), header.value())).toMap,
            entityName = name,
            entity     = Some(content.data.utf8String),
            request    = if (getOption[Boolean](request.options, LoaderOptions.RETURN_REQUEST, true))
              Some(request)
            else None
          )
        })
      } else {
        // save entity to file
        response.entity.dataBytes.async.runWith(FileIO.toPath(Path.of(path)))
        Future.successful(Response(
          url        = request.url,
          method     = request.method,
          headers    = response.headers.map(header => (header.name(), header.value())).toMap,
          entityName = name,
          entity     = None,
          request    = if (getOption[Boolean](request.options, LoaderOptions.RETURN_REQUEST, true))
            Some(request)
          else None
        ))
      }
    })
  }

  def executeRequests(requests: Requests): Future[Responses] = {
    Future.sequence(requests.requests.map(request => {
      executeRequest(request)
    })).map(responses => {
      Responses(responses, requests.options)
    })
  }

  def entity(entityName: String): String = {
    try {
      UUID.fromString(entityName)
    } catch {
      case _: Throwable => return null
    }

    val file = new File(s"${FileHelper.tempDir()}$entityName")

    if (file.canRead && file.isFile) {
      val source = scala.io.Source.fromFile(file)
      val lines = try source.mkString finally source.close()

      lines
    } else {
      null
    }
  }

  private def getOption[T](options: Map[LoaderOptions, AnyVal], key: LoaderOptions, default: T): T = {
    if (options.contains(key) && options(key).isInstanceOf[T]) {
      options(key).asInstanceOf[T]
    } else {
      default
    }
  }
}
