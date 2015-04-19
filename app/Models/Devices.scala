package models

import scala.Enumeration

import play.api.libs.json.{Json, Reads}


case class Device (
    deviceIdentifier: String
)
object Device {
    implicit val deviceReads: Reads[Device] = Json.reads[Device]
}

