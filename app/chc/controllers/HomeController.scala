package chc.controllers

import javax.inject._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request: Request[AnyContent] =>
    Ok(chc.views.html.index())
  }

  /**
   * show file upload page
   *
   * @return
   */
  def upload = Action { implicit request: Request[AnyContent] =>
    Ok(chc.views.html.upload())
  }


  /**
   * show file search page
   *
   * @return
   */
  def filesearch = Action { implicit request: Request[AnyContent] =>
    Ok(chc.views.html.filesearch())
  }
}
