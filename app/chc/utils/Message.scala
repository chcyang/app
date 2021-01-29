package chc.utils

sealed trait Message {
  def messageContent: String
}

sealed trait ClientErrorMessage extends Message

sealed trait SystemFailureMessage extends Message

final case class BaseClientError(msg: String) extends ClientErrorMessage {
  override def messageContent: String = msg
}

final case class NotFound(resourceName: String) extends ClientErrorMessage {
  override def messageContent: String = s"${resourceName} Not Found!"
}

final case class TimeOut(serviceName: String) extends ClientErrorMessage {
  override def messageContent: String = s"${serviceName} TimeOut Occurred!"
}


final case class BaseSystemFailure(msg: String) extends SystemFailureMessage {
  override def messageContent: String = msg
}

final case class InternalServerError(serviceName: String) extends SystemFailureMessage {
  override def messageContent: String = s"${serviceName} Internal Server Error Occurred!"
}