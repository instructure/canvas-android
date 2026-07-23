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
import com.instructure.canvasapi2.models.postmodels.FileUploadWorkerData
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.daos.PendingSubmissionCommentDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import com.instructure.pandautils.room.appdatabase.entities.PendingSubmissionCommentEntity
import java.util.UUID
import javax.inject.Inject

/**
 * Ensures the DB state needed to render an in-flight file-upload comment while its [FileUploadWorker] is
 * RUNNING: a [FileUploadInputEntity] for the worker (created once per worker id) and a SENDING pending
 * comment row referencing it (also created once, since the RUNNING state may be observed more than once
 * for the same worker).
 */
class CreatePendingFileCommentUseCase @Inject constructor(
    private val pendingSubmissionCommentDao: PendingSubmissionCommentDao,
    private val fileUploadInputDao: FileUploadInputDao,
) : BaseUseCase<CreatePendingFileCommentUseCase.Params, Unit>() {

    data class Params(
        val workerId: String,
        val courseId: Long,
        val assignmentId: Long,
        val studentId: Long,
        val filePaths: List<String>,
        val pageId: String,
        val attemptId: Long?,
    )

    override suspend fun execute(params: Params) {
        var fileUploadInput = fileUploadInputDao.findByWorkerId(params.workerId)
        if (fileUploadInput == null) {
            fileUploadInput = FileUploadInputEntity(
                workerId = params.workerId,
                courseId = params.courseId,
                assignmentId = params.assignmentId,
                userId = params.studentId,
                filePaths = params.filePaths,
                action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                attemptId = params.attemptId
            )
            fileUploadInputDao.insert(fileUploadInput)
        }

        val comment = pendingSubmissionCommentDao.findByPageId(params.pageId)?.find {
            it.fileUploadInput?.workerId == params.workerId
        }
        if (comment == null) {
            val newComment = PendingSubmissionComment(params.pageId).apply {
                this.workerId = UUID.fromString(params.workerId)
                this.status = CommentSendStatus.SENDING
                this.workerInputData = FileUploadWorkerData(
                    params.filePaths,
                    params.courseId,
                    params.assignmentId,
                    params.studentId
                )
                this.attemptId = params.attemptId
            }
            pendingSubmissionCommentDao.insert(PendingSubmissionCommentEntity(newComment))
        }
    }
}
