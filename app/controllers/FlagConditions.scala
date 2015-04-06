package controllers

import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, Controller}
import play.api.Play.{current => implicitRunningApp}
import scala.slick.driver.PostgresDriver.simple.{queryToAppliedQueryInvoker}
import scala.slick.jdbc.StaticQuery.interpolation

import models.{FlagConditionHistory, FlagCondition, WindCondition, WindDirection, FlagColor, FlagConditionJsonWrites, QueryHelpers}

import FlagConditionJsonWrites._
import QueryHelpers.getFlagResult

object FlagConditions extends Controller {

    def current =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>
            DB.withSession { implicit rs =>
                QueryHelpers.conditions.sortBy(_.recordedDateTime.desc).firstOption.map(fc => Ok(Json.toJson(fc))).getOrElse(NoContent)
            }
        }

    def history =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>
            DB.withSession { implicit rs =>
                //Slick doesn't really support dates in filter statements so we use a non-typesafe query
                val today: String = DateTimeFormat.forPattern("yyyy-MM-dd").print(LocalDate.now())
                Ok(Json.toJson(FlagConditionHistory(sql"SELECT * FROM condition WHERE recorded_datetime > '#$today'".as[FlagCondition].list)))
            }
        }

}
