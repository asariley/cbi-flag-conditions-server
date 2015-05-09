
import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

import akka.actor.{Cancellable, ActorRef, PoisonPill, Props}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.mvc.Results.{InternalServerError, NotFound, NoContent}
import utils.actors.{WeatherReporter, FlagReport, FullReport}

import utils.pushnotification.PushService

import models.FlagCondition


object Global extends play.api.GlobalSettings {
    final val MINUTES_BETWEEN_FULL_REPORT = 10

    var fullReportJob: Option[Cancellable] = None
    var flagReportJob: Option[Cancellable] = None
    var weatherReporter: Option[ActorRef] = None

    private def debugOnChange(oldFcOpt: Option[FlagCondition], newFcOpt: Option[FlagCondition]): Unit = {
        if (oldFcOpt.map(_.color) != newFcOpt.map(_.color)) {
            Logger.info(s"old: $oldFcOpt new: $newFcOpt")
        }
    }


    override def onStart(app: play.api.Application) {
        import play.api.Play.current

        fullReportJob.foreach(_.cancel())
        flagReportJob.foreach(_.cancel())
        weatherReporter.foreach(_ ! PoisonPill) //FIXME make this GracefulStop?
        val wr = Akka.system.actorOf(Props(new WeatherReporter(app.configuration, debugOnChange)), name = "weatherreporter")
        weatherReporter = Some(wr)
        fullReportJob = Some(Akka.system.scheduler.schedule(5.second, 10.minute, wr, FullReport))
        flagReportJob = Some(Akka.system.scheduler.schedule(30.second, 1.minute, wr, FlagReport))

        //Start Push Service
        PushService.start()
    }

    override def onStop(app: play.api.Application) {
        fullReportJob.foreach(_.cancel())
        flagReportJob.foreach(_.cancel())
        weatherReporter.foreach(_ ! PoisonPill) //FIXME make this GracefulStop?

        //Shutdown PushService
    }

    override def onError(request: RequestHeader, ex: Throwable) = Future.successful(
        InternalServerError("InternalServerError") //FIXME make these api failures
    )

    override def onHandlerNotFound(request: RequestHeader) = Future.successful(
        NotFound("Not Found") //FIXME make these api failures
    )

}