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
 * Persists the in-progress comment text as a DRAFT pending-comment row for the page, replacing any
 * existing draft. Called (debounced) on every keystroke; inserts nothing when [Params.text] is blank.
 */
class SaveCommentDraftUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
) : BaseUseCase<SaveCommentDraftUseCase.Params, Unit>() {

    data class Params(val pageId: String, val text: String, val attemptId: Long?)

    override suspend fun execute(params: Params) {
        val currentDrafts = pendingSubmissionCommentDao.findByPageId(params.pageId)
            .orEmpty()
            .filter { it.pendingSubmissionCommentEntity.status == CommentSendStatus.DRAFT.toString() }
        pendingSubmissionCommentDao.deleteAll(currentDrafts.map { it.pendingSubmissionCommentEntity })

        if (params.text.isNotBlank()) {
            val newComment = PendingSubmissionComment(params.pageId, params.text).apply {
                attemptId = params.attemptId
            }
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        }
    }
}
