package models

import org.joda.time.DateTime
import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified
import scala.slick.jdbc.{GetResult, StaticQuery, JdbcBackend, SQLInterpolationResult}

object QueryHelpers {
    val conditions: TableQuery[FlagConditionsTable] = TableQuery[FlagConditionsTable]

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
                new DateTime(r.rs.getTimestamp(6)),
                SkyCondition.withName(r.rs.getString(7)),
                r.rs.getDouble(8),
                Some(r.rs.getInt(1))
            )
        })

    def adHocQuery[A](f: java.sql.ResultSet => A, query: SQLInterpolationResult[_]): StaticQuery[Unit, A] = query.as[A](GetResult{ r => f(r.rs) })
}