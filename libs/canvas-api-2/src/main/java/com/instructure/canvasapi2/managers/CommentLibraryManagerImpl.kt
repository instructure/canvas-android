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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.CommentLibraryQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery

class CommentLibraryManagerImpl(private val apolloClient: ApolloClient) : CommentLibraryManager {

    override suspend fun getCommentLibraryItems(userId: Long): List<String> {
        var hasNextPage = true
        var nextCursor: String? = null
        val commentBankItems = mutableListOf<String>()

        while (hasNextPage) {
            val nextCursorParam = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            val query = CommentLibraryQuery(userId.toString(), QLClientConfig.GRAPHQL_PAGE_SIZE, nextCursorParam)
            val data = apolloClient.enqueueQuery(query).data
            val user = data?.user?.onUser
            val newItems = user?.commentBankItems?.edges?.mapNotNull {
                it?.node?.comment
            } ?: emptyList()

            commentBankItems.addAll(newItems)
            hasNextPage = user?.commentBankItems?.pageInfo?.hasNextPage ?: false
            nextCursor = user?.commentBankItems?.pageInfo?.endCursor
        }

        return commentBankItems
    }
}