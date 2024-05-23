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
package com.instructure.student.features.dashboard

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DashboardPositions
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate

class DashboardNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val apiPrefs: ApiPrefs,
    private val userApi: UserAPI.UsersInterface
): DashboardDataSource {

    override suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val coursesResult = if (apiPrefs.isStudentView) {
            courseApi.getFirstPageCoursesTeacher(params).depaginate { nextUrl -> courseApi.next(nextUrl, params) }
        } else {
            courseApi.getFirstPageCourses(params).depaginate { nextUrl -> courseApi.next(nextUrl, params) }
        }

        return coursesResult.dataOrNull.orEmpty()
    }

    override suspend fun getGroups(forceNetwork: Boolean): List<Group> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val groupsResult = groupApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }

        return groupsResult.dataOrNull ?: emptyList()
    }

    override suspend fun getDashboardCards(forceNetwork: Boolean): List<DashboardCard> {
        return courseApi.getDashboardCourses(RestParams(isForceReadFromNetwork = forceNetwork)).dataOrNull.orEmpty()
    }

    suspend fun updateDashboardPositions(dashboardPositions: DashboardPositions): DataResult<DashboardPositions> {
        return userApi.updateDashboardPositions(dashboardPositions, RestParams(isForceReadFromNetwork = true))
    }
}