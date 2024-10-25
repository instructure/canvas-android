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
package com.instructure.parentapp.features.inbox.coursepicker

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm
import javax.inject.Inject

class ParentInboxCoursePickerRepository @Inject constructor(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface
) {
    suspend fun getCourses(): DataResult<List<Course>> {
        val params = RestParams(usePerPageQueryParam = true)

        val coursesResult = courseAPI.getCoursesByEnrollmentType(Enrollment.EnrollmentType.Observer.apiTypeString, params)
            .depaginate { nextUrl -> courseAPI.next(nextUrl, params) }

        val courses = coursesResult.dataOrNull ?: return coursesResult

        val validCourses = courses.filter { it.isValidTerm() && it.hasActiveEnrollment() }

        return DataResult.Success(validCourses)
    }

    suspend fun getEnrollments(): DataResult<List<Enrollment>> {
        val params = RestParams(usePerPageQueryParam = true)
        return enrollmentApi.firstPageObserveeEnrollmentsParent(params)
            .depaginate { nextUrl -> enrollmentApi.getNextPage(nextUrl, params) }
    }
}