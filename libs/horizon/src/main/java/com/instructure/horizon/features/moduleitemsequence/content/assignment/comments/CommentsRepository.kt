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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CommentsData
import com.instructure.canvasapi2.managers.HorizonGetCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import javax.inject.Inject

class CommentsRepository @Inject constructor(
    private val getCommentsManager: HorizonGetCommentsManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) {
    suspend fun getComments(
        assignmentId: Long,
        userId: Long,
        attempt: Int,
        forceNetwork: Boolean,
        startCursor: String? = null,
        endCursor: String? = null,
        nextPage: Boolean = false
    ): CommentsData {
        return getCommentsManager.getComments(
            assignmentId = assignmentId,
            userId = userId,
            attempt = attempt,
            forceNetwork = forceNetwork,
            startCursor = startCursor,
            endCursor = endCursor,
            nextPage = nextPage
        )
    }

    suspend fun postComment(
        courseId: Long,
        assignmentId: Long,
        userId: Long,
        attempt: Int,
        commentText: String
    ): DataResult<Submission> {
        return submissionApi.postSubmissionComment(
            courseId,
            assignmentId,
            userId,
            commentText,
            attempt.toLong(),
            false,
            emptyList(),
            RestParams()
        )
    }
}