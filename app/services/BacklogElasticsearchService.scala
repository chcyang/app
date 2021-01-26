package services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import config.ElasticsearchGateWayConfig
import gateways.{ElasticsearchGateWay, Params}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.ContentTypes
import play.api.routing.sird.QueryString
import utils.DateTimeFactoryImpl

import scala.jdk.CollectionConverters._
import scala.util.Random

@Singleton
class BacklogElasticsearchService @Inject()(elasticsearchGateWay: ElasticsearchGateWay,
                                            gateWayConfig: ElasticsearchGateWayConfig,
                                            dateTimeFactory: DateTimeFactoryImpl,
                                            mapper: ObjectMapper) {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  final private val searchEndpoint = gateWayConfig.searchEndpoint

  /**
   * get all docs from a certain index
   *
   * @return json sting
   */
  def getAll() = {
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_search"
    elasticsearchGateWay.getAll(apiUrl)
  }

  /**
   * post a querystring to search file contents form elasticsearch
   *
   * @param queryString
   * @return json string
   */
  def doSearch(queryString: QueryString) = {
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_search"

    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val requestBody = queryString.get("esQueryString")
      .map { seq => for (term <- seq) yield Map("query" -> term, "minimum_should_match" -> "75%").asJava }
      .map { contents => for (content <- contents) yield Map("attachment.content" -> content).asJava }
      .map { matchFields => for (matchField <- matchFields) yield Map("match" -> matchField).asJava }
      .map { seq => seq.asJava }
      .map { should => Map("should" -> should).asJava }
      .map { bool => Map("bool" -> bool).asJava }
      .map { query => Map("query" -> query).asJava }
      .getOrElse(Map().asJava)

    val jsonBody = new ObjectMapper().writeValueAsString(requestBody)
    logger.info(s"Es search queryString:$jsonBody")
    elasticsearchGateWay.doSearch(endpoint = apiUrl,
      payload = jsonBody,
      headers = headers)
  }

  /**
   * add one fixed doc for test
   *
   * @return
   */
  def addIndex() = {
    val docId = getDocId("backlog-attachment")
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_doc/$docId"

    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val indexBody = Map("source" -> "YXX地区",
      "filename" -> "测试文档",
      "data" -> "UWJveCBlbmFibGVzIGxhdW5jaGluZyBzdXBwb3J0ZWQsIGZ1bGx5LW1hbmFnZWQsIFJFU1RmdWwgRWxhc3RpY3NlYXJjaCBTZXJ2aWNlIGluc3RhbnRseS4g",
      "fileId" -> 10002,
      "uploadUser" -> "Test-User",
      "fileType" -> "pdf"
    ).asJava

    val params = Params(
      "pipeline" -> "attachment",
      "pretty" -> "")

    val jsonBody = mapper.writeValueAsString(indexBody)
    elasticsearchGateWay.writeIndex(endpoint = apiUrl,
      payload = jsonBody,
      params = params,
      headers = headers)
  }

  /**
   * add index doc by params
   *
   * @param data
   * @param fileType
   * @param fileId
   * @param uploadUser
   * @param fileName
   * @param source
   * @return json string
   */
  def addIndex(data: String,
               fileType: String,
               fileId: Int,
               uploadUser: String,
               fileName: String,
               source: String) = {

    val docId = getDocId("backlog-attachment")
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_doc/$docId"

    val params = Params(
      "pipeline" -> "attachment",
      "pretty" -> "")
    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val indexBody = Map("source" -> source,
      "filename" -> fileName,
      "data" -> data,
      "fileId" -> fileId,
      "uploadUser" -> uploadUser,
      "fileType" -> fileType
    ).asJava

    val jsonBody = mapper.writeValueAsString(indexBody)
    elasticsearchGateWay.writeIndex(endpoint = apiUrl,
      payload = jsonBody,
      params = params,
      headers = headers)
  }

  /**
   *
   * @param indexPrefix
   * @return docId:String
   */
  private def getDocId(indexPrefix: String): String = {
    val dateTime = dateTimeFactory.now().format(dateTimeFactory.dateTimeFormatter)
    val randomSuffix = Random.alphanumeric.take(8).mkString
    val docId = s"${indexPrefix}_${dateTime}_${randomSuffix}"
    docId
  }
}
