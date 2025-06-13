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
import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.SubmissionCommentsQuery

class SubmissionCommentsManagerImpl : SubmissionCommentsManager {

    override suspend fun getSubmissionComments(submissionId: Long): SubmissionCommentsQuery.Data {
        val query = SubmissionCommentsQuery(submissionId.toString())
        val result = QLClientConfig.enqueueQuery(query)
        return result.dataAssertNoErrors
    }

    override suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int?,
        isGroupComment: Boolean
    ): CreateSubmissionCommentMutation.Data {
        val mutation = CreateSubmissionCommentMutation(
            submissionId.toString(),
            comment,
            Optional.Present(attempt),
            Optional.Present(isGroupComment)
        )
        val result = QLClientConfig.enqueueMutation(mutation)
        return result.dataAssertNoErrors
    }
}
