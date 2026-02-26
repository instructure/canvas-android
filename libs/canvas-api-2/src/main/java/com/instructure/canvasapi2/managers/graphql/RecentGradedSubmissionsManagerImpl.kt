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

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.RecentGradedSubmissionsQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.utils.toDate
import java.util.Date

class RecentGradedSubmissionsManagerImpl(
    private val apolloClient: ApolloClient
) : RecentGradedSubmissionsManager {

    override suspend fun getRecentGradedSubmissions(
        studentId: Long,
        gradedSince: String,
        pageSize: Int,
        forceNetwork: Boolean
    ): RecentGradedSubmissionsQuery.Data {
        val query = RecentGradedSubmissionsQuery(
            studentId = studentId.toString(),
            pageSize = pageSize,
            gradedSince = gradedSince.toDate() ?: Date(0),
            submissionCursor = Optional.absent()
        )
        val initialData = apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors

        val allCourses = initialData.allCourses?.map { course ->
            val allSubmissionEdges = course.submissions?.edges?.toMutableList() ?: mutableListOf()
            var hasNextPage = course.submissions?.pageInfo?.hasNextPage == true
            var cursor = course.submissions?.pageInfo?.endCursor

            while (hasNextPage) {
                val paginatedQuery = RecentGradedSubmissionsQuery(
                    studentId = studentId.toString(),
                    pageSize = pageSize,
                    gradedSince = gradedSince.toDate() ?: Date(0),
                    submissionCursor = Optional.present(cursor)
                )
                val paginatedData = apolloClient.enqueueQuery(paginatedQuery, forceNetwork = forceNetwork).dataAssertNoErrors

                val courseData = paginatedData.allCourses?.find { it._id == course._id }
                courseData?.submissions?.edges?.let { allSubmissionEdges.addAll(it) }

                hasNextPage = courseData?.submissions?.pageInfo?.hasNextPage == true
                cursor = courseData?.submissions?.pageInfo?.endCursor
            }

            course.copy(
                submissions = course.submissions?.copy(
                    edges = allSubmissionEdges
                )
            )
        }

        return RecentGradedSubmissionsQuery.Data(allCourses)
    }
}