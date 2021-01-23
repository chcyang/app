package utils

object DurationConverter {

  implicit def asFiniteDuration(d: java.time.Duration) =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)
}
