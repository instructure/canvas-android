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
 * Resolves a FAILED file-upload worker by marking its pending comment row ERROR so it renders as a
 * retryable/failed comment instead of staying stuck on the pending spinner.
 */
class MarkFileCommentFailedUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
) : BaseUseCase<MarkFileCommentFailedUseCase.Params, Unit>() {

    data class Params(val workerId: String)

    override suspend fun execute(params: Params) {
        pendingSubmissionCommentDao.findByWorkerIdWithInputData(params.workerId)?.apply {
            this.pendingSubmissionCommentEntity.status = CommentSendStatus.ERROR.toString()
        }?.let {
            pendingSubmissionCommentDao.update(it.pendingSubmissionCommentEntity)
        }
    }
}
