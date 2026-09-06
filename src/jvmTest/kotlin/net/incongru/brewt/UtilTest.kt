package net.incongru.brewt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime

class UtilTest : FunSpec({
    context("parseTime") {
        context("valid inputs") {
            withTests(
                Triple("11:12", LocalTime.parse("11:12:00"), "hour:minute"),
                Triple("11", LocalTime.parse("11:00:00"), "hour"),
                Triple("3", LocalTime.parse("03:00:00"), "hour, single digit"),
                Triple("03", LocalTime.parse("03:00:00"), "hour, single digit, 0-padded"),
                Triple("11:3", LocalTime.parse("11:03:00"), "minute, single digit"),
                Triple("11:03", LocalTime.parse("11:03:00"), "minute, single digit 0-padded"),
                Triple("3:5", LocalTime.parse("03:05:00"), "hour and minute both single digit"),
                Triple("03:05", LocalTime.parse("03:05:00"), "hour and minute both single digit, 0-padded"),

                Triple("11:12:00", LocalTime.parse("11:12:00"), "hour:minute:seconds"),
                Triple("11:12:0", LocalTime.parse("11:12:00"), "hour:minute:second-single-digit")
            ) { (input: String, expected: LocalTime, desc: String) ->
                parseTime(input) shouldBe expected
            }
        }
        test("Seconds are supported by ignored") {
            parseTime("11:12:13") shouldBe LocalTime.parse("11:12:00")

        }

        context("invalid inputs") {
            withTests<String>(
                { "<$it> is not a valid time format" },
                listOf("", " ", "abc", "12:abc", "12:12:12:12", "11:")
            ) { input ->
                shouldThrow<IllegalArgumentException> {
                    parseTime(input)
                }
            }
        }
    }

    context("int-to-ordinal") {
        withTests(
            Pair(0, "0th"),
            Pair(1, "1st"),
            Pair(2, "2nd"),
            Pair(3, "3rd"),
            Pair(4, "4th"),
            Pair(10, "10th"),
            Pair(11, "11th"),
            Pair(12, "12th"),
            Pair(13, "13th"),
            Pair(21, "21st"),
        )
        { (num, expected) ->
            num.ordinalString shouldBe expected
        }

        test("should throw for negative number") {
            shouldThrow<IllegalArgumentException> {
                (-4).ordinalString
            }
        }
    }
})
