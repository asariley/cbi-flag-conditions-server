package models

import scala.Enumeration
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.ProvenShape

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.{Action, Controller}
import play.api.mvc.BodyParsers.parse
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, JsString, Json, Writes}
import scala.slick.jdbc.GetResult
import Writes._ //FIXME make imports fully qualified


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

case class WindCondition(speed: Double, direction: WindDirection.Value)
object WindCondition {
    implicit val windEnumerationWrites: Writes[WindDirection.Value] = new Writes[WindDirection.Value] {
        def writes(enum: WindDirection.Value): JsValue = JsString(enum.toString)
    }

    implicit val windConditionWrites = Json.writes[WindCondition]
}

object FlagConditionJsonWrites {
   implicit val flagEnumerationWrites: Writes[FlagColor.Value] = new Writes[FlagColor.Value] {
        def writes(enum: FlagColor.Value): JsValue = JsString(enum.toString)
    }

    implicit val jodaDateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
        def writes(date: DateTime): JsValue = JsString(ISODateTimeFormat.basicDateTimeNoMillis.print(date))
    }

    implicit val flagConditionWrites: Writes[FlagCondition] = (
        (JsPath \ "color").write[FlagColor.Value] and
        (JsPath \ "since").write[DateTime] and
        (JsPath \ "wind").write[WindCondition]
    )(unlift({ fc: FlagCondition => Some(fc.color, fc.since, fc.wind) }))
}

//We do this manually to not expose the id
case class FlagCondition(color: FlagColor.Value, since: DateTime, wind: WindCondition, id: Option[Int] = None)

case class FlagConditionHistory(conditions: List[FlagCondition])
object FlagConditionHistory {
    import FlagConditionJsonWrites.flagConditionWrites
    implicit val flagConditionHistWrites = Json.writes[FlagConditionHistory]
}

object ColumnTypeImplicits {
    implicit val flagColorColumnType = MappedColumnType.base[FlagColor.Value, String](
        { fc => fc.toString },
        { FlagColor.withName }
    )
    implicit val windDirectionColumnType = MappedColumnType.base[WindDirection.Value, String](
        { wd => wd.toString },
        { WindDirection.withName }
    )
    implicit val jodaDateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
        { d => new java.sql.Timestamp(d.getMillis) },
        { ts => new DateTime(ts) }
    )

    implicit val getFlagResult =
        GetResult({r =>
            FlagCondition(
                FlagColor.withName(r.rs.getString(3)),
                new DateTime(r.rs.getTimestamp(2)),
                WindCondition(
                    r.rs.getDouble(4),
                    WindDirection.withName(r.rs.getString(5))
                ),
                Some(r.rs.getInt(1))
            )
        })
}

import ColumnTypeImplicits._

class FlagConditionsTable(tag: Tag) extends Table[FlagCondition](tag, "conditions") {
    def conditionId: Column[Int] = column[Int]("condition_id", O.PrimaryKey, O.AutoInc)
    def recordedDateTime: Column[DateTime] = column[DateTime]("recorded_datetime")
    def currentColor: Column[FlagColor.Value] = column[FlagColor.Value]("current_color")
    def windSpeed: Column[Double] = column[Double]("wind_speed")
    def windDirection: Column[WindDirection.Value] = column[WindDirection.Value]("wind_direction")

    def * : ProvenShape[FlagCondition] = {
        (recordedDateTime, currentColor, windSpeed, windDirection, conditionId.?) <>
        (
            { tup: (DateTime, FlagColor.Value, Double, WindDirection.Value, Option[Int]) =>
                FlagCondition(tup._2, tup._1, WindCondition(tup._3, tup._4), tup._5) },
            { fc: FlagCondition  => Some((fc.since, fc.color, fc.wind.speed, fc.wind.direction, fc.id)) }
        )
    }
}

