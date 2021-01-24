package gateways

import com.google.inject.{Inject, Singleton}
import config.ElasticsearchGateWayConfig
import play.api.http.Status
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ElasticsearchGateWay @Inject()(gateWayConfig: ElasticsearchGateWayConfig, webService: WebService)(implicit ec: ExecutionContext) {

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

  def writeIndex(): Unit = {

  }
}
