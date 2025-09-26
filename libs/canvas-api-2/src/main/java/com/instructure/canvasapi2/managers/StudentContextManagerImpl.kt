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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.enqueueQuery

class StudentContextManagerImpl(private val apolloClient: ApolloClient) : StudentContextManager {

    private var submissionPageSize = QLClientConfig.GRAPHQL_PAGE_SIZE
    private var endCursor: String? = null
    override var hasNextPage: Boolean = false
        private set

    override suspend fun getStudentContext(
        courseId: Long,
        userId: Long,
        submissionPageSize: Int,
        forceNetwork: Boolean
    ): StudentContextCardQuery.Data {
        this.submissionPageSize = submissionPageSize
        val query =
            StudentContextCardQuery(courseId.toString(), userId.toString(), submissionPageSize)
        val result = apolloClient.enqueueQuery(query, forceNetwork)

        this.endCursor = result.data?.course?.onCourse?.submissions?.pageInfo?.endCursor
        this.hasNextPage =
            result.data?.course?.onCourse?.submissions?.pageInfo?.hasNextPage ?: false

        return result.dataAssertNoErrors
    }

    override suspend fun getNextPage(
        courseId: Long,
        userId: Long,
        forceNetwork: Boolean
    ): StudentContextCardQuery.Data? {
        if (!hasNextPage) return null
        val query = StudentContextCardQuery(
            courseId.toString(),
            userId.toString(),
            submissionPageSize,
            Optional.present(endCursor)
        )
        val result = apolloClient.enqueueQuery(query, forceNetwork)

        this.endCursor = result.data?.course?.onCourse?.submissions?.pageInfo?.endCursor
        this.hasNextPage =
            result.data?.course?.onCourse?.submissions?.pageInfo?.hasNextPage ?: false

        return result.dataAssertNoErrors
    }
}