package utils

import java.time.{Clock, LocalDateTime}


trait DateTimeFactory {
  protected val clock: Clock

  def now(): LocalDateTime = {
    LocalDateTime.now(clock)
  }
}

