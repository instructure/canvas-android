/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.data.repository.course

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class CourseNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface
) : CourseDataSource {

    override suspend fun getCourse(courseId: Long, forceRefresh: Boolean): DataResult<Course> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCourse(courseId, params)
    }

    override suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return courseApi.getFirstPageCourses(params).depaginate { nextUrl ->
            courseApi.next(nextUrl, params)
        }
    }

    override suspend fun getFavoriteCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return courseApi.getFavoriteCourses(params).depaginate { nextUrl ->
            courseApi.next(nextUrl, params)
        }
    }

    override suspend fun getDashboardCards(forceRefresh: Boolean): DataResult<List<DashboardCard>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getDashboardCourses(params)
    }
}