package controllers

import scala.concurrent.Future

import play.api.Logger
import play.api.mvc.{Action, Request, ActionBuilder, Result, BodyParsers}



case class Logging[A](action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    Logger.info("Got Request: "+request)
    action(request)
  }

  lazy val parser = action.parser
}

/* Let's do this once I have something working that I can revert to if it fails
case class Interchange[A](action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    action(request)
  }

  lazy val parser = BodyParsers.parse.json
}*/

object ApiAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    block(request)
  }

  override def composeAction[A](action: Action[A]) = Logging(action)
}
