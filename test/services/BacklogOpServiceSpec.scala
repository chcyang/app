package services

import chc.config.BacklogGateWayConfig
import chc.models.FileUploadModel
import chc.services.BacklogOperatorServiceImpl
import com.typesafe.config.ConfigFactory
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Await
import scala.concurrent.duration._


class BacklogOpServiceSpec extends PlaySpec with MockitoSugar {

  import scala.concurrent.ExecutionContext.Implicits.global

  "webservice testing" should {
    "allow mocking a service" in {
      //#mock-service
      val backlogConfig = new BacklogGateWayConfig(ConfigFactory.load("test.conf"))
      val FilesBody =
        """[
          |    {
          |        "id": 11881669,
          |        "type": "file",
          |        "dir": "/",
          |        "name": "海外居住　扶養親族に関して.pdf",
          |        "size": 648623,
          |        "createdUser": {
          |            "id": 373653,
          |            "userId": "*RaTnwT3lk0",
          |            "name": "Rand Chen",
          |            "roleType": 2,
          |            "lang": "ja",
          |            "mailAddress": "chcyang202@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "chcyang202"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "created": "2021-01-22T15:42:42Z",
          |        "updatedUser": null,
          |        "updated": "2021-01-22T15:42:42Z"
          |    },
          |    {
          |        "id": 11881668,
          |        "type": "file",
          |        "dir": "/",
          |        "name": "3045aFY.xlsx",
          |        "size": 15099,
          |        "createdUser": {
          |            "id": 373653,
          |            "userId": "*RaTnwT3lk0",
          |            "name": "Rand Chen",
          |            "roleType": 2,
          |            "lang": "ja",
          |            "mailAddress": "chcyang202@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "chcyang202"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "created": "2021-01-22T15:42:12Z",
          |        "updatedUser": null,
          |        "updated": "2021-01-22T15:42:12Z"
          |    }
          |]""".stripMargin
      val webService2 = WebServiceMock(FilesBody)
      val operatorServiceImpl = new BacklogOperatorServiceImpl(backlogConfig, webService2)

      val result: List[FileUploadModel] = Await.result(operatorServiceImpl.getAllFiles(), 10.seconds)
      result(0).fileId mustBe 11881669
      result(1).fileId mustBe 11881668

    }
  }


  //TODO

}

