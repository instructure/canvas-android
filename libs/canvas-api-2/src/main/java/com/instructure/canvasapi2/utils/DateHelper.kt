/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.canvasapi2.utils

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import java.text.Format
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
object DateHelper {
    fun stringToDateWithMillis(iso8601string: String?): Date? {
        return try {
            var s = iso8601string!!.replace("Z", "+00:00")
            s = s.substring(0, 22) + s.substring(23)
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(s)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Date Format Standards for Android
     * SHORT: 12/31/2000 or 1/3/2000
     * MEDIUM: Jan 3, 2000
     * LONG: Monday, January 3, 2000
     */
    fun getPreferredDateFormat(context: Context): Format = DateFormat.getMediumDateFormat(context)

    fun getFormattedDate(context: Context?, date: Date?): String? {
        if (context == null || date == null) return null
        return getPreferredDateFormat(context).format(date.time)
    }

    /**
     * Returns the time of day if the [date] is today, otherwise returns the abbreviated month name and day of month
     */
    fun getDayMonthDateString(context: Context, date: Date): String {
        val format = if (DateUtils.isToday(date.time)) getPreferredDateFormat(context) else dayMonthDateFormat
        return format.format(date.time)
    }

    fun getDayMonthYearDateString(context: Context, date: Date): String {
        val format = if (DateUtils.isToday(date.time)) getPreferredDateFormat(context) else dayMonthYearFormat
        return format.format(date.time)
    }

    fun getPreferredTimeFormat(context: Context?): SimpleDateFormat {
        return if (DateFormat.is24HourFormat(context)) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("h:mm a", Locale.getDefault())
        }
    }

    fun getTimeNoMinutesFormat(context: Context?): SimpleDateFormat {
        return if (DateFormat.is24HourFormat(context)) {
            SimpleDateFormat("HH", Locale.getDefault())
        } else {
            SimpleDateFormat("hha", Locale.getDefault())
        }
    }

    val dayMonthDateFormat: SimpleDateFormat get() = SimpleDateFormat("MMM d", Locale.getDefault())

    val fullDayFormat: SimpleDateFormat get() = SimpleDateFormat("EEEE,", Locale.getDefault())

    val fullMonthNoLeadingZeroDateFormat: SimpleDateFormat get() = SimpleDateFormat("MMMM d", Locale.getDefault())

    val dayMonthYearFormat: SimpleDateFormat get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val dayMonthDateFormatUniversal: SimpleDateFormat get() = SimpleDateFormat("MMM d", Locale.getDefault())

    val monthDayYearDateFormatUniversal: SimpleDateFormat
        get() = SimpleDateFormat("MMMM d, YYYY", Locale.getDefault())

    val monthDayYearDateFormatUniversalShort: SimpleDateFormat
        get() = SimpleDateFormat("MMM d, YYYY", Locale.getDefault())


    fun getFormattedTime(context: Context?, date: Date?): String? {
        if (context == null || date == null) return null
        return getPreferredTimeFormat(context).format(date)
    }

    fun createPrefixedDateString(context: Context?, prefix: String, date: Date?): String? {
        return context?.let { prefix + ": " + getFormattedDate(it, date) }
    }

    fun createPrefixedDateString(context: Context?, prefixResId: Int, date: Date?): String? {
        return context?.let { createPrefixedDateString(it, it.resources.getString(prefixResId), date) }
    }

    fun createPrefixedDateTimeString(context: Context?, prefix: String, date: Date?): String? {
        return context?.let { prefix + ": " + getFormattedDate(it, date) + " " + getFormattedTime(it, date) }
    }

    fun createPrefixedDateTimeString(context: Context?, prefixResId: Int, date: Date?): String? {
        return context?.let { createPrefixedDateTimeString(it, it.resources.getString(prefixResId), date) }
    }

    fun getDateTimeString(context: Context?, date: Date?): String? {
        return context?.let { getFormattedDate(it, date) + " " + getFormattedTime(it, date) }
    }

    fun getDateAtTimeString(context: Context?, stringResId: Int, dateTime: Date?): String? {
        if (context == null || dateTime == null) {
            return null
        }
        val date = getDayMonthDateString(context, dateTime)
        val time = getFormattedTime(context, dateTime)
        return context.getString(stringResId, date, time)
    }

    fun getDateAtTimeString(context: Context?, dateTime: Date?): String? {
        if (context == null || dateTime == null) {
            return null
        }
        val date = getDayMonthDateString(context, dateTime)
        val time = getFormattedTime(context, dateTime)
        return "$date, $time"
    }

    fun getDateAtTimeWithYearString(context: Context?, stringResId: Int, dateTime: Date?): String? {
        if (context == null || dateTime == null) {
            return null
        }
        val date = getDayMonthYearDateString(context, dateTime)
        val time = getFormattedTime(context, dateTime)
        return context.getString(stringResId, date, time)
    }

    /**
     * Simple date helper, formats date as:
     *
     * Month 15 separator 11:59 PM
     * @param context
     * @param date
     * @param separator example "at", spacing is handled by helper
     * @return
     */
    fun getMonthDayAtTime(context: Context?, date: Date?, separator: String): String? {
        date ?: return null
        var dateString: String = dayMonthDateFormatUniversal.format(date)
        dateString += " $separator "
        dateString += getPreferredTimeFormat(context).format(date)
        return dateString
    }

    /**
     * Simple date helper, formats date as:
     *
     * Month 15 separator 11:59 PM
     * @param context
     * @param date
     * @param stringResSeparator String resource for separator; example "at", spacing is handled by helper
     * @return
     */
    fun getMonthDayAtTime(context: Context, date: Date?, stringResSeparator: Int): String {
        val separator = context.getString(stringResSeparator)
        var dateString: String = dayMonthDateFormatUniversal.format(date)
        dateString += " $separator "
        dateString += getPreferredTimeFormat(context).format(date)
        return dateString
    }

    /**
     * Simple date helper, examples:
     *
     * Sep 15, 2018 {separator} 9:02am
     *
     * or
     *
     * Sep 15 {separator} 11pm <- No year if year matches the curren year; Minutes not displayed if there are none
     *
     * Year is added only if it is not the current year.
     * @param context
     * @param date
     * @param stringResSeparator String resource for separator; example "at", spacing is handled by helper
     * @return If date is null, will return a null string
     */
    fun getMonthDayTimeMaybeMinutesMaybeYear(context: Context, date: Date?, stringResSeparator: Int): String? {
        date ?: return null
        val separator = context.getString(stringResSeparator)
        val dateString: StringBuilder = if (isThisYear(date)) {
            StringBuilder(dayMonthDateFormatUniversal.format(date))
        } else {
            StringBuilder(monthDayYearDateFormatUniversal.format(date))
        }
        dateString.append(" ").append(separator).append(" ")
        if (timeHasMinutes(date)) {
            dateString.append(getPreferredTimeFormat(context).format(date))
        } else {
            dateString.append(getTimeNoMinutesFormat(context).format(date))
        }
        return dateString.toString()
    }

    fun timeHasMinutes(date: Date?): Boolean {
        val srcCal = Calendar.getInstance()
        srcCal.time = date
        return srcCal[Calendar.MINUTE] > 0
    }

    fun isThisYear(date: Date?): Boolean {
        val srcCal = Calendar.getInstance()
        srcCal.time = date
        val compare = Date()
        val compareCal = Calendar.getInstance()
        compareCal.time = compare
        return srcCal[Calendar.YEAR] == compareCal[Calendar.YEAR]
    }

    /**
     * Transform Calendar to ISO 8601 string.
     */
    fun dateToDayMonthYearString(context: Context?, date: Date?): String? {
        return date?.let { getFormattedDate(context, it) }
    }

    /**
     * Used for making a clean date when comparing items.
     * @param dateTime
     * @return
     */
    fun getCleanDate(dateTime: Long): Date {
        val cal = GregorianCalendar()
        cal.timeInMillis = dateTime
        val genericDate = GregorianCalendar(
                cal[Calendar.YEAR],
                cal[Calendar.MONTH],
                cal[Calendar.DAY_OF_MONTH]
        )
        return Date(genericDate.timeInMillis)
    }

    /**
     * Creates a new date from the provided year, month, day, hour, minute, and second
     * @param year The year
     * @param month Month of the year, zero-indexed (e.g. 0 is January)
     * @param day Day of the month
     * @param hour Hour of the day
     * @param minute Minute of the hour
     * @param second Second of the minute
     * @return The new Date
     */
    fun makeDate(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Date {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, hour, minute] = second
        return calendar.time
    }

}
