package chc.utils

import java.time.Clock
import java.time.format.DateTimeFormatter

import com.google.inject.Singleton

@Singleton
class DateTimeFactoryImpl  extends DateTimeFactory {
  override val clock: Clock = Clock.systemDefaultZone

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
}


object DateTimeFactoryImpl {

  def apply(): DateTimeFactoryImpl = new DateTimeFactoryImpl()
}