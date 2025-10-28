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
package com.instructure.horizon.features.skillspace

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class SkillspaceRepository @Inject constructor(
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs,
) {
    suspend fun getAuthenticatedSession(): AuthenticatedSession? {
        return oAuthApi.getAuthenticatedSession(
            apiPrefs.fullDomain,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrNull
    }

    fun getEmbeddedSkillspaceUrl(): String {
        val baseUrl = apiPrefs.fullDomain
        val skillspaceUrl = "$baseUrl/career/skillspace"
        return "$skillspaceUrl?embedded=true"
    }
}