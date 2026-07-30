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
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SubmitFilesUseCaseTest {

    private val submissionUploadHelper: SubmissionUploadHelper = mockk()
    private val useCase = SubmitFilesUseCase(submissionUploadHelper)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)
    private val assignmentId = 42L
    private val files = listOf(
        FileSubmitObject("essay.pdf", 1024L, "application/pdf", "/tmp/essay.pdf"),
    )

    @Test
    fun `invoke - calls startFileSubmission with ONLINE_UPLOAD as deleteBySubmissionTypeFilter`() = runTest {
        every {
            submissionUploadHelper.startFileSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                assignmentGroupCategoryId = any(),
                files = any(),
                deleteBySubmissionTypeFilter = any(),
                attempt = any(),
            )
        } returns 5L

        useCase(
            SubmitFilesUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Test Assignment",
                files = files,
                attempt = 1L,
            )
        )

        verify {
            submissionUploadHelper.startFileSubmission(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Test Assignment",
                assignmentGroupCategoryId = any(),
                files = ArrayList(files),
                deleteBySubmissionTypeFilter = Assignment.SubmissionType.ONLINE_UPLOAD,
                attempt = 1L,
            )
        }
    }

    @Test
    fun `invoke - passes files as ArrayList to startFileSubmission`() = runTest {
        every { submissionUploadHelper.startFileSubmission(any(), any(), any(), any(), any(), any(), any()) } returns 1L

        useCase(SubmitFilesUseCase.Params(canvasContext, assignmentId, "Test", files, attempt = 3L))

        verify {
            submissionUploadHelper.startFileSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                assignmentGroupCategoryId = any(),
                files = match { it.containsAll(files) },
                deleteBySubmissionTypeFilter = any(),
                attempt = 3L,
            )
        }
    }
}
