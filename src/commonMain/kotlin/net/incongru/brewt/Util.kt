package net.incongru.brewt

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

fun parseDay(s: String): DayOfWeek {
    // val match = Regex("^(?<day>[A-Za-z]{3,9})?@(?<time>\\d{1,2}(?::\\d{1,2}]))$").matchEntire(s)
    val dayStr = s.trim()
        .also { println("day : " + it) }
        .uppercase().substring(0, 3)

    return when (dayStr) {
        "MON" -> DayOfWeek.MONDAY
        "TUE" -> DayOfWeek.TUESDAY
        "WED" -> DayOfWeek.WEDNESDAY
        "THU" -> DayOfWeek.THURSDAY
        "FRI" -> DayOfWeek.FRIDAY
        "SAT" -> DayOfWeek.SATURDAY
        "SUN" -> DayOfWeek.SUNDAY
        else -> throw IllegalArgumentException("Invalid day of week: $dayStr")
    }
}

fun parseTime(s: String): LocalTime {
    // Rather than dick around with multiple custom formats, we'll just regex it
     val match = Regex("^(?<hour>\\d{1,2})(?::(?<minute>\\d{1,2})(?::(?<second>\\d{1,2}))?)?$").matchEntire(s.trim())
        ?: throw IllegalArgumentException("Invalid time format: $s - Expected hh:mm")

    val hour = match.groups["hour"]?.value ?.toInt() ?: -1 // wtf
    val minute = match.groups["minute"]?.value ?.toInt() ?:0
    val second = match.groups["second"]?.value ?.toInt() ?:0
    // TODO issue a warning if seconds != 0
    return LocalTime(hour, minute, 0)
}

val Int.ordinalString: String
    get() {
        if (this < 0) {
            throw IllegalArgumentException("Ordinate value makes no sense for negative numbers. Current value is ${this}")
        }

        // teen exceptions (11, 12, 13)
        if (this % 100 in 11..13) {
            return "${this}th"
        }

        // standard rule based on last digit
        val suffix = when (this % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }

        return "$this$suffix"
    }