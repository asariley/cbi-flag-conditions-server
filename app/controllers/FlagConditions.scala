package controllers

import scala.Enumeration

import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import play.api.mvc.BodyParsers.parse
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json}

import models.{FlagCondition, WindCondition, WindDirection, FlagColor, FlagConditionJsonWrites}
import FlagConditionJsonWrites._

object FlagConditions extends Controller {

    def current = ApiAction(parse.tolerantFormUrlEncoded) { request =>
        Ok(Json.toJson(
            FlagCondition(
                color = FlagColor.RED,
                since = new DateTime,
                wind = WindCondition(14.35, WindDirection.NORTHEAST)
            )
        ))
    }

  def history = TODO

}
