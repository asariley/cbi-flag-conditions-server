package controllers

import org.joda.time.format.DateTimeFormat
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, Controller}
import play.api.Play.{current => implicitRunningApp}
import scala.slick.driver.PostgresDriver.simple.{queryToAppliedQueryInvoker}

import models.{FlagConditionHistory, FlagCondition, WindCondition, WindDirection, FlagColor, FlagConditionJsonWrites, QueryHelpers}

import FlagConditionJsonWrites._

object FlagConditions extends Controller {

    def current =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>
            DB.withSession { implicit rs =>
                FlagCondition.table.sortBy(_.recordedDateTime.desc).firstOption.map(fc => Ok(Json.toJson(fc))).getOrElse(NoContent)
            }
        }

    def history =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>
            DB.withSession { implicit rs =>
                Ok(Json.toJson(FlagCondition.todaysConditions))
            }
        }

}
