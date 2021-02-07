package services


//import org.scalatestplus.play.PlaySpec
//import org.scalatestplus.play.guice.GuiceOneServerPerSuite
//import play.api.Application
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.libs.ws.WSClient
//import play.api.mvc.DefaultActionBuilder

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.Results._

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticsearchGateWayServiceSpec extends PlaySpec with GuiceOneServerPerSuite {
  // Override app if you need an Application with other than
  // default parameters.
  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .appRoutes(app => {
        case ("GET", "/") => app.injector.instanceOf(classOf[DefaultActionBuilder]) {
          Ok("ok")
        }
      })
      .build()
  }

  /**
   * just a sample of test code
   */
  "test server logic" in {
    val wsClient = app.injector.instanceOf[WSClient]
    val myPublicAddress = s"localhost:$port"
    val testPaymentGatewayURL = s"http://$myPublicAddress"
    // The test payment gateway requires a callback to this server before it returns a result...
    val callbackURL = s"http://$myPublicAddress/callback"
    // await is from play.api.test.FutureAwaits
    val response =
      Await.result(wsClient.url(testPaymentGatewayURL).addQueryStringParameters("callbackURL" -> callbackURL).get(), 10.seconds)

    response.status mustBe 200
  }

  //TODO implement the test code when there have a complicate logic in ElasticsearchGateWayService
}
