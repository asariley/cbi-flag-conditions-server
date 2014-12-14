package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    play.api.Logger.info(System.getProperty("java.class.path"))
    Ok(views.html.index("Your new application is ready."))
  }

}