package chc.adapter

import chc.models.FileUploadModel

import scala.concurrent.Future

trait BacklogOperatorService {
  def getAllFiles(): Future[List[FileUploadModel]]

}
