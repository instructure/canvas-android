/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.student.widget.todo

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.room.calendar.entities.CalendarFilterEntity

class ToDoWidgetRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val calendarFilterDao: CalendarFilterDao
) {
    suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): DataResult<List<PlannerItem>> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork,
            shouldLoginOnTokenError = false
        )

        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            contextCodes,
            null,
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.map { items ->
            items.filter {
                it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST
            }
        }
    }

    suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork,
            shouldLoginOnTokenError = false
        )

        return coursesApi.getFirstPageCourses(restParams).depaginate { nextUrl ->
            coursesApi.next(nextUrl, restParams)
        }.dataOrNull.orEmpty()
    }

    suspend fun getCalendarFilters(userId: Long, domain: String): CalendarFilterEntity? {
        return calendarFilterDao.findByUserIdAndDomain(userId, domain)
    }
}
