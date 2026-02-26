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

package com.instructure.pandautils.domain.usecase.session

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class GetAuthenticatedSessionUseCase @Inject constructor(
    private val oauthApi: OAuthAPI.OAuthInterface,
    private val apiPrefs: ApiPrefs
) : BaseUseCase<GetAuthenticatedSessionUseCase.Params, String>() {

    data class Params(
        val targetUrl: String
    )

    override suspend fun execute(params: Params): String {
        var url = params.targetUrl

        if (url.startsWith(apiPrefs.fullDomain)) {
            val restParams = RestParams()
            val authSession = oauthApi.getAuthenticatedSession(url, restParams)
            authSession.dataOrNull?.sessionUrl?.let { sessionUrl ->
                url = sessionUrl
            }
        }

        return url
    }
}