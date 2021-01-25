package controllers

import java.io.ByteArrayInputStream
import java.nio.file.Paths

import com.google.common.io.Files
import com.google.inject.{Inject, Singleton}
import exception.AppException
import play.api.Logger
import play.api.mvc.{BaseController, ControllerComponents}
import services.{BacklogElasticsearchService, BacklogOpService}
import utils.AppExceptionHandler

import scala.concurrent.ExecutionContext
import scala.reflect.io.File

@Singleton
class BackLogFileController @Inject()(val controllerComponents: ControllerComponents,
                                      backlogOpService: BacklogOpService,
                                      backlogElasticsearchService: BacklogElasticsearchService)
                                     (implicit ec: ExecutionContext)
  extends BaseController with AppExceptionHandler {

  private val logger = Logger(this.getClass)

  def upload() = Action(parse.multipartFormData) { request =>
    request.body
      .file("picture")
      .map { picture =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename = Paths.get(picture.filename).getFileName
        val fileSize = picture.fileSize
        val contentType = picture.contentType

        val base64code = javax.xml.bind.DatatypeConverter.printBase64Binary(Files.toByteArray(picture.ref.toFile))
        val imgstr = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64code)

        import java.awt.image.BufferedImage

        import javax.imageio.ImageIO
        val bufferedImage: BufferedImage = ImageIO.read(new ByteArrayInputStream(imgstr))
        val file = File(s"/tmp/picture/$filename")
        file.outputStream().write(imgstr)
        //        picture.ref.copyTo(Paths.get(s"/tmp/picture/$filename"), replace = true)
        Ok("File uploaded")
      }
      .getOrElse {
        Redirect(routes.HomeController.index).flashing("error" -> "Missing file")
      }
  }

  /**
   * search data from Elasticsearch
   *
   * @return
   */
  def doEsSearch() = Action.async {
    backlogElasticsearchService.doSearch().map {
      res =>
        Ok(res)
    }.recover {
      case exception => errorHandle(exception)
    }
  }


  def addIndex() = Action.async {
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
