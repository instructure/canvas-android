/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders

class InboxRepository(private val inboxApi: InboxApi.InboxInterface) {

    suspend fun getConversations(scope: InboxApi.Scope, forceNetwork: Boolean, nextPageLink: String? = null): DataResult<List<Conversation>> {
        // TODO Change perPageCount to default at the end, this is only for testing purposes.
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork, perPageCount = 15)
        val linkHeaders = if (nextPageLink != null) {
            LinkHeaders().apply { nextUrl = nextPageLink }
        } else null

        return if (StatusCallback.isFirstPage(linkHeaders)) {
            inboxApi.getConversationsSuspend(InboxApi.conversationScopeToString(scope), params)
        } else if (linkHeaders != null && StatusCallback.moreCallsExist(linkHeaders)) {
            inboxApi.getNextPageSuspend(linkHeaders.nextUrl!!, params)
        } else {
            DataResult.Fail()
        }
    }

    suspend fun batchUpdateConversations(conversationIds: List<Long>, conversationEvent: String): DataResult<Void> {
        return inboxApi.batchUpdateConversations(conversationIds, conversationEvent)
    }
}