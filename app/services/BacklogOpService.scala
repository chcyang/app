package services

import com.google.inject.{Inject, Singleton}
import config.BacklogGateWayConfig
import exception.AppException
import gateways.{Params, WebService}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status
import play.api.libs.ws.WSResponse
import utils.{BaseClientError, BaseSystemFailure}

import scala.concurrent.Future

@Singleton
class BacklogOpService @Inject()(gateWayConfig: BacklogGateWayConfig, webService: WebService) {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getSpace(): Future[String] = {
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

  def getSharedFiles():Future[String]  = {

    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    val params = Params("apiKey" -> gateWayConfig.backlogApiKey,
      "order"->"asc",
      "offset"->"123",
      "count"-> "1000"
    )
    webService.getResponseWithHeaders(endpoint, params = params) (esResponseTransform)
  }

  private def esResponseTransform(response: WSResponse) = {
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
