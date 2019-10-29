package graceas.loader

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import graceas.loader.config.Config
import graceas.loader.utils.Logging
import scaldi.Injectable
import graceas.loader.GraceasLoaderModule._
import graceas.loader.route.HttpRouter
import graceas.loader.utils.scheduler.Scheduler

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object Boot extends App with Injectable with Logging {

  private implicit val system: ActorSystem = ActorSystem("GraceasLoaderHttpServer")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val config: Config = inject[Config]

  val apiDispatcher: ExecutionContext = system.dispatchers.lookup("akka.blocking-api-dispatcher")
  val httpRouter  = new HttpRouter()(system, materializer, apiDispatcher)

  lazy val routes: Route = httpRouter.routes

  val scheduler = inject[Scheduler]
  log.info(s"Run scheduler, with ${scheduler.taskRegistry.tasks.size} tasks")

  scheduler.run()

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, config.http.host, config.http.port)

  serverBinding.onComplete {
    case Success(bound) =>
      log.info(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      log.error(s"Server could not start! Error: ${e.getMessage}")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
