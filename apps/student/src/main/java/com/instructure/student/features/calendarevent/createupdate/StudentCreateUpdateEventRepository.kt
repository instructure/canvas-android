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

package com.instructure.student.features.calendarevent.createupdate

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventRepository


class StudentCreateUpdateEventRepository(
    calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val groupsApi: GroupAPI.GroupInterface,
    private val apiPrefs: ApiPrefs
) : CreateUpdateEventRepository(calendarEventApi) {

    override suspend fun getCanvasContexts(): List<CanvasContext> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = false)

        val coursesResult = coursesApi.getFirstPageCoursesCalendar(params)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        val courses = coursesResult.dataOrNull.orEmpty()
        val validCourses = courses.filter { !it.accessRestrictedByDate && it.hasActiveEnrollment() }

        val groupsResult = groupsApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupsApi.getNextPageGroups(nextUrl, params) }

        val groups = groupsResult.dataOrNull.orEmpty()

        val courseMap = validCourses.associateBy { it.id }
        val validGroups = groups.filter { it.courseId == 0L || courseMap[it.courseId] != null }

        val users = listOfNotNull(apiPrefs.user)

        return users + validGroups
    }
}
