import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val time = "2021-01-20T00:00:00Z"

LocalDateTime.parse(time,DateTimeFormatter.ISO_ZONED_DATE_TIME)