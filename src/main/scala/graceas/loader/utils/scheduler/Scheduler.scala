package graceas.loader.utils.scheduler

import akka.actor.ActorSystem
import graceas.loader.config.Config

import scala.concurrent.ExecutionContext

class Scheduler(
  val taskRegistry: TaskRegistry
)(implicit executor: ExecutionContext, actorSystem: ActorSystem, config: Config) {
  def run(): Unit = {
    taskRegistry.tasks.foreach(task => {
      actorSystem.scheduler.schedule(task.descriptor.initialDelay, task.descriptor.interval)(task.execute)
    })
  }
}
