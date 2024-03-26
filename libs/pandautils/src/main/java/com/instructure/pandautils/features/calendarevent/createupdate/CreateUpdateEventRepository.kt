/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.calendarevent.createupdate

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ScheduleItem


class CreateUpdateEventRepository(
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface
) {

    suspend fun createEvent(
        title: String,
        startDate: String,
        endDate: String,
        rrule: String?,
        contextCode: String,
        locationName: String,
        locationAddress: String,
        description: String
    ): List<ScheduleItem> {
        val result = calendarEventApi.createCalendarEvent(
            contextCode = contextCode,
            title = title,
            description = description,
            startDate = startDate,
            endDate = endDate,
            allDay = startDate == endDate,
            rrule = rrule,
            locationName = locationName,
            locationAddress = locationAddress,
            body = "",
            restParams = RestParams()
        ).dataOrThrow

        return listOf(result) + result.duplicates.map { it.calendarEvent }
    }
}