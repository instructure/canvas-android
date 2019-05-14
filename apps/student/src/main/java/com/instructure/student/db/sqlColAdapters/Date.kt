package com.instructure.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.OffsetDateTime
import java.text.SimpleDateFormat
import java.util.*


typealias Date = OffsetDateTime

class DateAdapter : ColumnAdapter<Date, String> {
    override fun decode(databaseValue: String) = Date.parse(databaseValue) as Date
    override fun encode(value: Date): String = value.toApiString()
}

fun OffsetDateTime?.toApiString(timeZone: TimeZone? = null): String {
    this ?: return ""

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    timeZone?.let { format.timeZone = it }
    val formatted = format.format(this)
    return formatted.substring(0, 22) + ":" + formatted.substring(22)
}