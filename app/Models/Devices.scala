package models

import scala.Enumeration

import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape
import play.api.libs.json.{Json, Reads}


case class Device (
    deviceIdentifier: String
)
object Device {
    implicit val deviceReads: Reads[Device] = Json.reads[Device]
}

