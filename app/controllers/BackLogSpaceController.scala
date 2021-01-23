package controllers

import com.google.inject.{Inject, Singleton}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import services.BacklogOpService

import scala.concurrent.ExecutionContext

@Singleton
class BackLogSpaceController @Inject()(val controllerComponents: ControllerComponents, backlogOpService: BacklogOpService)
                                      (implicit ec: ExecutionContext) extends BaseController {

  def getSpace() = Action.async {
    implicit request: Request[AnyContent] =>
      backlogOpService.getSpace().map {
        response =>
          Ok(response)
      }.recover {
        case exception: Exception => println(exception.getMessage)
          NotFound("ErroR")
      }
  }


}
