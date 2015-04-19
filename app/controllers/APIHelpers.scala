package controllers

import scala.concurrent.Future

import play.api.Logger
import play.api.mvc.{Action, Request, ActionBuilder, Result, BodyParsers}



case class Logging[A](action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    Logger.info("Got Request: "+request) //FIXME get rid of this and use a filter instead on the global object https://www.playframework.com/documentation/2.3.x/ScalaLogging
    action(request)
  }

  lazy val parser = action.parser
}

object ApiAction extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
    block(request)
  }

  override def composeAction[A](action: Action[A]) = Logging(action)
}
