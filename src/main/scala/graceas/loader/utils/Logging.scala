package graceas.loader.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


trait Logging {

  val log: Logger = getLogger(this.getClass.getName)

  def getLogger(name: String) = Logger(LoggerFactory.getLogger(name))
}


object Logging extends Logging {
  override val log: Logger = getLogger(this.getClass.getName)
}