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
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.depaginate

class ToDoWidgetRepository(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val coursesApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface
) {
    suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): List<PlannerItem> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork,
            shouldLoginOnTokenError = false
        )

        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            contextCodes,
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.dataOrThrow.filter {
            it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST
        }
    }

    suspend fun getFavouriteCourses(forceNetwork: Boolean): List<Course> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork,
            shouldLoginOnTokenError = false
        )

        val courses = coursesApi.getFirstPageCourses(restParams).depaginate { nextUrl ->
            coursesApi.next(nextUrl, restParams)
        }.dataOrNull.orEmpty()

        return courses.filter {
            it.isFavorite
        }.ifEmpty {
            courses
        }
    }

    suspend fun getFavouriteGroups(forceNetwork: Boolean): List<Group> {
        val restParams = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork,
            shouldLoginOnTokenError = false
        )

        val groups = groupApi.getFirstPageGroups(restParams).depaginate { nextUrl ->
            groupApi.getNextPageGroups(nextUrl, restParams)
        }.dataOrNull.orEmpty()

        return groups.filter {
            it.isFavorite
        }.ifEmpty {
            groups
        }
    }
}
