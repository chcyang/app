package services

import com.google.inject.{Inject, Singleton}
import gateways.ElasticsearchGateWay
import play.api.Logger

@Singleton
class BacklogElasticsearchService @Inject()(elasticsearchGateWay: ElasticsearchGateWay) {

  private val logger = Logger(this.getClass)

  def doSearch() = elasticsearchGateWay.doSearch()


}
