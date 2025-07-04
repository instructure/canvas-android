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
package com.instructure.student.widget.grades

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.isInvited

class GradesWidgetRepository(
    private val coursesApi: CourseAPI.CoursesInterface
) {
    suspend fun getCoursesWithGradingScheme(forceNetwork: Boolean = true): DataResult<List<Course>> {
        val restParams = RestParams(
            isForceReadFromNetwork = forceNetwork,
            usePerPageQueryParam = true,
            shouldLoginOnTokenError = false
        )

        return coursesApi.getFirstPageCoursesWithGradingScheme(restParams)
            .depaginate { nextUrl -> coursesApi.next(nextUrl, restParams) }
            .map { courses ->
                val validCourses = courses
                    .filter { it.isCurrentEnrolment() && !it.isInvited() }

                validCourses
                    .filter { it.isFavorite }
                    .ifEmpty { validCourses }
            }
    }
}
