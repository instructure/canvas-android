/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.SubmissionContentQuery

class SubmissionContentManagerImpl : SubmissionContentManager {

    override suspend fun getSubmissionContent(
        userId: Long,
        assignmentId: Long
    ): SubmissionContentQuery.Data {
        var hasNextPage = true
        var nextCursor: String? = null

        val allEdges = mutableListOf<SubmissionContentQuery.Edge?>()
        lateinit var data: SubmissionContentQuery.Data

        while (hasNextPage) {
            val query = SubmissionContentQuery(
                userId = userId.toString(),
                assignmentId = assignmentId.toString(),
                pageSize = QLClientConfig.GRAPHQL_PAGE_SIZE,
                nextCursor = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            )

            data = QLClientConfig.enqueueQuery(query).dataAssertNoErrors
            val connection = data.submission?.submissionHistoriesConnection

            allEdges.addAll(connection?.edges.orEmpty())

            hasNextPage = connection?.pageInfo?.hasNextPage ?: false
            nextCursor = connection?.pageInfo?.endCursor
        }

        return data.copy(
            submission = data.submission?.copy(
                submissionHistoriesConnection = data.submission?.submissionHistoriesConnection?.copy(
                    edges = allEdges
                )
            )
        )
    }
}
