package config

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config

import scala.jdk.DurationConverters._

@Singleton
class BaseConfig @Inject()(config: Config) {

  final private val baseConfig = config.getConfig("io.github.chc")

  lazy val serviceTimeout = baseConfig.getDuration("service-timeout").toScala


}
