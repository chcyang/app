package chc.services

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import chc.adapter.{BacklogOperatorService, WService}
import chc.config.BacklogGateWayConfig
import chc.exception.AppException
import chc.gateways.Params
import chc.models.FileUploadModel
import chc.utils.{BaseClientError, BaseSystemFailure}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.inject.{Inject, Singleton}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.Status
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BacklogOperatorServiceImpl @Inject()(gateWayConfig: BacklogGateWayConfig, val webService: WService)(implicit ec: ExecutionContext)
  extends BacklogOperatorService {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  final private val params = Params("apiKey" -> gateWayConfig.backlogApiKey)

  /**
   * get backlof user space
   *
   * @return
   */
  def getSpace(): Future[String] = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    webService.getResponseWithHeaders(endpoint, params = params, timeout = gateWayConfig.serviceTimeout) {
      case response => if (response.status == Status.OK) {
        logger.info(response.body)
        Future.successful(response.body)
      } else {
        Future.failed(new RuntimeException("Get backlog space error"))
      }
    }
  }


  /**
   * find all work hours of a certain user
   * and analysis work hours of every day
   *
   * @param assigneeId
   * @return
   */
  def getAllIssueHours(assigneeId: String): Future[String] = {

    val endpoint = gateWayConfig.baseUrl + gateWayConfig.getAllIssueApiPath
    val paramsSpec = params ++ Params("assigneeId[]" -> assigneeId)
    //"assigneeId" -> "373653"
    webService.getResponseWithHeaders(endpoint, params = paramsSpec, timeout = gateWayConfig.serviceTimeout)(backlogResponseTransform).map {
      response =>
        val mapper = new ObjectMapper
        mapper.registerModule(DefaultScalaModule)
        val issues = mapper.readValue(response, classOf[List[Map[String, Object]]])

        val issueMaps = issues.map {
          map =>
            map.get("id").map {
              id =>
                val actualHours = map.get("actualHours")
                val estimatedHours = map.get("estimatedHours")
                val startDate = map.get("startDate")
                val dueDate = map.get("dueDate")

                if (startDate == Some(null) || dueDate == Some(null)
                  || actualHours == Some(null) || estimatedHours == Some(null)) None
                else {
                  logger.debug(s"startDate:${startDate} dueDate:${dueDate} actualHours:${actualHours}  estimatedHours:${estimatedHours}")
                  val startDateObj = LocalDateTime.parse(startDate.getOrElse("").toString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                  val dueDateObj = LocalDateTime.parse(dueDate.getOrElse("").toString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                  val start = Timestamp.valueOf(startDateObj).getTime
                  val due = Timestamp.valueOf(dueDateObj).getTime
                  val between = (due - start) / (24 * 60 * 60 * 1000)
                  val actualHoursData = actualHours.getOrElse(0).toString.toFloat
                  val estimatedHoursData = estimatedHours.getOrElse(0).toString.toFloat
                  val averageActulHours = actualHoursData / between
                  val averageEstimateHours = estimatedHoursData / between
                  val hoursSeq = for (i <- 0.toLong until between) yield (startDateObj.plusDays(i).toString -> (averageActulHours, averageEstimateHours))
                  val hoursMap = hoursSeq.map { value => (value._1 -> value._2) }.toMap
                  Map(id -> hoursMap)
                }
            }
              .filterNot(_ == None)
        }.filterNot(_ == None)

        // find parent issue id list
        val parentIssueIdList = issues.map(v => v.get("parentIssueId"))
          .map(_.flatMap(Option(_)))
          .filterNot(_ == None).toSet

        // filter parent issue for avoid add hours twice
        val actualIssueList = issueMaps.flatMap {
          _.get
        }.filterNot {
          map => parentIssueIdList.contains(Option(map._1))
        }

        val result = actualIssueList
          .flatMap(_._2)
          .groupBy(_._1) // group the data of same day
          .view.mapValues(seq => seq.map(_._2) // get hours list
          .reduce((a, b) => (a._1 + b._1, a._2 + b._2))) // collect hours of same day(actualHours,estimateHours)
          .toMap

        // to Json
        mapper.writeValueAsString(result)
    }
  }

  /**
   * get list of all issue
   *
   * @return Future[String]
   **/
  def getAllIssues(): Future[String] = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params)(backlogResponseTransform)
  }


  /**
   *
   * @param projectId
   * @param filepath
   * @return
   */
  def getAllFiles(projectId: String, filepath: String): Future[List[FileUploadModel]] = {
//    val projectId = "146597"
//    val filepath = ""
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.getALlFilesApiPath.format(projectId, filepath)
    logger.debug(endpoint)
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params)(backlogResponseTransform)
      .map {
        response =>
          val mapper = new ObjectMapper
          mapper.registerModule(DefaultScalaModule)
          val files = mapper.readValue(response, classOf[List[Map[String, Object]]])
          files.map {
            file =>
              val fileType = file.get("type")
              val fileId = file.get("id")
              val fileName = file.get("name")
              val uploadUser = file.get("CreatedUser").map {
                user =>
                  mapper.readValue(user.toString, classOf[Map[String, String]]).getOrElse("name", "").toString
              }

              FileUploadModel(
                data = "",
                fileType = fileType.getOrElse("").toString,
                fileId = fileId.getOrElse(0).toString.toInt,
                uploadUser = uploadUser.getOrElse(""),
                fileName = fileName.getOrElse("").toString,
                source = Option("")
              )
          }
      }
  }


  def downLoadFile(projectId: String, fileId: Int): Future[String] = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.downLoadFileApiPath.format(projectId, fileId)
    logger.debug(endpoint)
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params)(backlogResponseTransform)
  }

  /**
   * handle backlog request error
   *
   * @param response
   * @return
   */
  private def backlogResponseTransform(response: WSResponse): Future[String] = {
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
        throw new AppException(BaseSystemFailure(s"Backlog request get other status:${response.body}"))
    }
  }

}
