package gateways

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import config.ElasticsearchGateWayConfig
import exception.AppException
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{DefaultBodyWritables, WSResponse}
import utils.{BaseClientError, BaseSystemFailure, DateTimeFactoryImpl}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ElasticsearchGateWay @Inject()(gateWayConfig: ElasticsearchGateWayConfig,
                                     webService: WebService,
                                     dateTimeFactory: DateTimeFactoryImpl)
                                    (implicit ec: ExecutionContext) extends DefaultBodyWritables {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  final private val searchEndpoint = gateWayConfig.searchEndpoint

  def doSearch(): Future[String] = {
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_search"
    webService.getResponse(endpoint = apiUrl) {
      case response: WSResponse =>
        if (response.status == Status.OK)
          Future.successful(response.body)
        else
          throw new RuntimeException("Elasticsearch error!")
    }
  }

  /**
   *
   * @return
   */
  def writeIndex() = {
    val dateTime = dateTimeFactory.now().format(dateTimeFactory.dateTimeFormatter)
    val randomSuffix = Random.alphanumeric.take(8).mkString
    val docId = s"backlog-attachment_${dateTime}_${randomSuffix}"
    val apiUrl = s"$searchEndpoint/${gateWayConfig.fileSearchIndex}/_doc/$docId"

    val headers = Seq("Content-Type" -> ContentTypes.JSON)
    val indexBody = Map("source" -> "YXX地区",
      "filename" -> "测试文档",
      "data" -> "UWJveCBlbmFibGVzIGxhdW5jaGluZyBzdXBwb3J0ZWQsIGZ1bGx5LW1hbmFnZWQsIFJFU1RmdWwgRWxhc3RpY3NlYXJjaCBTZXJ2aWNlIGluc3RhbnRseS4g",
      "fileId" -> 10002,
      "uploadUser" -> "Test-User",
      "fileType" -> "pdf"
    )
    val mapper = new ObjectMapper()
    val jsonBody = mapper.writeValueAsString(indexBody)
    webService.putRequest(endpoint = apiUrl, headers = headers, payload = jsonBody) {
      response: WSResponse =>
        response.status match {
          case status if Status.isSuccessful(status) =>
            logger.debug(response.body)
            Future.successful(response.body)
          case status if Status.isClientError(status) =>
            logger.error(response.body)
            throw new AppException(BaseClientError(response.body))
          case status if Status.isServerError(status) =>
            logger.error(response.body)
            throw new AppException(BaseSystemFailure(response.body))
          case _ =>
            logger.error(response.body)
            throw new AppException(BaseSystemFailure(s"Elasticsearch other status:${response.body}"))
        }

    }
  }
}
