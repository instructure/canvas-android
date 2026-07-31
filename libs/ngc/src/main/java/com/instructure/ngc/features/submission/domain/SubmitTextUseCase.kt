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
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import javax.inject.Inject

class SubmitTextUseCase @Inject constructor(
    private val submissionUploadHelper: SubmissionUploadHelper
) : BaseUseCase<SubmitTextUseCase.Params, Unit>() {

    data class Params(
        val canvasContext: CanvasContext,
        val assignmentId: Long,
        val assignmentName: String?,
        val html: String,
        val attempt: Long?,
    )

    override suspend fun execute(params: Params) {
        submissionUploadHelper.startTextSubmission(
            canvasContext = params.canvasContext,
            assignmentId = params.assignmentId,
            assignmentName = params.assignmentName,
            text = params.html,
            attempt = params.attempt ?: 1L,
            deleteBySubmissionTypeFilter = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
        )
    }
}
