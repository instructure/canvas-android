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

package com.instructure.student.mobius.conferences.conference_list.datasource

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.offline.sync.ConferenceSyncHelper

class ConferenceListNetworkDataSource(
    private val conferenceSyncHelper: ConferenceSyncHelper,
    private val oAuthApi: OAuthAPI.OAuthInterface
) : ConferenceListDataSource {

    override suspend fun getConferencesForContext(
        canvasContext: CanvasContext, forceNetwork: Boolean
    ): DataResult<List<Conference>> {
        return conferenceSyncHelper.getConferencesForContext(canvasContext, forceNetwork)
    }

    suspend fun getAuthenticatedSession(targetUrl: String): AuthenticatedSession {
        val params = RestParams(isForceReadFromNetwork = true)

        return oAuthApi.getAuthenticatedSession(targetUrl, params).dataOrThrow
    }
}
