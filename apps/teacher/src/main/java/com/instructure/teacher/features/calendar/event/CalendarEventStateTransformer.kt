/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.features.calendar.event

import android.content.Context
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.teacher.R
import java.util.*

class CalendarEventStateTransformer {

    fun transformScheduleItem(context: Context, scheduleItem: ScheduleItem?): CalendarEventViewState {
        return scheduleItem?.let {
            createCalendarEventViewState(context, it)
        } ?: CalendarEventViewState()
    }

    private fun createCalendarEventViewState(context: Context, scheduleItem: ScheduleItem): CalendarEventViewState? {
        val dateTitle: String
        val dateSubtitle: String
        val locationTitle: String
        val locationSubtitle: String

        if (scheduleItem.isAllDay) {
            dateTitle = context.getString(R.string.allDayEvent)
            dateSubtitle = getFullDateString(context, scheduleItem.endDate)
        } else {
            // Setup the calendar event start/end times
            if (scheduleItem.startDate != null && scheduleItem.endDate != null) {
                // Our date times are different so we display two strings
                dateTitle = getFullDateString(context, scheduleItem.endDate)
                val startTime = DateHelper.getFormattedTime(context, scheduleItem.startDate) ?: ""
                val endTime = DateHelper.getFormattedTime(context, scheduleItem.endDate)

                val isTimeIntervalEvent = (scheduleItem.startDate?.time != scheduleItem.endDate?.time)
                dateSubtitle = if (isTimeIntervalEvent) "$startTime - $endTime" else startTime
            } else {
                dateTitle = getFullDateString(context, scheduleItem.startDate)
                dateSubtitle = ""
            }
        }

        val noLocationName = scheduleItem.locationName.isNullOrBlank()
        val noLocationAddress = scheduleItem.locationAddress.isNullOrBlank()

        if (noLocationAddress && noLocationName) {
            locationTitle = context.getString(R.string.noLocation)
            locationSubtitle = ""
        } else {
            if (noLocationName) {
                locationTitle = scheduleItem.locationAddress ?: ""
                locationSubtitle = ""
            } else {
                locationTitle = scheduleItem.locationName ?: ""
                locationSubtitle = scheduleItem.locationAddress ?: ""
            }
        }

        return CalendarEventViewState(
            scheduleItem.title ?: "",
            dateTitle,
            dateSubtitle,
            locationTitle,
            locationSubtitle,
            scheduleItem.description ?: "")
    }

    private fun getFullDateString(context: Context, date: Date?): String {
        return date?.let {
            val dayOfWeek = DateHelper.fullDayFormat.format(it)
            val dateString = DateHelper.getFormattedDate(context, it)

            "$dayOfWeek $dateString"
        } ?: ""
    }
}