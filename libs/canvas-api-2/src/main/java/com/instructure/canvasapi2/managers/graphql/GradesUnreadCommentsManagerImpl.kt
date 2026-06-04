/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.GradesUnreadCommentCountsQuery
import com.instructure.canvasapi2.enqueueQuery

class GradesUnreadCommentsManagerImpl(
    private val apolloClient: ApolloClient
) : GradesUnreadCommentsManager {

    override suspend fun getUnreadCommentCounts(
        courseId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ): Map<Long, Int> {
        val result = mutableMapOf<Long, Int>()
        var cursor: String? = null

        do {
            val query = GradesUnreadCommentCountsQuery(
                courseId = courseId.toString(),
                studentId = studentId.toString(),
                pageSize = PAGE_SIZE,
                submissionCursor = Optional.presentIfNotNull(cursor)
            )
            val connection = apolloClient.enqueueQuery(query, forceNetwork = forceNetwork)
                .dataAssertNoErrors
                .course
                ?.submissionsConnection

            connection?.nodes?.filterNotNull()?.forEach { node ->
                val assignmentId = node.assignment?._id?.toLongOrNull() ?: return@forEach
                result[assignmentId] = node.unreadCommentCount
            }

            cursor = connection?.pageInfo?.endCursor
            // Guard against a malformed response advancing no further while still reporting more pages.
        } while (connection?.pageInfo?.hasNextPage == true && cursor != null)

        return result
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}