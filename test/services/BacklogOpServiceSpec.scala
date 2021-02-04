package services

import chc.adapter.WService
import chc.config.BacklogGateWayConfig
import chc.gateways.WebService
import chc.models.FileUploadModel
import chc.services.BacklogOperatorServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.ConfigFactory
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient

import scala.concurrent.Await
import scala.concurrent.duration._


class BacklogOpServiceSpec extends PlaySpec with MockitoSugar with GuiceOneServerPerSuite {

  import scala.concurrent.ExecutionContext.Implicits.global

  val backlogConfig = new BacklogGateWayConfig(ConfigFactory.load("test.conf"))
  val mapper = new ObjectMapper()

  "webservice testing" should {
    "allow mocking a service" in {
      //#mock-service
      val FilesBody =
        """[
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
          |            "mailAddress": "test-user@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "test-user"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "created": "2021-01-22T15:42:12Z",
          |        "updatedUser": null,
          |        "updated": "2021-01-22T15:42:12Z"
          |    }
          |]""".stripMargin
      val webService = WebServiceMock(FilesBody)
      val operatorServiceImpl = new BacklogOperatorServiceImpl(backlogConfig, webService, mapper)

      val result: List[FileUploadModel] = Await.result(operatorServiceImpl.getAllFiles("146597", ""), 10.seconds)
      result(0).fileId mustBe 11881668
    }


    "mock service test for downLoadFile" in {
      //#mock-service
      val wsClient = app.injector.instanceOf[WSClient]
      val webService: WService = new WebService(backlogConfig, wsClient)
      val operatorServiceImpl = new BacklogOperatorServiceImpl(backlogConfig, webService, mapper)
      val result: String = Await.result(operatorServiceImpl.downLoadFile("146597", 11881668), 10.seconds)
    }


    "mock service test for getAllIssues" in {
      //#mock-service
      val requestBody =
        """[
          |    {
          |        "id": 18710472,
          |        "projectId": 146597,
          |        "issueKey": "CHEN-9",
          |        "keyId": 9,
          |        "issueType": {
          |            "id": 723311,
          |            "projectId": 146597,
          |            "name": "Task",
          |            "color": "#7ea800",
          |            "displayOrder": 0
          |        },
          |        "summary": "実装関連の改善と追加実装",
          |        "description": "ファイルダウロードAPIなどとの連携\n少しだけテストコードを書く",
          |        "resolution": {
          |            "id": 0,
          |            "name": "対応済み"
          |        },
          |        "priority": {
          |            "id": 3,
          |            "name": "中"
          |        },
          |        "status": {
          |            "id": 4,
          |            "projectId": 146597,
          |            "name": "完了",
          |            "color": "#b0be3c",
          |            "displayOrder": 4000
          |        },
          |        "assignee": {
          |            "id": 373653,
          |            "userId": "*RaTnwT3lk0",
          |            "name": "Rand Chen",
          |            "roleType": 2,
          |            "lang": "ja",
          |            "mailAddress": "test-user@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "test-user"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "category": [],
          |        "versions": [
          |            {
          |                "id": 250235,
          |                "projectId": 146597,
          |                "name": "V1.0",
          |                "description": null,
          |                "startDate": "2021-01-19T00:00:00Z",
          |                "releaseDueDate": "2021-01-31T00:00:00Z",
          |                "archived": false,
          |                "displayOrder": 0
          |            }
          |        ],
          |        "milestone": [
          |            {
          |                "id": 250235,
          |                "projectId": 146597,
          |                "name": "V1.0",
          |                "description": null,
          |                "startDate": "2021-01-19T00:00:00Z",
          |                "releaseDueDate": "2021-01-31T00:00:00Z",
          |                "archived": false,
          |                "displayOrder": 0
          |            }
          |        ],
          |        "startDate": "2021-01-29T00:00:00Z",
          |        "dueDate": "2021-01-30T00:00:00Z",
          |        "estimatedHours": 8,
          |        "actualHours": 12,
          |        "parentIssueId": 18465690,
          |        "createdUser": {
          |            "id": 373653,
          |            "userId": "*RaTnwT3lk0",
          |            "name": "Rand Chen",
          |            "roleType": 2,
          |            "lang": "ja",
          |            "mailAddress": "test-user@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "test-user"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "created": "2021-01-30T10:50:35Z",
          |        "updatedUser": {
          |            "id": 373653,
          |            "userId": "*RaTnwT3lk0",
          |            "name": "Rand Chen",
          |            "roleType": 2,
          |            "lang": "ja",
          |            "mailAddress": "test-user@gmail.com",
          |            "nulabAccount": {
          |                "nulabId": "ABFqozryQ3yuynNrvbHlu41HOKCGR0CbAWfGl0xBkJLJYb9Vk0",
          |                "name": "Rand Chen",
          |                "uniqueId": "test-user"
          |            },
          |            "keyword": "Rand Chen Rand Chen"
          |        },
          |        "updated": "2021-01-30T10:51:10Z",
          |        "customFields": [],
          |        "attachments": [],
          |        "sharedFiles": [],
          |        "stars": []
          |    }
          |]""".stripMargin
      val webService = WebServiceMock(requestBody)
      val operatorServiceImpl = new BacklogOperatorServiceImpl(backlogConfig, webService, mapper)

      val result: String = Await.result(operatorServiceImpl.getAllIssueHours("146597"), 10.seconds)
      val actualJson: String = "{\"2021-01-29T00:00\":[12.0,8.0]}"
      result mustBe actualJson

    }
  }

}

