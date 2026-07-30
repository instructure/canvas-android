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
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import javax.inject.Inject

/**
 * Marks a pending comment row ERROR by id, so it renders as a retryable/failed comment instead of
 * staying stuck on the pending spinner. This is the by-id sibling of [MarkFileCommentFailedUseCase],
 * used by the text-send and media-recording FAILED paths.
 */
class MarkPendingCommentFailedUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
) : BaseUseCase<MarkPendingCommentFailedUseCase.Params, Unit>() {

    data class Params(val commentId: Long)

    override suspend fun execute(params: Params) {
        pendingSubmissionCommentDao.findById(params.commentId)?.let {
            it.status = CommentSendStatus.ERROR.toString()
            pendingSubmissionCommentDao.update(it)
        }
    }
}
