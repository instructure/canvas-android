/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.dashboard.edit.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StudentEditDashboardNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface
) : StudentEditDashboardDataSource {

    override suspend fun getCourses(): List<List<Course>> {
        val params = RestParams(isForceReadFromNetwork = true, usePerPageQueryParam = true)

        return coroutineScope {
            val currentCourses = async { courseApi.firstPageCoursesByEnrollmentState("active", params)
                .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrThrow }
            val pastCourses = async { courseApi.firstPageCoursesByEnrollmentState("completed", params)
                .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrThrow }
            val futureCourses = async { courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", params)
                .depaginate { nextUrl -> courseApi.next(nextUrl, params) }.dataOrThrow }

            return@coroutineScope listOf(currentCourses, pastCourses, futureCourses).awaitAll()
        }
    }

    override suspend fun getGroups(): List<Group> {
        val params = RestParams(isForceReadFromNetwork = true, usePerPageQueryParam = true)

        return groupApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }
            .dataOrNull.orEmpty()
    }
}