package gateways


import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetWsClient @Inject()(ws: WSClient, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  private val logger = Logger(this.getClass)

  def gateWay() = Action.async {
    //    implicit val myec: ExecutionContext = ec

    val url = "https://backlog.com/OAuth2AccessRequest.action"
    val request: WSRequest = ws.url(url)

    val complexRequest: WSRequest =
      request
        .addHttpHeaders("Accept" -> "application/json")
        .addHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded ")
        .addQueryStringParameters("search" -> "play")
        .withRequestTimeout(10000.millis)

    val params = Map(
      "grant_type" -> "authorization_code",
      "code" -> "https://nulab-exam.backlog.jp/dashboard",
      "redirect_uri" -> "https://nulab-exam.backlog.jp/dashboard",
      "client_id" -> "FJJOlgk8l086zC7McsgOIixuGSJVKW7A",
      "client_secret" -> "0IxgOHg6Wo2iTUqEye6o1Xpvnq1Z47h2Mpb83biIhwxG9G7TPyusYFM5HxPD9PWm")
    val futureResponse: Future[WSResponse] = complexRequest.post(params)

    val futureResult = futureResponse.map {
      response => response.body
    }.recover {
      case exception: Exception => println(exception.getMessage)
        "ErroR"
    }

    futureResult.map {
      res => Ok(res)
    }

  }


  def getSpace() =  {
    val url = "https://nulab-exam.backlog.jp/api/v2/space"
    val request: WSRequest = ws.url(url)

    val complexRequest: WSRequest =
      request
        .addQueryStringParameters("apiKey" -> "nYXneieTzwJotlwhAuqWoIBmkp7wLnH7nWh2w74cKIj0WaqDeo5Ox8Zu7JLTR00k")
        .withRequestTimeout(10000.millis)

    val futureResponse: Future[WSResponse] = complexRequest.get()
    futureResponse.map {
      response =>
        logger.info(response.body)
        Ok(response.body)
    }.recover {
      case exception: Exception => println(exception.getMessage)
        NotFound("ErroR")
    }
  }



}

