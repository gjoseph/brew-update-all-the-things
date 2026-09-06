package net.incongru.brewt

import io.kotest.core.spec.style.FunSpec
import net.incongru.brewt.cli.ScheduleCmd

private val cmd = ScheduleCmd(null)

class ScheduleTest : FunSpec({
    context("schedule-to-plist") {
        test("daily, hour and minute - ignores seconds") {
            cmd.parseDaily("1:23:45").toDictXML() shouldBePlistLike """
                |<dict>
                        |<key>Hour</key>
                        |<integer>1</integer>
                        |<key>Minute</key>
                        |<integer>23</integer>
                        |</dict>""".trimMargin()
        }

        test("daily, defaults to 07:00") {
            cmd.parseDaily("").toDictXML() shouldBePlistLike """
                |<dict>
                        |<key>Hour</key>
                        |<integer>7</integer>
                        |<key>Minute</key>
                        |<integer>0</integer>
                        |</dict>""".trimMargin()
        }

        test("weekly day and time") {
            cmd.parseWeekly("Tuesday @ 15:23").toDictXML() shouldBePlistLike """
                |<dict>
                        |<key>Weekday</key>
                        |<integer>2</integer>
                        |<key>Hour</key>
                        |<integer>15</integer>
                        |<key>Minute</key>
                        |<integer>23</integer>
                        |</dict>""".trimMargin()
        }

        test("weekly defaults to 07:00:00") {
            cmd.parseWeekly("Wed").toDictXML() shouldBePlistLike """
                |<dict>
                        |<key>Weekday</key>
                        |<integer>3</integer>
                        |<key>Hour</key>
                        |<integer>7</integer>
                        |<key>Minute</key>
                        |<integer>0</integer>
                        |</dict>""".trimMargin()
        }

        test("weekly defaults to monday @ 07:00:00") {
            cmd.parseWeekly(null).toDictXML() shouldBePlistLike """
                |<dict>
                        |<key>Weekday</key>
                        |<integer>1</integer>
                        |<key>Hour</key>
                        |<integer>7</integer>
                        |<key>Minute</key>
                        |<integer>0</integer>
                        |</dict>""".trimMargin()
        }
    }
})