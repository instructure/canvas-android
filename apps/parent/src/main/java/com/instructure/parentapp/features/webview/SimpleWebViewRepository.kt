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

package com.instructure.parentapp.features.webview

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs


class SimpleWebViewRepository(
    private val oAuthApi: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs
) {
    suspend fun getAuthenticatedSession(url: String): String {
        val params = RestParams(isForceReadFromNetwork = true)
        val userId = apiPrefs.user?.id
        return if (apiPrefs.isMasquerading && userId != null) {
            oAuthApi.getAuthenticatedSessionMasquerading(url, userId, params).dataOrThrow.sessionUrl
        } else {
            oAuthApi.getAuthenticatedSession(url, params).dataOrThrow.sessionUrl
        }
    }
}
