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
package com.instructure.pandautils.data.repository.submissioncomments

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.MarkSubmissionCommentsReadMutation
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper

class SubmissionCommentsRepositoryImpl(
    private val submissionCommentsManager: SubmissionCommentsManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) : SubmissionCommentsRepository {

    override suspend fun getSubmissionComments(
        userId: Long,
        assignmentId: Long,
        forceNetwork: Boolean
    ): SubmissionCommentsResponseWrapper {
        return submissionCommentsManager.getSubmissionComments(userId, assignmentId, forceNetwork)
    }

    override suspend fun getSingleSubmission(
        courseId: Long,
        assignmentId: Long,
        studentId: Long
    ): Submission? {
        return submissionApi.getSingleSubmission(courseId, assignmentId, studentId, RestParams()).dataOrNull
    }

    override suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int?,
        isGroupComment: Boolean
    ): CreateSubmissionCommentMutation.Data {
        return submissionCommentsManager.createSubmissionComment(submissionId, comment, attempt, isGroupComment)
    }

    override suspend fun markSubmissionCommentsRead(
        submissionId: Long,
        submissionCommentIds: List<Long>
    ): MarkSubmissionCommentsReadMutation.Data {
        return submissionCommentsManager.markSubmissionCommentsRead(submissionId, submissionCommentIds)
    }
}
