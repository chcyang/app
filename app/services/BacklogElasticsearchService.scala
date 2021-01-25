package services

import com.google.inject.{Inject, Singleton}
import gateways.ElasticsearchGateWay
import org.slf4j.{Logger, LoggerFactory}

@Singleton
class BacklogElasticsearchService @Inject()(elasticsearchGateWay: ElasticsearchGateWay) {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def doSearch() = elasticsearchGateWay.doSearch()

  def addIndex()=elasticsearchGateWay.writeIndex()

}
