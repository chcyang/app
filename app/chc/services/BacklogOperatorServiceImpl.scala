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
class BacklogOperatorServiceImpl @Inject()(val gateWayConfig: BacklogGateWayConfig,
                                           val webService: WService,
                                           mapper: ObjectMapper)
                                          (implicit ec: ExecutionContext)
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
    webService.getResponseWithHeaders(endpoint,
      params = params,
      timeout = gateWayConfig.serviceTimeout)(backlogResponseTransform)
  }


  /**
   * find all work hours of a certain user
   * and analysis work hours of every day
   *
   * @param assigneeId the use id you want gather
   * @return
   */
  def getAllIssueHours(assigneeId: String): Future[String] = {

    val endpoint = gateWayConfig.baseUrl + gateWayConfig.getAllIssueApiPath
    val paramsSpec = params ++ Params("assigneeId[]" -> assigneeId)
    //"assigneeId" -> "373653"
    webService.getResponseWithHeaders(endpoint,
      params = paramsSpec,
      timeout = gateWayConfig.serviceTimeout)(backlogResponseTransform).map {
      response =>
        mapper.registerModule(DefaultScalaModule)
        val issues = mapper.readValue(response, classOf[List[Map[String, Object]]])

        val issueMaps = issues.flatMap {
          map =>
            map.get("id").map {
              id =>
                val actualHours = map.get("actualHours")
                val estimatedHours = map.get("estimatedHours")
                val startDate = map.get("startDate")
                val dueDate = map.get("dueDate")

                // only the task have complete can be collect
                // and all the collect data should not be null
                if (startDate.contains(null) || dueDate.contains(null)
                  || actualHours.contains(null) || estimatedHours.contains(null)) None
                else {
                  logger.debug(s"startDate:$startDate dueDate:$dueDate actualHours:$actualHours  estimatedHours:$estimatedHours")
                  val startDateObj = LocalDateTime.parse(startDate.fold("")(_.toString), DateTimeFormatter.ISO_ZONED_DATE_TIME)
                  val dueDateObj = LocalDateTime.parse(dueDate.fold("")(_.toString), DateTimeFormatter.ISO_ZONED_DATE_TIME)
                  val start = Timestamp.valueOf(startDateObj).getTime
                  val due = Timestamp.valueOf(dueDateObj).getTime
                  val between = (due - start) / (24 * 60 * 60 * 1000)
                  val actualHoursData = actualHours.fold(0.0)(_.toString.toFloat)
                  val estimatedHoursData = estimatedHours.fold(0.0)(_.toString.toFloat)
                  val averageActualHours = actualHoursData / between
                  val averageEstimateHours = estimatedHoursData / between
                  val hoursSeq = for (i <- 0.toLong until between) yield startDateObj.plusDays(i).toString -> (averageActualHours, averageEstimateHours)
                  val hoursMap = hoursSeq.map { value => value._1 -> value._2 }.toMap
                  Map(id -> hoursMap)
                }
            }
        }

        // find parent issue id list
        val parentIssueIdList = issues.map(v => v.get("parentIssueId"))
          .flatMap(_.flatMap(Option(_)))
          .toSet // get duplicate id list

        // filter parent issue for avoid add hours twice
        val actualIssueList = issueMaps
          .flatten
          .filterNot {
            map => parentIssueIdList.contains(map._1)
          }

        // collect work hours by date
        val result = actualIssueList
          .flatMap(_._2) // get work hours list
          .groupBy(_._1) // group work hours of same day
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
   * */
  def getAllIssues(): Future[String] = {
    val endpoint = gateWayConfig.baseUrl + gateWayConfig.spaceApiPath
    webService.getResponseWithHeaders(endpoint, timeout = gateWayConfig.serviceTimeout, params = params)(backlogResponseTransform)
  }


  /**
   * get file list of the special project and path
   *
   * @param projectId project you want check
   * @param filepath  full path of directory you want check
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
          mapper.registerModule(DefaultScalaModule)
          val files = mapper.readValue(response, classOf[List[Map[String, Object]]])
          files.map {
            file =>
              val fileType = file.get("type")
              val fileId = file.get("id")
              val fileName = file.get("name")
              val uploadUser = file.get("CreatedUser").map {
                user =>
                  mapper.readValue(user.toString, classOf[Map[String, String]]).get("name")
              }

              // build a FileUploadModel for response
              FileUploadModel(
                data = "",
                fileType = fileType.fold("")(_.toString),
                fileId = fileId.fold(0)(_.toString.toInt),
                uploadUser = uploadUser.fold("")(_.toString),
                fileName = fileName.fold("")(_.toString),
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
   * @param response request body
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
