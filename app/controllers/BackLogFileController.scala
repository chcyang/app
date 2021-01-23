package controllers

import java.io.ByteArrayInputStream
import java.nio.file.Paths

import com.google.common.io.Files
import com.google.inject.{Inject, Singleton}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import services.BacklogOpService

import scala.concurrent.ExecutionContext
import scala.reflect.io.File

@Singleton
class BackLogFileController @Inject()(val controllerComponents: ControllerComponents, backlogOpService: BacklogOpService)
                                     (implicit ec: ExecutionContext) extends BaseController {

  def getSpace() = Action.async {
    implicit request: Request[AnyContent] =>
      backlogOpService.getSpace().map {
        response =>
          Ok(response)
      }.recover {
        case exception: Exception => println(exception.getMessage)
          NotFound("ErroR")
      }
  }

  def upload() = Action(parse.multipartFormData) { request =>
    request.body
      .file("picture")
      .map { picture =>
        // only get the last part of the filename
        // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
        val filename = Paths.get(picture.filename).getFileName
        val fileSize = picture.fileSize
        val contentType = picture.contentType

        //        val fileString = new String(Files.toByteArray(picture.ref.toFile))
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

}