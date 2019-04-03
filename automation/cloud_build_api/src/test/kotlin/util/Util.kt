//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Util {

    @Test
    fun testFormatDuration() {
        assertThat(formatDuration(-59), `is`("0s"))
        assertThat(formatDuration(0), `is`("0s"))
        assertThat(formatDuration(1), `is`("1s"))
        assertThat(formatDuration(59), `is`("59s"))
        assertThat(formatDuration(60), `is`("1m"))
        assertThat(formatDuration(61), `is`("1m 1s"))
        assertThat(formatDuration(61 * 2), `is`("2m 2s"))
        assertThat(formatDuration(61 * 3), `is`("3m 3s"))
    }

    @Test
    fun yearWeekMath() {
        val yearWeek = ZonedDateTime.parse("2017-11-07T20:41:29Z").utcYearWeek()
        assertThat(yearWeek, `is`("2017 45"))

        val date = yearWeekToLocalDate(yearWeek).toString()
        assertThat(date, `is`("2017-11-06"))
    }

    @Test
    fun buildSuccessPercent() {
        val success = percent(5, 10)
        assertThat(success, `is`("50﹪"))
    }

    @Test
    fun weekRangeDefaultValue() {
        val actual = weekRange(1)

        val weeks = 1
        val start = ZonedDateTime.now()
        val after = yearWeekToLocalDate(start.utcYearWeek()) // Monday
        val limitDateAfter = after.minusDays(1) // Sunday
        val before = after.plusDays(weeks * 6L) // Sunday
        val limitDateBefore = before.plusDays(1) // Monday
        val endOfDay = LocalTime.MAX
        val expected = WeekRange(
                after = after,
                limitAfter = ZonedDateTime.of(limitDateAfter.atTime(endOfDay), ZoneOffset.UTC),
                before = before,
                limitBefore = ZonedDateTime.of(limitDateBefore.atStartOfDay(), ZoneOffset.UTC)
        )

        assertThat(actual, `is`(expected))
    }

    @Test
    fun weekRangeFixedStart() {
        val start = ZonedDateTime.of(
                LocalDate.parse("2018-04-02").atStartOfDay(),
                ZoneOffset.UTC
        )
        var actual = weekRange(weeks = 1, start = start)
        assertThat(actual.after.pretty(), `is`("Monday, April 2"))
        assertThat(actual.limitAfter.pretty(), `is`("Sunday, April 1"))
        assertThat(actual.before.pretty(), `is`("Sunday, April 8"))
        assertThat(actual.limitBefore.pretty(), `is`("Monday, April 9"))

        actual = weekRange(weeks = 2, start = start)
        assertThat(actual.after.pretty(), `is`("Monday, April 2"))
        assertThat(actual.limitAfter.pretty(), `is`("Sunday, April 1"))
        assertThat(actual.before.pretty(), `is`("Sunday, April 15"))
        assertThat(actual.limitBefore.pretty(), `is`("Monday, April 16"))
    }

    @Test
    fun weekRangesFixedDate() {
        val start = yearWeekToLocalDate("2018 14")

        val results = weekRanges(6, start)
        val actual = results.map { result -> "${result.after} - ${result.before}" }

        val expected = listOf(
                "2018-02-19 - 2018-02-25",
                "2018-02-26 - 2018-03-04",
                "2018-03-05 - 2018-03-11",
                "2018-03-12 - 2018-03-18",
                "2018-03-19 - 2018-03-25",
                "2018-03-26 - 2018-04-01",
                "2018-04-02 - 2018-04-08"
        )

        assertThat(actual, `is`(expected))
    }
}
