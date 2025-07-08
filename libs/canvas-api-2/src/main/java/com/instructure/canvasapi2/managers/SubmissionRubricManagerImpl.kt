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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.SubmissionRubricQuery

class SubmissionRubricManagerImpl : SubmissionRubricManager {

    override suspend fun getRubrics(assignmentId: Long, userId: Long): SubmissionRubricQuery.Data {
        var hasNextPage = true
        var nextCursor: String? = null

        val assessments = mutableListOf<SubmissionRubricQuery.Edge?>()
        lateinit var data: SubmissionRubricQuery.Data

        while (hasNextPage) {
            val nextCursorParam =
                if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            val query =
                SubmissionRubricQuery(assignmentId.toString(), userId.toString(), nextCursorParam)

            data = QLClientConfig.enqueueQuery(query, forceNetwork = true).dataAssertNoErrors
            assessments.addAll(data.submission?.rubricAssessmentsConnection?.edges ?: emptyList())

            hasNextPage =
                data.submission?.rubricAssessmentsConnection?.pageInfo?.hasNextPage ?: false
            nextCursor = data.submission?.rubricAssessmentsConnection?.pageInfo?.endCursor
        }

        return data.copy(
            submission = data.submission?.copy(
                rubricAssessmentsConnection = data.submission?.rubricAssessmentsConnection?.copy(
                    edges = assessments
                )
            )
        )
    }
}