package chc.adapter

import chc.models.FileUploadModel
import chc.services.BacklogOperatorServiceImpl
import com.google.inject.ImplementedBy

import scala.concurrent.Future

@ImplementedBy(classOf[BacklogOperatorServiceImpl])
trait BacklogOperatorService {
  def getAllFiles(projectId: String, filepath: String): Future[List[FileUploadModel]]

  def downLoadFile(projectId: String, fileId: Int): Future[String]

  def getSpace(): Future[String]

  def getAllIssueHours(assigneeId: String): Future[String]

  def getAllIssues(): Future[String]
}
