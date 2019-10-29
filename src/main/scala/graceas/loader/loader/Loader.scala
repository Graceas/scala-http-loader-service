package graceas.loader.loader

import java.io.File
import java.nio.file.{OpenOption, Path}

import sun.nio.fs.UnixPath
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

      val path = FileHelper.uniqueFileName()
      response.entity.dataBytes.async.runWith(FileIO.toPath(Path.of(path)))

      if (request.options.contains("return_content") && request.options("return_content").asInstanceOf[Boolean]) {
        response.entity.toStrict(core.timeout).map(content => {
          Response(
            request.url,
            request.method,
            response.headers.map(header => (header.name(), header.value())).toMap,
            path,
            Some(content.data.utf8String),
            request
          )
        })
      } else {
        Future.successful(Response(
          request.url,
          request.method,
          response.headers.map(header => (header.name(), header.value())).toMap,
          path,
          None,
          request
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
}
