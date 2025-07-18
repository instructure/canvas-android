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
package com.instructure.horizon.features.account

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.models.User
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userApi: UserAPI.UsersInterface,
    private val experienceAPI: ExperienceAPI
) {
    suspend fun getUserDetails(forceRefresh: Boolean): User {
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
        return userApi.getSelf(restParams).dataOrThrow
    }

    suspend fun getExperiences(forceRefresh: Boolean): List<String> {
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
        return experienceAPI.getExperienceSummary(restParams).dataOrNull?.availableApps ?: emptyList()
    }

    suspend fun switchExperience() {
        experienceAPI.switchExperience(RestParams(), ExperienceSummary.ACADEMIC_EXPERIENCE).dataOrThrow
    }
}