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

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.instructure.canvasapi2.CommentLibraryQuery
import com.instructure.canvasapi2.QLCallback
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.utils.weave.awaitQL

class CommentLibraryManagerImpl : CommentLibraryManager {

    override suspend fun getCommentLibraryItems(userId: Long): List<String> {
        var hasNextPage = true
        var nextCursor: String? = null
        val commentBankItems = mutableListOf<String>()

        while (hasNextPage) {
            val data = awaitQL { getCommentLibraryItems(userId, it, nextCursor) }
            val user = data.user as CommentLibraryQuery.AsUser
            val newItems =  user.commentBankItems?.edges?.mapNotNull {
                it.node?.comment
            } ?: emptyList()

            commentBankItems.addAll(newItems)
            hasNextPage = user.commentBankItems?.pageInfo?.isHasNextPage ?: false
            nextCursor = user.commentBankItems?.pageInfo?.endCursor
        }

        return commentBankItems
    }

    private fun getCommentLibraryItems(
        userId: Long,
        callback: QLCallback<CommentLibraryQuery.Data>,
        nextCursor: String? = null
    ) {
        val query = CommentLibraryQuery.builder()
            .userId(userId.toString())
            .pageSize(QLClientConfig.GRAPHQL_PAGE_SIZE)
            .nextCursor(nextCursor)
            .build()

        callback.enqueueQuery(query) {
            cachePolicy = HttpCachePolicy.NETWORK_FIRST
        }
    }
}