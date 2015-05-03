package utils.actors

import javax.naming.ConfigurationException
import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

import akka.actor.Actor
import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalTime, DateTimeConstants}
import play.api.Configuration
import play.api.db.slick.DB
import play.api.libs.json.{Json, DefaultReads, JsSuccess, JsError}
import play.api.libs.ws.WS
import play.api.Logger
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.driver.PostgresDriver.simple.{queryToAppliedQueryInvoker, queryToUpdateInvoker, queryToInsertInvoker}
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.jdbc.StaticQuery.interpolation

import models.{FlagCondition, WindCondition, FlagColor, WindDirection, SkyCondition, QueryHelpers}
import utils.helpers.{CbiHours, WundergroundApiConversion, TimeHelpers}


/** Object for decoding wunderground api response */
case class WundergroundConditionsResponse(temp_f: Double, wind_degrees: Int, wind_mph: Double, weather: String)
object WundergroundConditionsResponse {
    implicit val wcrReads = Json.reads[WundergroundConditionsResponse]
}
case class WundergroundSunsetResponse(hour: String, minute: String)
object WundergroundSunsetResponse {
    implicit val warReads = Json.reads[WundergroundSunsetResponse]
}

/** Commands that can be issued to WeatherReporter */
case object FullReport
case object FlagReport

class WeatherReporter(configuration: Configuration, onChange: (Option[FlagCondition], Option[FlagCondition]) => Unit) extends Actor {

    def receive = {
        case FullReport if CbiHours.isCbiOpen =>
            compileFullReport.map(reportAndRecordNewCondition) onComplete {
                case Success(_) => Logger.info("Scheduled full report completed successfully")
                case Failure(t) => Logger.error(s"Full report failed: $t")
            }
        case FlagReport if CbiHours.isCbiOpen => requestFlagColor.map(reportAndRecordNewFlag) onComplete {
                case Success(_) => Logger.info("Scheduled flag report completed successfully")
                case Failure(t) => Logger.error(s"Flag report failed: $t")
            }
        case FullReport | FlagReport => () //Don't invest the time if we know we don't care about the data

    }

    private def requestFlagColor: Future[FlagColor.Value] = configuration.getString("cbi.site.flagurl").map { cbiUrl =>
        WS.url(cbiUrl).get().map { response =>
            val flagPattern = """var FLAG_COLOR = "([C|G|Y|R|c|g|y|r])";""".r
            (flagPattern findFirstIn response.body) match {
                case Some(flagPattern("C"|"c")) => FlagColor.CLOSED
                case Some(flagPattern("G"|"g")) => FlagColor.GREEN
                case Some(flagPattern("Y"|"y")) => FlagColor.YELLOW
                case Some(flagPattern("R"|"r")) => FlagColor.RED
                case _ => throw new IllegalStateException("Unable to determine flag color from response body: ${response.body}")
            }
        }
    }.getOrElse(Future.failed[FlagColor.Value](new ConfigurationException("Incomplete configuration for cbi url")))

    /** Call out to external sources (wunderground and CBI to compile current conditions */
    private def compileFullReport: Future[FlagCondition] = {
        val wundergroundInfo: Future[(WindCondition, SkyCondition.Value, Double, LocalTime)] = {
            for {
                base <- configuration.getString("weatherapi.wunderground.api.baseurl")
                key  <- configuration.getString("weatherapi.wunderground.api.key")
                pws  <- configuration.getString("weatherapi.wunderground.api.pws")
            } yield {
                WS.url(s"$base$key/conditions/astronomy/q/pws:$pws.json").get().map {
                    response =>

                    val wunderResponse = for {
                        conditionsResponse <- (response.json \ "current_observation").validate[WundergroundConditionsResponse]
                        sunsetResponse     <- (response.json \ "sun_phase" \ "sunset").validate[WundergroundSunsetResponse]
                    } yield {
                        val windDirection: WindDirection.Value = WundergroundApiConversion.convertWindDegreesToWindDirection(conditionsResponse.wind_degrees) match {
                            case Right(wd) => wd
                            case Left(error) => throw new IllegalStateException(error)
                        }
                        val skyCondition: SkyCondition.Value = WundergroundApiConversion.convertSkyCondition(conditionsResponse.weather) match {
                            case Right(wd) => wd
                            case Left(error) => throw new IllegalStateException(error)
                        }

                        (
                            WindCondition(conditionsResponse.wind_mph, windDirection),
                            skyCondition,
                            conditionsResponse.temp_f,
                            new LocalTime(sunsetResponse.hour.toInt, sunsetResponse.minute.toInt)
                        )
                    }

                    wunderResponse match {
                        case JsSuccess(s, _) => s
                        case JsError(errors) => {
                            errors.foreach {
                                case (path, es) => Logger.warn(s"Json error for path: $path error(s): ${es.toString}")
                            }
                            throw new IllegalStateException(s"could not parse wundergrounds json. Got response: ${response.body}")
                        }
                    }
                }
            }
        }.getOrElse(Future.failed[(WindCondition, SkyCondition.Value, Double, LocalTime)](new ConfigurationException("Incomplete configuration for weather api")))

        requestFlagColor zip wundergroundInfo map {
            case (fc: FlagColor.Value, (wc: WindCondition, sc: SkyCondition.Value, temp: Double, ss: LocalTime)) =>
                FlagCondition (
                    color=fc,
                    since=TimeHelpers.now,
                    wind=wc,
                    sunset=ss,
                    sky=sc,
                    tempF=temp
                )
        }
    }

    /** Go to DB and get latest Flag Condition and compare them. Insert the new flag condtion into the database */
    private def reportAndRecordNewCondition(newCondition: FlagCondition): Unit = {
        val lastCondition: Option[FlagCondition] = DB.withSession { implicit rs =>
            val lc = FlagCondition.table.sortBy(_.recordedDateTime.desc).firstOption
            //QueryHelpers.conditions += newCondition
            sqlu"""
                INSERT INTO condition (recorded_datetime, current_color, wind_speed, wind_direction, sunset, sky_condition, temperature_farenheit)
                VALUES (
                    ${new java.sql.Timestamp(newCondition.since.getMillis)},
                    ${newCondition.color.toString}::flag_color,
                    ${newCondition.wind.speed},
                    ${newCondition.wind.direction.toString}::wind_direction,
                    ${DateTimeFormat.forPattern("HH:mm").print(newCondition.sunset)},
                    ${newCondition.sky.toString}::sky_condition,
                    ${newCondition.tempF}
                )
            """.execute

            lc
        }
        onChange(lastCondition, Some(newCondition))
    }

    /** Get latest flag condition, update it with the new flag color and pass both versions as arguments to the supplied function*/
    private def reportAndRecordNewFlag(newFlagColor: FlagColor.Value): Unit = {
        val lastCondition = DB.withTransaction { implicit rs =>
            val lcOpt = FlagCondition.table.sortBy(_.recordedDateTime.desc).firstOption
            lcOpt.foreach { lc =>
                /*val statement = QueryHelpers.conditions.filter(_.conditionId === lc.id.get).map(t => (t.recordedDateTime, t.currentColor))
                val numRows = statement.update((TimeHelpers.now, newFlagColor))*/
                val statement = sqlu"""
                    UPDATE condition SET recorded_datetime = now(), current_color = ${newFlagColor.toString}::flag_color
                    WHERE condition_id = ${lc.id.get}"""
                val numRows: Int = statement.first
                if (numRows != 1) {
                    Logger.error("Updating flag color changed $numRows but we only expected 1 row to be updated. Statement: ${statement.updateStatement}")
                }
            }
            lcOpt
        }
        onChange(lastCondition, lastCondition.map(_.copy(color=newFlagColor, since=TimeHelpers.now)))
    }



}