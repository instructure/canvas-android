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
import com.instructure.canvasapi2.managers.graphql.horizon.CommentsData
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.datasource.AssignmentCommentsLocalDataSource
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class CommentsRepository @Inject constructor(
    private val getCommentsManager: HorizonGetCommentsManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val localDataSource: AssignmentCommentsLocalDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getComments(
        assignmentId: Long,
        userId: Long,
        attempt: Int,
        forceNetwork: Boolean,
        startCursor: String? = null,
        endCursor: String? = null,
        nextPage: Boolean = false
    ): CommentsData {
        return if (shouldFetchFromNetwork()) {
            val commentsData = getCommentsManager.getComments(
                assignmentId = assignmentId,
                userId = userId,
                attempt = attempt,
                forceNetwork = forceNetwork,
                startCursor = startCursor,
                endCursor = endCursor,
                nextPage = nextPage
            )
            if (shouldSync() && startCursor == null && endCursor == null && !nextPage) {
                localDataSource.saveComments(assignmentId, attempt, commentsData)
            }
            commentsData
        } else {
            localDataSource.getComments(assignmentId, attempt)
        }
    }

    suspend fun getUnreadCommentCount(assignmentId: Long, userId: Long, forceNetwork: Boolean): Int {
        return if (shouldFetchFromNetwork()) {
            getCommentsManager.getUnreadCommentsCount(assignmentId, userId, forceNetwork)
        } else {
            localDataSource.getUnreadCommentCount(assignmentId)
        }
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