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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SubmitStudentAnnotationUseCaseTest {

    private val submissionUploadHelper: SubmissionUploadHelper = mockk()
    private val useCase = SubmitStudentAnnotationUseCase(submissionUploadHelper)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)
    private val assignmentId = 42L

    @Test
    fun `invoke - calls startStudentAnnotationSubmission with the given annotatableAttachmentId`() = runTest {
        every {
            submissionUploadHelper.startStudentAnnotationSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                annotatableAttachmentId = any(),
                attempt = any(),
            )
        } returns 5L

        useCase(
            SubmitStudentAnnotationUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Essay",
                annotatableAttachmentId = 123L,
                attempt = 2L,
            )
        )

        verify {
            submissionUploadHelper.startStudentAnnotationSubmission(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Essay",
                annotatableAttachmentId = 123L,
                attempt = 2L,
            )
        }
    }

    @Test
    fun `invoke - defaults attempt to 1 when null`() = runTest {
        every {
            submissionUploadHelper.startStudentAnnotationSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                annotatableAttachmentId = any(),
                attempt = any(),
            )
        } returns 1L

        useCase(
            SubmitStudentAnnotationUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = null,
                annotatableAttachmentId = 456L,
                attempt = null,
            )
        )

        verify {
            submissionUploadHelper.startStudentAnnotationSubmission(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = null,
                annotatableAttachmentId = 456L,
                attempt = 1L,
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke - throws and never calls the helper when annotatableAttachmentId is 0`() = runTest {
        try {
            useCase(
                SubmitStudentAnnotationUseCase.Params(
                    canvasContext = canvasContext,
                    assignmentId = assignmentId,
                    assignmentName = "Essay",
                    annotatableAttachmentId = 0L,
                    attempt = 2L,
                )
            )
        } finally {
            verify(exactly = 0) {
                submissionUploadHelper.startStudentAnnotationSubmission(
                    canvasContext = any(),
                    assignmentId = any(),
                    assignmentName = any(),
                    annotatableAttachmentId = any(),
                    attempt = any(),
                )
            }
        }
    }
}
