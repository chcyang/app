package chc.config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config

import scala.jdk.DurationConverters._

@Singleton
class BacklogGateWayConfig @Inject()(config: Config) extends BaseConfig(config: Config) {

  final private val gateWayConfig = config.getConfig("io.github.chc.backlog.gateway")

  lazy val baseUrl = gateWayConfig.getString("baseUrl")

  lazy val spaceApiPath = gateWayConfig.getString("api-path-get-space")

  lazy val getAllIssueApiPath = gateWayConfig.getString("api-path-get-issues")

  lazy val getALlFilesApiPath = gateWayConfig.getString("api-path-get-all-files")

  lazy val backlogServiceTimeout = gateWayConfig.getDuration("service-timeout").toScala

  lazy val backlogApiKey = gateWayConfig.getString("backlog-api-key")
}
