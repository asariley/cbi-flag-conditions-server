package models

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified
import scala.slick.jdbc.{GetResult, StaticQuery, JdbcBackend, SQLInterpolationResult}

object QueryHelpers {
    def adHocQuery[A](f: java.sql.ResultSet => A, query: SQLInterpolationResult[_]): StaticQuery[Unit, A] = query.as[A](GetResult{ r => f(r.rs) })
}


