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

package com.instructure.pandautils.features.offline.sync

import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceList
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.ConferenceFacade

class ConferenceSyncHelper(
    private val conferencesApi: ConferencesApi.ConferencesInterface,
    private val conferenceFacade: ConferenceFacade
) {

    suspend fun fetchConferences(courseId: Long) {
        val conferences = getConferencesForContext(CanvasContext.emptyCourseContext(courseId), true).dataOrNull
        conferences?.let { conferenceFacade.insertConferences(it, courseId) }
    }

    suspend fun getConferencesForContext(
        canvasContext: CanvasContext, forceNetwork: Boolean
    ): DataResult<List<Conference>> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        return conferencesApi.getConferencesForContext(canvasContext.toAPIString().drop(1), params).depaginate {
            conferencesApi.getNextPage(it, params)
        }.map {
            it.conferences
        }
    }

    private suspend fun DataResult<ConferenceList>.depaginate(
        nextPageCall: suspend (nextUrl: String) -> DataResult<ConferenceList>
    ): DataResult<ConferenceList> {
        if (this !is DataResult.Success) return this

        val depaginatedList = data.conferences.toMutableList()
        var nextUrl = linkHeaders.nextUrl
        while (nextUrl != null) {
            val newItemsResult = nextPageCall(nextUrl)
            nextUrl = if (newItemsResult is DataResult.Success) {
                depaginatedList.addAll(newItemsResult.data.conferences)
                newItemsResult.linkHeaders.nextUrl
            } else {
                null
            }
        }

        return DataResult.Success(ConferenceList(depaginatedList))
    }
}
