package config

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.Logger

import scala.jdk.DurationConverters._

@Singleton
class GateWayConfig @Inject()(config: Config) {

  private val logger = Logger(this.getClass)

  val gateWayConfig = config.getConfig("io.github.chc.backlog.gateway")

  val baseUrl = gateWayConfig.getString("baseUrl")

  val spaceApiPath = gateWayConfig.getString("api-path-get-space")

  val serviceTimeout = gateWayConfig.getDuration("service-timeout").toScala
}
