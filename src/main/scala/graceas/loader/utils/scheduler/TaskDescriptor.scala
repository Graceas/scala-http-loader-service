package graceas.loader.utils.scheduler

import scala.concurrent.duration.FiniteDuration

case class TaskDescriptor(
  initialDelay: FiniteDuration,
  interval:     FiniteDuration
)
