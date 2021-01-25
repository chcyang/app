package exception

import utils.Message

class AppException(val message: Message) extends RuntimeException(message.messageContent)
