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

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import api.bitrise.BitriseBuildObject
import java.math.BigInteger
import java.math.BigInteger.ZERO
import java.text.DecimalFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields

const val defaultBitriseAppSlug = "6ec29cf3b6e62901"
const val defaultBitriseTxt = "build/tmp/bitriseJobs.txt"

fun getEnv(name: String): String {
    return System.getenv(name) ?: throw RuntimeException("$name is null")
}

// YYYY-MM-DD https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types#date-type
// https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
fun LocalDate.sqlDate(): String {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(this)
}

/** week num is not zero padded. Examples: 2017 45, 2017 1 */
fun LocalDate.yearWeek(space: Boolean = true): String {
    val weekNumber = this.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val sb = StringBuilder()
    sb.append(year)
    if (space) sb.append(" ") // space delimiter is required for LocalDate.parse to work.
    sb.append(weekNumber)
    return sb.toString()
}

/**
 * Yearweek in YYYY w format.
 * Begins on Monday. Week is not zero padded.
 */
fun yearWeekToLocalDate(yearWeek: String): LocalDate {
    // YYYY = 2017 - year
    // w = 1 - non-zero padded week number
    return LocalDate.parse(yearWeek,
            DateTimeFormatterBuilder().appendPattern("YYYY w")
                    .parseDefaulting(WeekFields.ISO.dayOfWeek(), DayOfWeek.MONDAY.value.toLong())
                    .toFormatter())
}


fun ZonedDateTime.utcYearWeek(): String {
    return this.withZoneSameInstant(ZoneOffset.UTC).toLocalDate().yearWeek()
}


fun formatDuration(durationSeconds: Number?): String {
    if (durationSeconds == null) {
        throw RuntimeException("durationSeconds is null")
    }

    var duration = durationSeconds.toLong()
    // Bitrise has a weird bug where times may be negative.
    duration = if (durationSeconds.toLong() < 0L) 0 else duration

    val minutes = duration / 60
    val seconds = duration % 60

    return when {
        minutes == 0L && seconds == 0L -> "0s"
        minutes == 0L -> "${seconds}s"
        seconds == 0L -> "${minutes}m"
        else -> "${minutes}m ${seconds}s"
    }
}

fun percent(numerator: Int, denominator: Int, percentSymbol: Boolean = true): String {
    val result = (numerator.toDouble() / denominator.toDouble()) * 100

    // Use small percent so Google Sheets doesn't auto-convert percent to decimal.
    var string = DecimalFormat("#.##").format(result)
    if (percentSymbol) string += "﹪"
    return string
}

fun startDateWithinRange(buildObject: Any,
                         rangeAfter: ZonedDateTime?,
                         rangeBefore: ZonedDateTime?): Boolean {
    if (rangeAfter == null && rangeBefore == null) return true

    val buildStart = when (buildObject) {
        is BitriseBuildObject -> {
            ZonedDateTime.parse(buildObject.triggered_at).withZoneSameInstant(ZoneOffset.UTC)
        }
        else -> throw RuntimeException("Unknown build object $buildObject")
    }

    val dateAfterStart = if (rangeAfter == null) true else buildStart.isAfter(rangeAfter)
    val dateBeforeEnd = if (rangeBefore == null) true else buildStart.isBefore(rangeBefore)

    return dateAfterStart && dateBeforeEnd
}

data class WeekRange(
        val after: LocalDate,
        val limitAfter: ZonedDateTime,
        val before: LocalDate,
        val limitBefore: ZonedDateTime
)

fun weekRange(weeks: Int,
              start: LocalDate): WeekRange {
    return weekRange(weeks, ZonedDateTime.of(start.atStartOfDay(), ZoneOffset.UTC))
}

fun weekRanges(weeks: Int, start: LocalDate = yearWeekToLocalDate(ZonedDateTime.now(ZoneOffset.UTC).utcYearWeek())): List<WeekRange> {
    val result = mutableListOf<WeekRange>()

    for (weekNumber in weeks downTo 0) {
        result.add(weekRange(1, start.minusDays(weekNumber * 7L)))
    }

    return result
}

fun weekRange(weeks: Int,
              start: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): WeekRange {
    // Report is limited to a full week (after Sunday and before next Monday)
    // 1 2 3 4 5 6 7 1 2 3 4 5 6 7 1 2 3 4 5 6 7
    //             a_b___________c_d
    //
    // limitDateAfter to limitDateBefore gives us: 1 2 3 4 5 6 7
    //
    // 7a limitDateAfter  Sunday
    // 1b after           Monday
    // 7c before          Sunday
    // 1d limitDateBefore Monday
    //
    val after = yearWeekToLocalDate(start.utcYearWeek()) // Monday
    val limitDateAfter = after.minusDays(1) // Sunday
    val before = after.plusDays(weeks * 7L - 1) // Sunday
    val limitDateBefore = before.plusDays(1) // Monday

    val endOfDay = LocalTime.MAX
    val startOfDay = LocalTime.MIDNIGHT
    // if limitDateAfter is Sunday and limitDateBefore is Monday then:
    // after Sunday just before midnight at the end of the day.
    val limitTimeAfter = limitDateAfter.atTime(endOfDay).atZone(ZoneOffset.UTC)
    // before Monday midnight at the start of the day
    val limitTimeBefore = limitDateBefore.atTime(startOfDay).atZone(ZoneOffset.UTC)

    return WeekRange(
            after = after,
            limitAfter = limitTimeAfter,
            before = before,
            limitBefore = limitTimeBefore)
}

private const val _prettyDateTime = "EEEE, MMMM d"
val prettyDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern(_prettyDateTime)
val prettyDateTimeYear: DateTimeFormatter = DateTimeFormatter.ofPattern("${_prettyDateTime} yyyy")

fun LocalDate.pretty(): String {
    return prettyDateTime.format(this)
}

fun ZonedDateTime.pretty(): String {
    return prettyDateTime.format(this)
}

private val KB = BigInteger.valueOf(1024)
private val MB = KB.multiply(KB)
private val GB = KB.multiply(MB)
private val TB = KB.multiply(GB)

fun Long.humanReadable(): String {
    val size = BigInteger.valueOf(this)

    return when {
        size.divide(TB) > ZERO -> "${size.divide(TB)} TB"
        size.divide(GB) > ZERO -> "${size.divide(GB)} GB"
        size.divide(MB) > ZERO -> "${size.divide(MB)} MB"
        size.divide(KB) > ZERO -> "${size.divide(KB)} KB"
        else -> "$size bytes"
    }
}

fun BitriseApps.getOnlyInstructureApps(): List<BitriseAppObject> {
    return this.getAppsForOrg()
            .filterNot { it.title.startsWith("px-") }
}
