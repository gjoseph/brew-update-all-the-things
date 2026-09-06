package net.incongru.brewt.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import net.incongru.brewt.Brewt
import net.incongru.brewt.DailySchedule
import net.incongru.brewt.MonthlySchedule
import net.incongru.brewt.Schedule
import net.incongru.brewt.Scheduler
import net.incongru.brewt.WeeklySchedule
import net.incongru.brewt.parseDay
import net.incongru.brewt.parseTime

/**
--daily <optional time HH:MM, default to 7:00>
--weekly <optional day of week and time, Mon|Tue|Wed|Thu|Fri|Sat|Sun@HH:MM>, default to sun 7:00
--monthly <optional day of month and time, D@HH:MM default to 1 7:00

OR

--freq daily|weekly|monthly
--at <optional, depends on above>
 */
// TODO nullable for testing -- use mocks or an interface or better dependencies instead
class ScheduleCmd(val brewt: Brewt?) : CliktCommand("schedule") {
    // todo: subcommands: add, replace, remove-all

    // these could actually be sub-commands too
    val schedule: Schedule? by mutuallyExclusiveOptions<Schedule>(
        option("--daily").convert { parseDaily(it) },
        option("--weekly").convert { parseWeekly(it) },
        option("--monthly").convert { parseMonthly(it) }
    ).required()

    private fun parseMonthly(arg: String): MonthlySchedule {
        TODO("Not yet implemented")
    }

    fun parseWeekly(arg: String?): WeeklySchedule {
        println("arg = ${arg}")
        if (arg.isNullOrBlank()) {
            return WeeklySchedule(DayOfWeek.MONDAY, LocalTime(7, 0))
        }
        val match = Regex("^(?<day>.*?)(?:@(?<time>.*))?$").matchEntire(arg)
            ?: throw IllegalArgumentException("Invalid time format: $arg - Expected day<@hh:mm>")
        val dayStr = match.groups["day"]?.value ?: throw IllegalStateException()
        val timeStr = match.groups["time"]?.value?: "7"
        println("dayStr = ${dayStr}")
        println("timeStr = ${timeStr}")

        return WeeklySchedule(parseDay(dayStr), parseTime( timeStr))
    }

    fun parseDaily(arg: String?): DailySchedule {
        if (arg.isNullOrBlank()) {
            return DailySchedule(LocalTime(7, 0))
        }
        return DailySchedule(parseTime(arg))
    }


    override fun help(context: Context): String {
        return "Schedule updates"
    }

    override fun run() {
        if (schedule == null) {
            error("no schedule specified")
        }
        val schedule = schedule!! // capture a local copy of the delegate property
        Scheduler(brewt!!).enable(schedule)
        brewt.log.info("Scheduling done for ${schedule}")
        // TODO print current schedule(s)
    }
}