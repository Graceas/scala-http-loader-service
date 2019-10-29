package graceas.loader.config

import java.nio.file.Paths

import graceas.loader.utils.Logging
import pureconfig.loadConfig

import scala.concurrent.duration.FiniteDuration
import scala.sys.SystemProperties

case class Config(
  secretKey: String,
  http: HttpConfig,
  loader: LoaderConfig,
)

object Config extends Logging {
  val config: Config = load()

  def load(): Config = {
    new SystemProperties().get("conf.file").fold({
      log.info("Loading default config")
      loadConfig[Config]
    }) { configPath =>
      log.info(s"Loading config from $configPath")
      loadConfig[Config](Paths.get(configPath))
    } match {
      case Right(configM) => configM
      case Left(error) =>
        throw new RuntimeException("Cannot read config file, errors:\n" + error.toList.mkString("\n"))
    }
  }
}

case class HttpConfig(startServer: Boolean, host: String, port: Int, logLevel: String, mockAuth: Boolean)
case class LoaderConfig(cacheEntityExpiredMinutes: Int)
