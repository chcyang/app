package chc.exception

import chc.utils.Message

class AppException(val message: Message) extends RuntimeException(message.messageContent)
