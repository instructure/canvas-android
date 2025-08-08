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

import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionGradeMutation
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation

class SubmissionGradeManagerImpl : SubmissionGradeManager {

    override suspend fun getSubmissionGrade(
        assignmentId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ): SubmissionGradeQuery.Data {
        var hasNextPage = true
        var nextCursor: String? = null

        var submission: SubmissionGradeQuery.Data? = null

        while (hasNextPage) {
            val nextCursorParam =
                if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
            val query =
                SubmissionGradeQuery(studentId.toString(), assignmentId.toString(), nextCursorParam)
            val data = QLClientConfig.enqueueQuery(query, forceNetwork = true).dataAssertNoErrors
            if (submission == null) {
                submission = data
            } else {
                submission = submission.copy(
                    submission = submission.submission?.copy(
                        assignment = submission.submission?.assignment?.copy(
                            course = submission.submission?.assignment?.course?.copy(
                                customGradeStatusesConnection = submission.submission?.assignment?.course?.customGradeStatusesConnection?.copy(
                                    edges = submission.submission?.assignment?.course?.customGradeStatusesConnection?.edges.orEmpty() +
                                            data.submission?.assignment?.course?.customGradeStatusesConnection?.edges.orEmpty()
                                )
                            )
                        )
                    )
                )
            }

            hasNextPage = data.submission?.assignment?.course?.customGradeStatusesConnection?.pageInfo?.hasNextPage ?: false
            nextCursor = data.submission?.assignment?.course?.customGradeStatusesConnection?.pageInfo?.endCursor
        }

        return submission ?: throw IllegalStateException("No submission data found")
    }

    override suspend fun updateSubmissionGrade(
        score: Double,
        submissionId: Long
    ): UpdateSubmissionGradeMutation.Data {
        val mutation = UpdateSubmissionGradeMutation(score.toInt(), submissionId.toString())
        val result = QLClientConfig.enqueueMutation(mutation)
        return result.dataAssertNoErrors
    }

    override suspend fun updateSubmissionStatus(
        submissionId: Long,
        customGradeStatusId: String?,
        latePolicyStatus: String?
    ): UpdateSubmissionStatusMutation.Data {
        val statusId = if (customGradeStatusId != null) Optional.present(customGradeStatusId) else Optional.absent()
        val status = if (latePolicyStatus != null) Optional.present(latePolicyStatus) else Optional.absent()

        val mutation = UpdateSubmissionStatusMutation(
            submissionId.toString(),
            statusId,
            status
        )
        val result = QLClientConfig.enqueueMutation(mutation)
        return result.dataAssertNoErrors
    }
}