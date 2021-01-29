package chc.gateways

import chc.exception.AppException
import chc.utils.{BaseClientError, BaseSystemFailure}
import com.google.inject.{Inject, Singleton}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status
import play.api.libs.ws.{DefaultBodyWritables, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ElasticsearchGateWay @Inject()(webService: WebService)
                                    (implicit ec: ExecutionContext) extends DefaultBodyWritables {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)


  /**
   * get all docs form a certain index
   *
   * @param endpoint
   * @return json string
   */
  def getAll(endpoint: String): Future[String] = {
    webService.getResponse(endpoint = endpoint)(esResponseTransform)
  }

  /**
   *
   * @param endpoint
   * @param payload
   * @param params
   * @param headers
   * @return
   */
  def doSearch(endpoint: String,
               payload: String,
               params: Params = Params.empty,
               headers: Seq[(String, String)] = Seq.empty): Future[String] = {

    webService.postRequest(endpoint = endpoint, payload = payload, headers = headers)(esResponseTransform)
  }


  /**
   * add one index doc
   *
   * @param endpoint
   * @param payload
   * @param params
   * @param headers
   * @return
   */
  def writeIndex(endpoint: String,
                 payload: String,
                 params: Params = Params.empty,
                 headers: Seq[(String, String)] = Seq.empty) = {

    webService.putRequest(endpoint = endpoint,
      payload = payload,
      params = params,
      headers = headers)(esResponseTransform)
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
