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

package com.instructure.parentapp.features.managestudents

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.depaginate


class ManageStudentsRepository(
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
    private val userApi: UserAPI.UsersInterface
) {

    suspend fun getStudents(forceRefresh: Boolean): List<User> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceRefresh)
        return enrollmentApi.firstPageObserveeEnrollmentsParent(params).depaginate {
            enrollmentApi.getNextPage(it, params)
        }.dataOrNull
            .orEmpty()
            .mapNotNull { it.observedUser }
            .distinct()
            .sortedBy { it.sortableName }
    }

    suspend fun saveStudentColor(contextId: String, color: String): String? {
        val params = RestParams()
        return userApi.setColor(contextId, color, params).dataOrNull?.hexCode
    }
}
