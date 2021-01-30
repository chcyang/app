package chc.adapter

import chc.gateways.Params
import play.api.libs.ws.{BodyWritable, WSResponse}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait WService {

  def getResponse[T](endpoint: String,
                     params: Params,
                     timeout: Duration)(f: WSResponse => Future[T]):Future[T]

  def getResponseWithHeaders[T](endpoint: String,
                                params: Params,
                                headers: Seq[(String, String)] = Seq.empty,
                                timeout: Duration)(f: WSResponse => Future[T]):Future[T]

  def postRequest[T, S](endpoint: String,
                        payload: S,
                        params: Params,
                        headers: Seq[(String, String)]= Seq.empty,
                        timeout: Duration)
                       (f: WSResponse => Future[T])(implicit wrt: BodyWritable[S]):Future[T]

  def putRequest[T, S](endpoint: String,
                       payload: S,
                       params: Params,
                       headers: Seq[(String, String)]= Seq.empty,
                       timeout: Duration)
                      (f: WSResponse => Future[T])(implicit wrt: BodyWritable[S]):Future[T]
}
