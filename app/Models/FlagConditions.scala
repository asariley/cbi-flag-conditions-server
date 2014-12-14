package models

import scala.Enumeration

import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import play.api.mvc.BodyParsers.parse
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, JsString, Json, Writes}
import Writes._




case class FlagCondition(color: FlagColor.Value, since: DateTime, wind: WindCondition)
case class WindCondition(speed: Double, direction: WindDirection.Value)

object FlagColor extends Enumeration {
    val UNKNOWN = Value("UNKNOWN")
    val CLOSED = Value("CLOSED")
    val RED    = Value("RED")
    val YELLOW = Value("YELLOW")
    val GREEN  = Value("GREEN")
}

object WindDirection extends Enumeration {
    val NORTH     = Value("NORTH")
    val NORTHEAST = Value("NORTHEAST")
    val EAST      = Value("EAST")
    val SOUTHEAST = Value("SOUTHEAST")
    val SOUTH     = Value("SOUTH")
    val SOUTHWEST = Value("SOUTHWEST")
    val WEST      = Value("WEST")
    val NORTHWEST = Value("NORTHWEST")
}

object FlagConditionJsonWrites {
    implicit val windEnumerationWrites: Writes[WindDirection.Value] = new Writes[WindDirection.Value] {
        def writes(enum: WindDirection.Value): JsValue = JsString(enum.toString)
    }

    implicit val windConditionWrites: Writes[WindCondition] = (
        (JsPath \ "speed").write[Double] and
        (JsPath \ "direction").write[WindDirection.Value]
    )(unlift(WindCondition.unapply))

   implicit val flagEnumerationWrites: Writes[FlagColor.Value] = new Writes[FlagColor.Value] {
        def writes(enum: FlagColor.Value): JsValue = JsString(enum.toString)
    }

    implicit val flagConditionWrites: Writes[FlagCondition] = (
        (JsPath \ "color").write[FlagColor.Value] and
        (JsPath \ "since").write[DateTime] and
        (JsPath \ "wind").write[WindCondition]
    )(unlift(FlagCondition.unapply))

//For enums use slick's MappedColumnType. See http://slick.typesafe.com/doc/2.0.1/userdefined.html "Scalar Types" for an example
//For dates use slick's MappedColumnType. See http://slick.typesafe.com/doc/2.0.1/userdefined.html "Scalar Types" for an example


}

