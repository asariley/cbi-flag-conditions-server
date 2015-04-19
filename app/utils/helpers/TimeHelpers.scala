package utils.helpers

import org.joda.time.{DateTime, DateTimeConstants}

object CbiHours {
    import TimeHelpers.now

    /** Hours for 2015 for CBI. Ultimately we would like to read this information from an external source (DB, cfg file...) */
    def isCbiOpen: Boolean = now.getDayOfWeek match {
        case DateTimeConstants.SUNDAY | DateTimeConstants.SATURDAY =>
            isTimeOfDayInPast(9, 0) && isTimeOfDayInFuture(21, 0)
        case _ =>
            if (isDateInPast(2015, 6, 15) && isDateInFuture(2015, 8, 22)) {
                isTimeOfDayInPast(15, 0) && isTimeOfDayInFuture(21, 0) //3PM open in the summer
            } else if (isDateInPast(2015, 4, 1) && isDateInFuture(2015, 11, 1)) {
                isTimeOfDayInPast(13, 0) && isTimeOfDayInFuture(21, 0) //1PM open otherwise
            } else {
                false
            }
    }

    //If the date is today then it counts as in the past
    private def isDateInPast(year: Int, month: Int, day: Int): Boolean =
        now.isAfter(now.withDate(year, month, day).withTimeAtStartOfDay())

    private def isDateInFuture(year: Int, month: Int, day: Int): Boolean =
        now.isBefore(now.withDate(year, month, day).withTimeAtStartOfDay())

    private def isTimeOfDayInPast(hour: Int, minute: Int): Boolean =
        now.isAfter(now.withTimeAtStartOfDay().withHourOfDay(hour).withMinuteOfHour(minute))

    private def isTimeOfDayInFuture(hour: Int, minute: Int): Boolean =
        now.isBefore(now.withTimeAtStartOfDay().withHourOfDay(hour).withMinuteOfHour(minute))

}

object TimeHelpers {
    def now: DateTime = DateTime.now() //FIXME make sure this is Eastern time
}