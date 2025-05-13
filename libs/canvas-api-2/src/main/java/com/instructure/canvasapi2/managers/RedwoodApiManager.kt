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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.RedwoodGraphQLClientConfig
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import java.util.Date
import javax.inject.Inject

class RedwoodApiManager @Inject constructor(
    private val redwoodClient: RedwoodGraphQLClientConfig,
) {
    suspend fun getNotes(
        filter: NoteFilterInput? = null,
        firstN: Int? = null,
        lastN: Int? = null,
        after: Date? = null,
        before: Date? = null,
        orderBy: OrderByInput? = null,
    ): QueryNotesQuery.Notes {
        val query = QueryNotesQuery(
            filter = Optional.presentIfNotNull(filter),
            first = Optional.presentIfNotNull(firstN?.toDouble()),
            last = Optional.presentIfNotNull(lastN?.toDouble()),
            after = Optional.presentIfNotNull(after?.toApiString()),
            before = Optional.presentIfNotNull(before?.toApiString()),
            orderBy = Optional.presentIfNotNull(orderBy),
        )
        val result = QLClientConfig
            .enqueueQuery(query, block = redwoodClient.createClientConfigBlock())
            .dataAssertNoErrors.notes

        return result
    }
}