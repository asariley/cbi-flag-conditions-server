package models

import scala.Enumeration

import play.api.libs.json._
import play.api.libs.json.{Json, Format}


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

