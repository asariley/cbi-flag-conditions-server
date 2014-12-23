package controllers

import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import play.api.mvc.BodyParsers.parse
import play.api.libs.functional.syntax._

import play.api.libs.json.{Json}
import play.api.db.slick.DB

import play.api.Play.{current => runningApp}

import scala.slick.driver.PostgresDriver.simple.{queryToAppliedQueryInvoker}

import models.{FlagCondition, WindCondition, WindDirection, FlagColor, FlagConditionJsonWrites, QueryHelpers}
import FlagConditionJsonWrites._

object FlagConditions extends Controller {

    def current =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>



            DB.withSession { implicit rs =>
                QueryHelpers.conditions.sortBy(_.recordedDateTime.desc).firstOption.map(fc => Ok(Json.toJson(fc))).getOrElse(NoContent)
            }

//            Ok(Json.toJson(
//                FlagCondition(
//                    color = FlagColor.RED,
//                    since = new DateTime,
//                    wind = WindCondition(14.35, WindDirection.NORTHEAST)
//                )
//            ))

        }

  def history = TODO

}
