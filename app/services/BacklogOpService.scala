package services

import com.google.inject.{Inject, Singleton}
import config.BacklogGateWayConfig
import gateways.{Params, WebService}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status

import scala.concurrent.Future

@Singleton
class BacklogOpService @Inject()(gateWayConfig: BacklogGateWayConfig, webService: WebService) {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getSpace() = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    val params = Params("apiKey" -> gateWayConfig.backlogApiKey)
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params) {
      case response => if (response.status == Status.OK) {
        logger.info(response.body)
        Future.successful(response.body)
      } else {
        Future.failed(new RuntimeException("Get backlog space error"))
      }
    }
  }

}
