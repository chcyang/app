package services

import chc.adapter.WService
import chc.gateways.Params
import play.api.libs.ws.{BodyWritable, WSResponse}
import play.shaded.ahc.org.asynchttpclient.Response.ResponseBuilder

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** *
 * Test Mock WebService
 *
 * @param body
 */
class WebServiceMock(body: String) extends WService {

  import scala.concurrent.ExecutionContext.Implicits.global

  val responsebuilder = new ResponseBuilder()

  override def getResponse[T](endpoint: String, params: Params, timeout: Duration)(f: WSResponse => Future[T]): Future[T] = ???

  override def getResponseWithHeaders[T](endpoint: String, params: Params, headers: Seq[(String, String)], timeout: Duration)(f: WSResponse => Future[T]): Future[T] = {

    import play.api.libs.ws.ahc.AhcWSResponse
    import play.api.libs.ws.ahc.cache.{CacheableHttpResponseBodyPart, CacheableHttpResponseStatus}
    import play.shaded.ahc.io.netty.handler.codec.http.DefaultHttpHeaders
    import play.shaded.ahc.org.asynchttpclient.uri.Uri

    val responsebuilder = new ResponseBuilder()
    responsebuilder.accumulate(new CacheableHttpResponseStatus(Uri.create("http://127.0.0.1/api/v2/projects/11881668/files/metadata/"), 200, "status text", "protocols!"))
    responsebuilder.accumulate(new DefaultHttpHeaders().add("Content-Type", "application/json"))
    responsebuilder.accumulate(new CacheableHttpResponseBodyPart(body.getBytes(), true))

    val resp = new AhcWSResponse(responsebuilder.build())
    Future.successful(resp).flatMap(r => f(r))
  }

  override def postRequest[T, S](endpoint: String, payload: S, params: Params, headers: Seq[(String, String)], timeout: Duration)(f: WSResponse => Future[T])(implicit wrt: BodyWritable[S]): Future[T] = ???

  override def putRequest[T, S](endpoint: String, payload: S, params: Params, headers: Seq[(String, String)], timeout: Duration)(f: WSResponse => Future[T])(implicit wrt: BodyWritable[S]): Future[T] = ???
}

object WebServiceMock {
  def apply(body: String): WebServiceMock = new WebServiceMock(body)
}