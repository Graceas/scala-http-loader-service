package graceas.loader.utils.scheduler

import graceas.loader.utils.Logging
import scaldi.Injectable

import scala.concurrent.{ExecutionContext, Future}

trait Task extends Injectable with Logging {
  val descriptor: TaskDescriptor

  def execute()(implicit executor: ExecutionContext): Future[Boolean]
}
