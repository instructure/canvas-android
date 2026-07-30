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
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper

/**
 * Online-only repository wrapping a single GraphQL manager ([com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager])
 * responsible solely for submission comments data (fetching, posting, marking read).
 */
interface SubmissionCommentsRepository {

    suspend fun getSubmissionComments(
        userId: Long,
        assignmentId: Long,
        forceNetwork: Boolean = false
    ): SubmissionCommentsResponseWrapper

    /**
     * Fetches the student's submission via REST (with `submission_comments` included). Used to resolve
     * comment attachments to their verifier-authenticated download URLs — the GraphQL comment query only
     * returns unverified file URLs, which can't be previewed/downloaded on their own.
     */
    suspend fun getSingleSubmission(
        courseId: Long,
        assignmentId: Long,
        studentId: Long
    ): Submission?

    suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int? = null,
        isGroupComment: Boolean = false
    ): CreateSubmissionCommentMutation.Data

    suspend fun markSubmissionCommentsRead(
        submissionId: Long,
        submissionCommentIds: List<Long>
    ): MarkSubmissionCommentsReadMutation.Data
}
