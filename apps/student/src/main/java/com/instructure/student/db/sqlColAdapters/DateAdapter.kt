package com.instructure.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*


typealias Date = OffsetDateTime

/**
 * Expectation is that the database will hold whatever datetime the server gives back to us.
 * When we pull it out, it will be converted to a local [OffsetDateTime].
 * When we put something in, it will be converted to a UTC-based ISO-8601 datetime (what the server currently uses).
 */
class DateAdapter : ColumnAdapter<Date, String> {
    override fun decode(databaseValue: String) = Date.parse(databaseValue).withOffsetSameInstant(OffsetDateTime.now().offset) as Date
    override fun encode(value: Date): String = value.toApiString()
}

/**
 * Turns a local OffsetDateTime to a UTC-based ISO-8601 Datetime with timezone string
 */
fun OffsetDateTime?.toApiString(): String {
    this ?: return ""
    val utcTime = this.withOffsetSameInstant(ZoneOffset.UTC)
    return utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}