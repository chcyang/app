package chc.services

import chc.adapter.BacklogElasticsearchService
import chc.config.ElasticsearchGateWayConfig
import chc.gateways.{ElasticsearchGateWay, Params}
import chc.utils.DateTimeFactoryImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.{Inject, Singleton}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.ContentTypes
import play.api.routing.sird.QueryString
import play.shaded.oauth.org.apache.commons.codec.digest.DigestUtils

import scala.concurrent.Future

@Singleton
class BacklogElasticsearchServiceImpl @Inject()(elasticsearchGateWay: ElasticsearchGateWay,
                                                gateWayConfig: ElasticsearchGateWayConfig,
                                                dateTimeFactory: DateTimeFactoryImpl,
                                                mapper: ObjectMapper) extends BacklogElasticsearchService {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  final private val searchEndpoint = gateWayConfig.searchEndpoint

  // set mapper as scala Mode
  mapper.registerModule(DefaultScalaModule)

  /**
   * get all docs from a certain index
   *
   * @return json sting
   */
  def getAll(): Future[String] = {
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_search"
    elasticsearchGateWay.getAll(apiUrl)
  }

  /**
   * post a querystring to search file contents form elasticsearch
   *
   * @param queryString
   * @return json string
   */
  def doSearch(queryString: QueryString): Future[String] = {
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_search"

    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val requestBody = queryString.get("esQueryString")
      .map { seq => for (term <- seq) yield Map("query" -> term, "minimum_should_match" -> "75%") }
      .map { contents => for (content <- contents) yield Map("attachment.content" -> content) }
      .map { matchFields => for (matchField <- matchFields) yield Map("match" -> matchField) }
      .map { should => Map("should" -> should) }
      .map { bool => Map("bool" -> bool) }
      .map { query => Map("query" -> query) }
      .getOrElse(Map())

    val jsonBody = mapper.writeValueAsString(requestBody)
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
  def addIndex(): Future[String] = {

    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val indexBody = Map("source" -> "YXX地区",
      "filename" -> "テストドキュメント",
      "data" -> "UWJveCBlbmFibGVzIGxhdW5jaGluZyBzdXBwb3J0ZWQsIGZ1bGx5LW1hbmFnZWQsIFJFU1RmdWwgRWxhc3RpY3NlYXJjaCBTZXJ2aWNlIGluc3RhbnRseS4g",
      "fileId" -> 10002,
      "uploadUser" -> "Test-User",
      "fileType" -> "pdf",
      "source" -> ""
    )

    val params = Params(
      "pipeline" -> "attachment",
      "pretty" -> "")

    val docId = getDocId("backlog-attachment", "10002" + "テストドキュメント" + "")
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_doc/$docId"

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
               source: String): Future[String] = {

    val docId = getDocId("backlog-attachment", fileId.toString + fileName + source)
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
    )

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
  private def getDocId(indexPrefix: String, fileUniqueName: String): String = {
    val md5 = DigestUtils.md5Hex(fileUniqueName)
    val docId = s"${indexPrefix}_${md5}"
    docId
  }
}
