package models

import scala.Enumeration
import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified
import scala.slick.lifted.ProvenShape

import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import play.api.mvc.BodyParsers.parse
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, JsString, Json, Writes}
import Writes._ //FIXME make imports fully qualified




case class FlagCondition(color: FlagColor.Value, since: DateTime, wind: WindCondition)
case class WindCondition(speed: Double, direction: WindDirection.Value)

//    object FlagCondition extends Table

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
}

object ColumnTypeImplicits {
    implicit val flagColorColumnType = MappedColumnType.base[FlagColor.Value, String](
        { fc => fc.toString },    // map FlagColor.Value to String
        { FlagColor.withName } // map String to FlagColor.Value
    )
    implicit val windDirectionColumnType = MappedColumnType.base[WindDirection.Value, String](
        { wd => wd.toString },    // map FlagColor.Value to String
        { WindDirection.withName } // map String to FlagColor.Value
    )
    implicit val jodaDateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
        { d => new java.sql.Timestamp(d.getMillis) },    // map FlagColor.Value to String
        { ts => new DateTime(ts) } // map String to FlagColor.Value
    )
}

import ColumnTypeImplicits._

class FlagConditionsTable(tag: Tag) extends Table[(Int, DateTime, FlagColor.Value, Double, WindDirection.Value)](tag, "conditions") {
    def conditionId: Column[Int] = column[Int]("condition_id", O.PrimaryKey)
    def recordedDateTime: Column[DateTime] = column[DateTime]("recorded_datetime")
    def currentColor: Column[FlagColor.Value] = column[FlagColor.Value]("flag_color")
    def windSpeed: Column[Double] = column[Double]("wind_speed")
    def windDirection: Column[WindDirection.Value] = column[WindDirection.Value]("wind_direction")

    def * : ProvenShape[(Int, DateTime, FlagColor.Value, Double, WindDirection.Value)] =
        (conditionId, recordedDateTime, currentColor, windSpeed, windDirection)
}

