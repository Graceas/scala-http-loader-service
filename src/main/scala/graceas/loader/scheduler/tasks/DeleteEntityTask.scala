package graceas.loader.scheduler.tasks

import java.util.Date

import graceas.loader.config.Config
import graceas.loader.helper.FileHelper
import graceas.loader.utils.scheduler.{Task, TaskDescriptor}
import graceas.loader.GraceasLoaderModule._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object DeleteEntityTask extends Task {
  override val descriptor: TaskDescriptor = TaskDescriptor(
    initialDelay = 5 minutes,
    interval     = 10 minutes
  )

  override def execute()(implicit executor: ExecutionContext): Future[Boolean] = {
    log.debug("Running delete entity task")

    val files = FileHelper.getListOfFiles(FileHelper.tempDir())

    files.foreach(file => {
      val diff = new Date().getTime - file.lastModified

      if (diff > inject[Config].loader.cacheEntityExpiredMinutes * 60 * 1000) {
        // remove files older than 20 minutes
        log.debug(s"Delete cache file ${file.getAbsolutePath}")
        file.delete()

      }
    })

    Future.successful(true)
  }
}
