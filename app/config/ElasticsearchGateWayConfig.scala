package config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config

import scala.jdk.DurationConverters._

@Singleton
class ElasticsearchGateWayConfig @Inject()(config: Config) {

  final private val esConfig = config.getConfig("io.github.chc.elasticsearch.gateway")

  lazy val searchEndpoint = esConfig.getString("search-endpoint")

  lazy val esServiceTimeout = esConfig.getDuration("service-timeout").toScala

  lazy val fileSearchIndex = esConfig.getString("file-search-index")
}
