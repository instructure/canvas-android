/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceList
import com.instructure.canvasapi2.utils.ExhaustiveCallback
import com.instructure.canvasapi2.utils.weave.apiAsync

object ConferenceManager {
    fun getConferences(canvasContext: CanvasContext, callback: StatusCallback<List<Conference>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            usePerPageQueryParam = true,
            isForceReadFromNetwork = forceNetwork
        )
        val depaginatedCallback = object : ExhaustiveCallback<ConferenceList, Conference>(callback) {
            override fun getNextPage(callback: StatusCallback<ConferenceList>, nextUrl: String, isCached: Boolean) {
                ConferencesApi.getNextPage(nextUrl, adapter, callback, params)
            }

            override fun extractItems(response: ConferenceList): List<Conference> = response.conferences
        }
        ConferencesApi.getConferences(canvasContext, adapter, depaginatedCallback, params)
    }

    fun getConferencesAsync(canvasContext: CanvasContext, forceNetwork: Boolean) = apiAsync<List<Conference>> {
        getConferences(canvasContext, it, forceNetwork)
    }
}
