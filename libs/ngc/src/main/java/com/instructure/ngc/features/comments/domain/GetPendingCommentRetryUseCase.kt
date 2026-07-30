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

import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Loads the data the ViewModel needs to branch a retry (text / file-upload / media) without touching
 * entities directly. Returns null when no pending row matches [Params.commentId].
 */
class GetPendingCommentRetryUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val fileUploadInputDao: FileUploadInputDao,
) : BaseUseCase<GetPendingCommentRetryUseCase.Params, PendingCommentRetryData?>() {

    data class Params(val commentId: Long)

    override suspend fun execute(params: Params): PendingCommentRetryData? {
        val pending = pendingSubmissionCommentDao.findById(params.commentId) ?: return null
        val workerId = if (pending.workerId != null && pending.workerId != "null") pending.workerId else null
        val filePaths = workerId?.let { fileUploadInputDao.findByWorkerId(it)?.filePaths }

        return PendingCommentRetryData(
            comment = pending.comment,
            workerId = workerId,
            filePaths = filePaths,
            filePath = pending.filePath
        )
    }
}
