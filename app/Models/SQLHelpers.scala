package models

import scala.slick.driver.PostgresDriver.simple._ //FIXME make imports fully qualified

object QueryHelpers {
    val conditions: TableQuery[FlagConditionsTable] = TableQuery[FlagConditionsTable]
}