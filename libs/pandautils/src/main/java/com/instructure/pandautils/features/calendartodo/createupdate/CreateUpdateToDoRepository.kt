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

package com.instructure.pandautils.features.calendartodo.createupdate

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.postmodels.PlannerNoteBody
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm

class CreateUpdateToDoRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val plannerApi: PlannerAPI.PlannerInterface
) {
    suspend fun getCourses(): List<Course> {
        val params = RestParams()
        return coursesApi.getFirstPageCoursesCalendar(params)
            .depaginate { nextUrl ->
                coursesApi.next(nextUrl, params)
            }
            .map {
                it.filter { course ->
                    !course.accessRestrictedByDate && course.hasActiveEnrollment()
                }
            }
            .dataOrNull
            .orEmpty()
    }

    suspend fun createToDo(
        title: String,
        details: String?,
        toDoDate: String,
        courseId: Long?
    ) {
        plannerApi.createPlannerNote(
            PlannerNoteBody(
                title = title,
                details = details,
                toDoDate = toDoDate,
                courseId = courseId
            ), RestParams()
        ).dataOrThrow
    }

    suspend fun updateToDo(
        id: Long,
        title: String,
        details: String?,
        toDoDate: String,
        courseId: Long?
    ) {
        plannerApi.updatePlannerNote(
            id,
            PlannerNoteBody(
                title = title,
                details = details,
                toDoDate = toDoDate,
                courseId = courseId
            ), RestParams()
        ).dataOrThrow
    }
}