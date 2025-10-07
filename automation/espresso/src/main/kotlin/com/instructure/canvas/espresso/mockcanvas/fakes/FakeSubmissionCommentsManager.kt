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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.SubmissionCommentsQuery
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper

class FakeSubmissionCommentsManager : SubmissionCommentsManager {
    override suspend fun getSubmissionComments(
        userId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): SubmissionCommentsResponseWrapper {
        return SubmissionCommentsResponseWrapper(
            comments = emptyList(),
            data = SubmissionCommentsQuery.Data(
                submission = null
            )
        )
    }

    override suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int?,
        isGroupComment: Boolean
    ): CreateSubmissionCommentMutation.Data {
        return CreateSubmissionCommentMutation.Data(null)
    }
}