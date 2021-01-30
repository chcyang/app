package chc.utils

import chc.exception.AppException
import play.api.libs.ws.DefaultBodyWritables
import play.api.mvc.BaseController

trait AppExceptionHandler extends BaseController with DefaultBodyWritables {

  def errorHandle(exception: Throwable) = {

    exception match {
      case appEx: AppException =>
        appEx.message match {
          case message: ClientErrorMessage => handleClientError(message)
          case message: SystemFailureMessage => handlerSystemFailure(message)
        }
      case other: Exception => InternalServerError(other.getMessage)
    }
  }

  def handleClientError(message: ClientErrorMessage) = {
    message match {
      case msg: NotFound => NotFound(msg.messageContent)
      case msg: TimeOut => RequestTimeout(msg.messageContent)
      case msg => BadRequest(msg.messageContent)
    }

  }

  def handlerSystemFailure(message: SystemFailureMessage) = {
    message match {
      case msg: InternalServerError => InternalServerError(msg.messageContent)
      case msg => InternalServerError(msg.messageContent)
    }
  }
}
