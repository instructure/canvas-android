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
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import javax.inject.Inject

/**
 * Replaces any existing draft for the page with a new pending text comment, inserted already in the
 * SENDING state so it renders as in-flight until [SendTextCommentUseCase] resolves it. Returns the new
 * pending row's id.
 */
class CreatePendingTextCommentUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
) : BaseUseCase<CreatePendingTextCommentUseCase.Params, Long>() {

    data class Params(val pageId: String, val comment: String, val attemptId: Long?)

    override suspend fun execute(params: Params): Long {
        val drafts = pendingSubmissionCommentDao.findByPageId(params.pageId)
            .orEmpty()
            .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }
        pendingSubmissionCommentDao.deleteAll(drafts.map { it.pendingSubmissionCommentEntity })

        val newComment = PendingSubmissionComment(params.pageId, params.comment).apply {
            attemptId = params.attemptId
            status = CommentSendStatus.SENDING
        }
        pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        return newComment.id
    }
}
