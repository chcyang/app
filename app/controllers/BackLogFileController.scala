package controllers

import java.nio.file.Paths

import com.google.common.io.Files
import com.google.inject.{Inject, Singleton}
import exception.AppException
import models.FileUploadModel
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, _}
import services.{BacklogElasticsearchService, BacklogOpService}
import utils.AppExceptionHandler

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackLogFileController @Inject()(val controllerComponents: ControllerComponents,
                                      backlogOpService: BacklogOpService,
                                      backlogElasticsearchService: BacklogElasticsearchService)
                                     (implicit ec: ExecutionContext)
  extends BaseController with AppExceptionHandler {

  final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * upload a file and create full contents index
   *
   * @return
   */
  def upload = Action(parse.multipartFormData).async { request =>
    request.body
      .file("docfile")
      .map { picture =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename = Paths.get(picture.filename).getFileName
        val contentType = picture.contentType
        val base64code = javax.xml.bind.DatatypeConverter.printBase64Binary(Files.toByteArray(picture.ref.toFile))

        val uploadModel = FileUploadModel(
          data = base64code,
          fileType = contentType.fold("")(identity),
          fileId = 10003,
          uploadUser = "Test-uploadUser",
          fileName = filename.toString,
          source = "Wiki")

        //        val imgstr = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64code)
        //        val file = File(s"/tmp/picture/$filename")
        //        file.outputStream().write(imgstr)
        picture.ref.copyTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
        uploadModel
      }
      .map {
        // write index for file
        model =>
          for {
            addIndexRes <- backlogElasticsearchService.addIndex(
              data = model.data,
              fileType = model.fileType,
              fileId = model.fileId,
              uploadUser = model.uploadUser,
              fileName = model.fileName,
              source = model.source
            )
          } yield Ok(addIndexRes)
      }
      .getOrElse {
        // if file not found,return error
        Future.successful(Redirect(routes.HomeController.index()).flashing("error" -> "Missing file"))
      }
      .recover {
        case exception: AppException => errorHandle(exception)
        case unknownEx: Exception =>
          logger.error("File search exception occur:", unknownEx)
          errorHandle(unknownEx)
      }

  }

  /**
   * getAll docs from one certain elasticSearch index
   *
   * @return
   */
  def getAll = Action.async {
    backlogElasticsearchService.getAll().map {
      res =>
        Ok(res)
    }.recover {
      case exception: AppException => errorHandle(exception)
      case unknownEx: Exception =>
        logger.error("File search  exception occur:", unknownEx)
        errorHandle(unknownEx)
    }
  }

  /**
   * search data from Elasticsearch
   * multi keywords should be split by space(full width or half width)
   *
   * @return json String
   */
  def doEsSearch = Action.async {
    implicit request: Request[AnyContent] =>

      request.body.asFormUrlEncoded.map {
        args =>
          val paramsStr = args("queryString").head
          val searchParams = paramsStr.replace("　", " ")
            .split("\\s+").toSeq

          val queryString = Map("esQueryString" -> searchParams)
          backlogElasticsearchService.doSearch(queryString).map {
            res => Ok(res)
          }.recover {
            case exception: AppException => errorHandle(exception)
            case unknownEx: Exception =>
              logger.error("File search exception occur:", unknownEx)
              errorHandle(unknownEx)
          }
      }.getOrElse(
        Future.successful(Redirect(routes.HomeController.filesearch()))
      )
  }


  /**
   * add a fixed content file index for test
   *
   * @return json string
   */
  def addIndex = Action.async {
    backlogElasticsearchService.addIndex().map {
      res => Ok(res)
    }.recover {
      case exception: AppException => errorHandle(exception)
      case unknownEx: Exception =>
        logger.error("File search add index exception occur:", unknownEx)
        errorHandle(unknownEx)
    }
  }


}
