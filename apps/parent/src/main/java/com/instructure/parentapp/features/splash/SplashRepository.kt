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

package com.instructure.parentapp.features.splash

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.depaginate


class SplashRepository(
    private val userApi: UserAPI.UsersInterface,
    private val themeApi: ThemeAPI.ThemeInterface,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface
) {

    suspend fun getSelf(): User? {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getSelf(params).dataOrNull
    }

    suspend fun getColors(): CanvasColor? {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getColors(params).dataOrNull
    }

    suspend fun getTheme(): CanvasTheme? {
        val params = RestParams(isForceReadFromNetwork = true)
        return themeApi.getTheme(params).dataOrNull
    }

    suspend fun getStudents(): List<User> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        return enrollmentApi.firstPageObserveeEnrollmentsParent(params).depaginate {
            enrollmentApi.getNextPage(it, params)
        }.dataOrNull
            .orEmpty()
            .mapNotNull { it.observedUser }
    }

    suspend fun getBecomeUserPermission(): Boolean {
        val params = RestParams(isForceReadFromNetwork = true)
        return userApi.getBecomeUserPermission(params).dataOrNull?.becomeUser ?: false
    }
}