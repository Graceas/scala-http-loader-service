package graceas.loader.scheduler

import graceas.loader.scheduler.tasks._
import graceas.loader.utils.scheduler.{Task, TaskRegistry}

object LoaderTaskRegistry extends TaskRegistry {
  override def tasks: List[Task] = List(
    DeleteEntityTask
  )
}
