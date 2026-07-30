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

import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import javax.inject.Inject

/**
 * Inserts the [FileUploadInputEntity] for a (ViewModel-created) restart worker, mirroring the input row
 * created for the original upload so the restarted [FileUploadWorker] can read its inputs.
 */
class InsertFileUploadInputUseCase @Inject constructor(
    private val fileUploadInputDao: FileUploadInputDao,
) : BaseUseCase<InsertFileUploadInputUseCase.Params, Unit>() {

    data class Params(
        val workerId: String,
        val filePaths: List<String>,
        val courseId: Long,
        val assignmentId: Long,
        val studentId: Long,
        val attemptId: Long?,
    )

    override suspend fun execute(params: Params) {
        fileUploadInputDao.insert(
            FileUploadInputEntity(
                workerId = params.workerId,
                filePaths = params.filePaths,
                courseId = params.courseId,
                assignmentId = params.assignmentId,
                userId = params.studentId,
                action = FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT,
                attemptId = params.attemptId
            )
        )
    }
}
