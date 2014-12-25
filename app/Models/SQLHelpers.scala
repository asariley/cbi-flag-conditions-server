package models

import org.joda.time.DateTime
import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified
import scala.slick.jdbc.GetResult

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
                Some(r.rs.getInt(1))
            )
        })
}