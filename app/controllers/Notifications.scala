package controllers

import scala.util.Try

import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, Controller, Result}
import play.api.Play.{current => implicitRunningApp}

import models.{NotificationPref, Device}

object Notifications extends Controller {

    def getPreferences(deviceIdentifier: String) =
        ApiAction(parse.tolerantFormUrlEncoded) { request =>
            DB.withSession { implicit s =>
                NotificationPref.fetchNotificationPref(deviceIdentifier).map(np => Ok(Json.toJson(np))).getOrElse(NoContent)
            }
        }

    def setPreferences(deviceIdentifier: String) =
        ApiAction(parse.tolerantJson) { request =>
            request.body.validate[NotificationPref].map { np =>
                DB.withSession { implicit s =>
                    NotificationPref.updateNotificationPref(np, Device(deviceIdentifier))
                    Ok(Json.toJson(np))
                }
            }.getOrElse(BadRequest)
        }



}
