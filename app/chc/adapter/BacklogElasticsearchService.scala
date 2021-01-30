package chc.adapter

import chc.services.BacklogElasticsearchServiceImpl
import com.google.inject.ImplementedBy
import play.api.routing.sird.QueryString

import scala.concurrent.Future

@ImplementedBy(classOf[BacklogElasticsearchServiceImpl])
trait BacklogElasticsearchService {
  def getAll(): Future[String]

  def doSearch(queryString: QueryString): Future[String]

  def addIndex(): Future[String]

  def addIndex(data: String,
               fileType: String,
               fileId: Int,
               uploadUser: String,
               fileName: String,
               source: String): Future[String]

}
