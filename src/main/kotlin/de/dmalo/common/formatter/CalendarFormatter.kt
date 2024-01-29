package de.dmalo.common.formatter

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object CalendarFormatter {

    private const val ISO_8601_PATTERN: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    private val formatter = DateTimeFormatter.ofPattern(ISO_8601_PATTERN).withZone(ZoneId.of("UTC"))

    @JvmStatic
    fun toISO8601(calendar: Calendar): String {
        val instant = calendar.toInstant()
        val zoneId = calendar.timeZone.toZoneId()
        val zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)

        return formatter.format(zonedDateTime)
    }
}
