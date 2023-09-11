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

package com.instructure.student.features.navigation.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult

class NavigationNetworkDataSource(
    private val courseApi: CourseAPI.CoursesInterface,
    private val userApi: UserAPI.UsersInterface,
) : NavigationDataSource {

    override suspend fun getCourse(courseId: Long, forceNetwork: Boolean): Course? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return courseApi.getCourse(courseId, params).dataOrNull
    }

    suspend fun getSelf(): DataResult<User> {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getSelf(params)
    }
}
