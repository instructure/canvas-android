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
 * Creates the optimistic SENDING pending comment row for a media (audio/video) recording that is about
 * to be uploaded, so it can be rendered as pending while the [com.instructure.pandautils.services.NotoriousUploadWorker]
 * upload is in flight. Returns the inserted row's id.
 */
class CreatePendingMediaCommentUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
) : BaseUseCase<CreatePendingMediaCommentUseCase.Params, Long>() {

    data class Params(val pageId: String, val filePath: String, val attemptId: Long?)

    override suspend fun execute(params: Params): Long {
        val newComment = PendingSubmissionComment(params.pageId).apply {
            this.filePath = params.filePath
            status = CommentSendStatus.SENDING
            attemptId = params.attemptId
        }
        return pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
    }
}
