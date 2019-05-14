package com.instructure.student.db.sqlColAdapters

import com.squareup.sqldelight.ColumnAdapter
import org.threeten.bp.OffsetDateTime
import java.text.SimpleDateFormat
import java.util.*


class Date : ColumnAdapter<OffsetDateTime, String> {
    override fun decode(databaseValue: String) = OffsetDateTime.parse(databaseValue) as OffsetDateTime
    override fun encode(value: OffsetDateTime): String = value.toApiString()
}

fun OffsetDateTime?.toApiString(timeZone: TimeZone? = null): String {
    this ?: return ""

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
    timeZone?.let { format.timeZone = it }
    val formatted = format.format(this)
    return formatted.substring(0, 22) + ":" + formatted.substring(22)
}