package models

import scala.Enumeration

import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import org.joda.time.{DateTime, LocalTime, LocalDate}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.{JsPath, JsValue, JsString, Json, Writes}
import play.api.Logger
import play.api.mvc.BodyParsers.parse
import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified
import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.lifted.ProvenShape
import scala.slick.jdbc.GetResult

import Writes.DoubleWrites

/** Convenience 'Get stuff' operations */
object FlagCondition {
    val table: TableQuery[FlagConditionsTable] = TableQuery[FlagConditionsTable]

    /* Used for converting static query results to case classes */
    implicit val getFlagResult: GetResult[FlagCondition] =
        GetResult({r =>
            FlagCondition(
                FlagColor.withName(r.rs.getString(3)),
                new DateTime(r.rs.getTimestamp(2)),
                WindCondition(
                    r.rs.getDouble(4),
                    WindDirection.withName(r.rs.getString(5))
                ),
                DateTimeFormat.forPattern("HH:mm").parseLocalTime(r.rs.getString(6)),
                SkyCondition.withName(r.rs.getString(7)),
                r.rs.getDouble(8),
                Some(r.rs.getInt(1))
            )
        })

    def todaysConditions(implicit session: Session): FlagConditionHistory = {
        //Slick doesn't really support dates in filter statements so we use a non-typesafe query
        val today: String = DateTimeFormat.forPattern("yyyy-MM-dd").print(LocalDate.now())
        FlagConditionHistory(sql"SELECT * FROM condition WHERE recorded_datetime > '#$today'".as[FlagCondition].list)
    }
}

object FlagColor extends Enumeration {
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

object SkyCondition extends Enumeration {
    val SUNNY  = Value("SUN")
    val CLOUDY = Value("OVERCAST")
    val RAIN   = Value("RAIN")
    val TSTORM = Value("THUNDERSTORM")
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
        def writes(date: DateTime): JsValue = JsString(ISODateTimeFormat.dateTimeNoMillis.print(date))
    }

    implicit val jodaLocalTimeWrites: Writes[LocalTime] = new Writes[LocalTime] {
        def writes(date: LocalTime): JsValue = JsString(DateTimeFormat.forPattern("HH:mm").print(date))
    }

    implicit val skyConditionEnumerationWrites: Writes[SkyCondition.Value] = new Writes[SkyCondition.Value] {
        def writes(enum: SkyCondition.Value): JsValue = JsString(enum.toString)
    }

    implicit val flagConditionWrites: Writes[FlagCondition] = (
        (JsPath \ "color").write[FlagColor.Value] and
        (JsPath \ "since").write[DateTime] and
        (JsPath \ "wind").write[WindCondition] and
        (JsPath \ "sunset").write[LocalTime] and
        (JsPath \ "sky").write[SkyCondition.Value] and
        (JsPath \ "tempF").write[Double]
    )(unlift({ fc: FlagCondition => Some(fc.color, fc.since, fc.wind, fc.sunset, fc.sky, fc.tempF) }))
}

//We do this manually to not expose the id
case class FlagCondition (
    color: FlagColor.Value,
    since: DateTime,
    wind: WindCondition,
    sunset: LocalTime,
    sky: SkyCondition.Value,
    tempF: Double,
    id: Option[Int] = None
)

case class FlagConditionHistory(conditions: List[FlagCondition])
object FlagConditionHistory {
    import FlagConditionJsonWrites.flagConditionWrites
    implicit val flagConditionHistWrites = Json.writes[FlagConditionHistory]
}

object FlagColumnTypeImplicits {
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
    implicit val jodaLocalTimeColumnType = MappedColumnType.base[LocalTime, String](
        { lt => DateTimeFormat.forPattern("HH:mm").print(lt) },
        { s => DateTimeFormat.forPattern("HH:mm").parseLocalTime(s) }
    )

    implicit val skyConditionColumnType = MappedColumnType.base[SkyCondition.Value, String](
        { sc => sc.toString },
        { SkyCondition.withName }
    )
}

import FlagColumnTypeImplicits._

class FlagConditionsTable(tag: Tag) extends Table[FlagCondition](tag, "condition") {
    def conditionId: Column[Int] = column[Int]("condition_id", O.PrimaryKey, O.AutoInc)
    def recordedDateTime: Column[DateTime] = column[DateTime]("recorded_datetime", O.NotNull)
    def currentColor: Column[FlagColor.Value] = column[FlagColor.Value]("current_color", O.NotNull)
    def windSpeed: Column[Double] = column[Double]("wind_speed", O.NotNull)
    def windDirection: Column[WindDirection.Value] = column[WindDirection.Value]("wind_direction", O.NotNull)
    def sunset: Column[LocalTime] = column[LocalTime]("sunset", O.NotNull)
    def skyCondition: Column[SkyCondition.Value] = column[SkyCondition.Value]("sky_condition", O.NotNull)
    def temperatureFarenheit: Column[Double] = column[Double]("temperature_farenheit", O.NotNull)

    def * : ProvenShape[FlagCondition] = {
        (recordedDateTime, currentColor, windSpeed, windDirection, sunset, skyCondition, temperatureFarenheit, conditionId.?) <>
        (
            { tup: (DateTime, FlagColor.Value, Double, WindDirection.Value, LocalTime, SkyCondition.Value, Double, Option[Int]) =>
                FlagCondition(
                    color = tup._2,
                    since = tup._1,
                    wind = WindCondition(tup._3, tup._4),
                    sunset = tup._5,
                    sky = tup._6,
                    tempF = tup._7,
                    id = tup._8
                )
            },
            { fc: FlagCondition  =>
                Some((
                    fc.since,
                    fc.color,
                    fc.wind.speed,
                    fc.wind.direction,
                    fc.sunset,
                    fc.sky,
                    fc.tempF,
                    fc.id
                ))
            }
        )
    }
}

