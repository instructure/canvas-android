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
package com.instructure.ngc.features.comments.domain

import com.instructure.canvasapi2.models.postmodels.CommentSendStatus
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes the pending (draft + in-flight) submission comments for a page. Partitions them into the
 * current draft text (if any) and the pending [SubmissionCommentItem]s to render alongside sent comments.
 */
class ObservePendingSubmissionCommentsUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val apiPrefs: ApiPrefs,
) : BaseFlowUseCase<ObservePendingSubmissionCommentsUseCase.Params, PendingSubmissionCommentsData>() {

    data class Params(val pageId: String)

    override fun execute(params: Params): Flow<PendingSubmissionCommentsData> {
        return pendingSubmissionCommentDao.findByPageIdFlow(params.pageId).map { pendingCommentsEntities ->
            val (drafts, pending) = pendingCommentsEntities
                .orEmpty()
                .map { it.pendingSubmissionCommentEntity.toApiModel() }
                .partition { it.status == CommentSendStatus.DRAFT }

            val pendingComments = pending.map { pendingComment ->
                SubmissionCommentItem(
                    id = pendingComment.id.toString(),
                    authorName = apiPrefs.user?.name.orEmpty(),
                    authorId = apiPrefs.user?.id?.toString().orEmpty(),
                    authorAvatarUrl = apiPrefs.user?.avatarUrl.orEmpty(),
                    content = pendingComment.comment.orEmpty(),
                    createdAt = pendingComment.date,
                    isOwnComment = true,
                    isPending = pendingComment.status == CommentSendStatus.SENDING,
                    isFailed = pendingComment.status == CommentSendStatus.ERROR,
                )
            }

            PendingSubmissionCommentsData(
                draftText = drafts.firstOrNull()?.comment,
                pendingComments = pendingComments,
            )
        }
    }
}
