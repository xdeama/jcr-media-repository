package de.dmalo.common.formatter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class CalendarFormatterTest {

    @Test
    fun testToISO8601() {
        val fixedTimeZone = TimeZone.getTimeZone("Etc/GMT+2")
        val calendar = Calendar.getInstance(fixedTimeZone).apply {
            set(2023, Calendar.FEBRUARY, 15, 20, 0, 0)
        }

        val result = CalendarFormatter.toISO8601(calendar)

        val expectedResult = "2023-02-15T22:00:00Z"
        assertEquals(expectedResult, result)
    }

    @Test
    fun testToISO8601_withGermanTimeWithoutDST() {
        val berlinTimeZone = TimeZone.getTimeZone("Europe/Berlin")

        val calendarNonDST = Calendar.getInstance(berlinTimeZone).apply {
            set(2023, Calendar.JANUARY, 15, 18, 0, 0)
        }

        val resultNonDST = CalendarFormatter.toISO8601(calendarNonDST)

        val expectedResultNonDST = "2023-01-15T17:00:00Z"
        assertEquals(expectedResultNonDST, resultNonDST)
    }

    @Test
    fun testToISO8601_withGermanTimeWithDST() {
        val berlinTimeZone = TimeZone.getTimeZone("Europe/Berlin")

        val calendarDST = Calendar.getInstance(berlinTimeZone).apply {
            set(2023, Calendar.JULY, 15, 18, 0, 0)
        }

        val resultDST = CalendarFormatter.toISO8601(calendarDST)

        val expectedResultDST = "2023-07-15T16:00:00Z"
        assertEquals(expectedResultDST, resultDST)
    }

}
