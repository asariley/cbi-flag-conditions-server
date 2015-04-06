package models

import scala.Enumeration

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.{Json, Format}
import play.api.Logger
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, Controller}
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}


import Writes._ //FIXME make imports fully qualified

case class NotificationPref (
    address: String,
    weekday: Boolean,
    weekend: Boolean,
    daytime: Boolean,
    evening: Boolean,
    redFlag: Boolean,
    yellowFlag: Boolean,
    greenFlag: Boolean,
    closedFlag: Boolean
)
object NotificationPref {
    implicit val notificationPrefFormat: Format[NotificationPref] = Json.format[NotificationPref]
}

