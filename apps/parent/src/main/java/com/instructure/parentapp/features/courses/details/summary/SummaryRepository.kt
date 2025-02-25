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
package com.instructure.parentapp.features.courses.details.summary

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.depaginate

class SummaryRepository(
    private val courseApi: CourseAPI.CoursesInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface
) {

    suspend fun getCourse(id: Long): Course {
        return courseApi.getCourse(id, RestParams()).dataOrThrow
    }

    suspend fun getCalendarEvents(contextId: String, forceRefresh: Boolean = false): List<ScheduleItem> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        val assignmentEvents = calendarEventApi.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
            startDate = null,
            endDate = null,
            contextCodes = listOf(contextId),
            restParams = params
        ).depaginate {
            calendarEventApi.next(it, params)
        }.dataOrNull
            .orEmpty()
            .filterNot { it.isHidden }

        val calendarEvents = calendarEventApi.getCalendarEvents(
            allEvents = true,
            type = CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
            startDate = null,
            endDate = null,
            contextCodes = listOf(contextId),
            restParams = params
        ).depaginate {
            calendarEventApi.next(it, params)
        }.dataOrNull
            .orEmpty()
            .filterNot { it.isHidden }

        return (assignmentEvents + calendarEvents).sorted()
    }
}