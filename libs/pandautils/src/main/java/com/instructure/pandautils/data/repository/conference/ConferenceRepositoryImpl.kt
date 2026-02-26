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

package com.instructure.pandautils.data.repository.conference

import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.utils.DataResult


class ConferenceRepositoryImpl(
    private val conferencesApi: ConferencesApi.ConferencesInterface
) : ConferenceRepository {

    override suspend fun getLiveConferences(forceRefresh: Boolean): DataResult<List<Conference>> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return conferencesApi.getLiveConferences(params).map { it.conferences }
    }
}
