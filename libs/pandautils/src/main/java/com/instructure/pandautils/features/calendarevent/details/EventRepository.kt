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

package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContextPermission.Companion.MANAGE_CALENDAR
import com.instructure.canvasapi2.models.ScheduleItem

class EventRepository(
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface
) {
    suspend fun getCalendarEvent(eventId: Long) = calendarEventApi.getCalendarEvent(eventId, RestParams()).dataOrThrow

    suspend fun deleteCalendarEvent(
        eventId: Long
    ): ScheduleItem {
        return calendarEventApi.deleteCalendarEvent(eventId, RestParams()).dataOrThrow
    }

    suspend fun deleteRecurringCalendarEvent(
        eventId: Long,
        modifyEventScope: CalendarEventAPI.ModifyEventScope
    ): List<ScheduleItem> {
        return calendarEventApi.deleteRecurringCalendarEvent(eventId, modifyEventScope.apiName, RestParams()).dataOrThrow
    }

    suspend fun canManageCourseCalendar(contextId: Long) = courseApi.getCoursePermissions(
        contextId, listOf(MANAGE_CALENDAR), RestParams()
    ).dataOrThrow.manageCalendar

    suspend fun canManageGroupCalendar(contextId: Long) = groupApi.getGroupPermissions(
        contextId, listOf(MANAGE_CALENDAR), RestParams()
    ).dataOrThrow.manageCalendar
}
