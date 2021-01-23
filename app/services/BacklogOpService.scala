package services

import com.google.inject.{Inject, Singleton}
import config.GateWayConfig
import gateways.{Params, WebService}
import play.api.Logger
import play.api.http.Status

import scala.concurrent.Future

@Singleton
class BacklogOpService @Inject()(gateWayConfig: GateWayConfig, webService: WebService) {

  private val logger = Logger(this.getClass)

  def getSpace() = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    val params = Params("apiKey" -> "nYXneieTzwJotlwhAuqWoIBmkp7wLnH7nWh2w74cKIj0WaqDeo5Ox8Zu7JLTR00k")
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params) {
      case response => if (response.status == Status.OK) {
        logger.info(response.body)
        Future.successful(response.body)
      } else {
        Future.failed(new RuntimeException("ErroR"))
      }
    }
  }
}
