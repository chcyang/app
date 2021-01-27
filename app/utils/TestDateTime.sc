import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import utils.DateTimeFactoryImpl

val nowDate = DateTimeFactoryImpl.apply().now()
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
println(nowDate.toString.replaceAll("^([^0-9]+)$", ""))
println(nowDate.format(dateTimeFormatter))

LocalDateTime.parse("")

val queryString = Map("esQueryString" -> Seq("Test1", "Test2"))
//val requestBody = queryString.get("esQueryString").map{
//  seq=>for( term <-seq ) yield  Map("query" -> term, "minimum_should_match" -> "75%").asJava
//
//}
//.map {
//    contents => for (content <- contents ) yield Map("attachment.content" -> content).asJava
//  }
////  .map{
////    content =>val jsonBody = new ObjectMapper().writeValueAsString(content.asJava)
////      println(jsonBody)
////      jsonBody
////  }
//
////val res23 =  requestBody
//  .map {
//    matchFields => for(matchField <- matchFields) yield Map("match" -> matchField).asJava
//  }
//  .map(seq=>seq.asJava)
//  .map {
//    should => Map("should" -> should).asJava
//  }.map {
//  bool => Map("bool" -> bool).asJava
//}.map {
//  query => Map("query" -> query).asJava
//}.getOrElse(Map().asJava)


val requestBody = queryString.get("esQueryString").map {
  seq => for (term <- seq) yield Map("query" -> term, "minimum_should_match" -> "75%")
}
  .map {
    contents => for (content <- contents) yield Map("attachment.content" -> content)
  }
  .map {
    matchFields => for (matchField <- matchFields) yield Map("match" -> matchField)
  }
  .map(seq => seq)
  .map {
    should => Map("should" -> should)
  }.map {
  bool => Map("bool" -> bool)
}.map {
  query => Map("query" -> query)
}.getOrElse(Map())

val jsonBody = new ObjectMapper().registerModule(DefaultScalaModule)
  .writeValueAsString(requestBody)

println(jsonBody)