package graceas.loader

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import graceas.loader.config.Config
import graceas.loader.scheduler.LoaderTaskRegistry
import graceas.loader.utils.JsonFormatter
import graceas.loader.utils.scheduler.Scheduler
import scaldi.{Injector, Module}

import scala.concurrent.ExecutionContext

class GraceasLoaderModule extends Module {
  // Akka back-end
  bind [ActorSystem] to ActorSystem()
  bind [ExecutionContext] to inject[ActorSystem].dispatcher
  bind [ActorMaterializer] to ActorMaterializer()(inject[ActorSystem])

  // Config
  bind [Config] to Config.load()

  // JSON
  bind [ObjectMapper with ScalaObjectMapper] to {
    JsonFormatter.mapper
  }

  // Scheduler
  bind [Scheduler] to new Scheduler(taskRegistry = LoaderTaskRegistry)(inject[ExecutionContext], inject[ActorSystem], inject[Config])

}

object GraceasLoaderModule {
  implicit val injector: Injector = new GraceasLoaderModule
}