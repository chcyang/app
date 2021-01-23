package gateways

import com.google.inject.{Inject, Singleton}
import config.GateWayConfig
import play.api.Logger
import play.api.libs.ws.{BodyWritable, WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WebService @Inject()(gateWayConfig: GateWayConfig, ws: WSClient)(implicit ec: ExecutionContext) {

  val logger = Logger(this.getClass)

  def getResponse[T](endpoint: String,
                     params: Params = Params.empty,
                     timeout: Duration = gateWayConfig.serviceTimeout)(f: WSResponse => Future[T]) = {
    val responseF = get(endpoint, params, timeout)
    transformResponse(endpoint, params, responseF)(f)
  }

  def getResponseWithHeaders[T](endpoint: String,
                                params: Params = Params.empty,
                                headers: Seq[(String, String)] = Seq.empty,
                                timeout: Duration = gateWayConfig.serviceTimeout)(f: WSResponse => Future[T]) = {
    val responseF = getWithHeaders(endpoint, params, headers, timeout)
    transformResponse(endpoint, params, responseF)(f)
  }

  def getHeadResponse[T](endpoint: String,
                         timeout: Duration = gateWayConfig.serviceTimeout)(f: WSResponse => Future[T]) = {
    val responseF = head(endpoint, timeout)
    transformResponse(endpoint, Params.empty, responseF)(f)
  }

  def postRequest[T, S](endpoint: String,
                        payload: S,
                        params: Params = Params.empty,
                        headers: Seq[(String, String)] = Seq.empty,
                        timeout: Duration = gateWayConfig.serviceTimeout)
                       (f: WSResponse => Future[T])(implicit wrt: BodyWritable[S]) = {
    val responseF = post(endpoint, payload, params, headers, timeout)
    transformResponse(endpoint, Params.empty, responseF)(f)
  }

  protected def get(endpoint: String,
                    params: Params = Params.empty,
                    timeout: Duration = gateWayConfig.serviceTimeout) =
    getRequestHolder(endpoint, params)
      .withRequestTimeout(timeout)
      .get()

  protected def getWithHeaders(endpoint: String,
                               params: Params = Params.empty,
                               headers: Seq[(String, String)] = Seq.empty,
                               timeout: Duration = gateWayConfig.serviceTimeout) =
    getRequestHolder(endpoint, params)
      .addHttpHeaders(headers: _*)
      .withRequestTimeout(timeout)
      .get()

  protected def head(endpoint: String,
                     timeout: Duration = gateWayConfig.serviceTimeout) =
    getRequestHolder(endpoint)
      .withRequestTimeout(timeout)
      .head()

  protected def post[T](endpoint: String,
                        payload: T,
                        params: Params,
                        headers: Seq[(String, String)],
                        timeout: Duration)
                       (implicit wrt: BodyWritable[T]) =
    getRequestHolder(endpoint, params)
      .addHttpHeaders(headers: _*)
      .withRequestTimeout(timeout)
      .post(payload)

  private def transformResponse[T](endpoint: String,
                                   params: Params = Params.empty,
                                   responseF: Future[(WSResponse)])
                                  (f: WSResponse => Future[T]): Future[T] = {
    responseF flatMap { case (response) =>
      f(response)
    } recover {
      case ex =>
        throw ex
    }
  }

  private def getRequestHolder(endpoint: String, params: Params = Params.empty) =
    ws.url(endpoint)
      .addQueryStringParameters(params.value: _*)
}


case class Params(value: (String, String)*) {

  override def toString = s"Params(${value.mkString(", ")})"

  def logString = value.map(param => s"(${param._1}=${param._2})").mkString(", ")

  def ++(that: Params) = Params((value ++ that.value): _*)

  def :+(p: (String, String)) = Params((value :+ p): _*)
}

object Params {
  val empty = Params()
}