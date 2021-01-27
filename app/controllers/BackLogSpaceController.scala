package controllers

import com.google.inject.{Inject, Singleton}
import exception.AppException
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import services.BacklogOpService
import utils.AppExceptionHandler

import scala.concurrent.ExecutionContext

@Singleton
class BackLogSpaceController @Inject()(val controllerComponents: ControllerComponents, backlogOpService: BacklogOpService)
                                      (implicit ec: ExecutionContext)
  extends BaseController with AppExceptionHandler {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * get user space
   *
   * @return
   */
  def getSpace = Action.async {
    implicit request: Request[AnyContent] =>
      backlogOpService.getSpace().map {
        response =>
          Ok(response)
      }
        .recover {
          case exception: AppException => errorHandle(exception)
          case unknownEx: Exception =>
            logger.error("Get backlog space Error:", unknownEx)
            errorHandle(unknownEx)
        }
  }

  /**
   * * find all work hours of a certain user
   * * and analysis work hours of every day
   *
   * @param assignee user id of who want to search
   * @return
   */
  def getAllIssueHours(assignee: String) = Action.async {
    implicit request: Request[AnyContent] =>
      backlogOpService.getAllIssueHours(assignee).map {
        res =>
          Ok(res)
      }.recover {
        case exception: AppException => errorHandle(exception)
        case unknownEx: Exception =>
          logger.error("Backlog operator exception occur:", unknownEx)
          errorHandle(unknownEx)
      }
  }

}
