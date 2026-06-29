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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import javax.inject.Inject

class SubmitTextUseCase @Inject constructor(
    private val submissionUploadHelper: SubmissionUploadHelper
) {
    suspend operator fun invoke(
        canvasContext: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        html: String,
        attempt: Long?
    ) {
        submissionUploadHelper.startTextSubmission(
            canvasContext = canvasContext,
            assignmentId = assignmentId,
            assignmentName = assignmentName,
            text = html,
            attempt = attempt ?: 1L
        )
    }
}
