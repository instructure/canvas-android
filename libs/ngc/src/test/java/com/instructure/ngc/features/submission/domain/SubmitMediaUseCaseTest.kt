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
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SubmitMediaUseCaseTest {

    private val submissionUploadHelper: SubmissionUploadHelper = mockk()
    private val useCase = SubmitMediaUseCase(submissionUploadHelper)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)
    private val assignmentId = 42L

    @Test
    fun `invoke - delegates to startMediaSubmission with clip path and type`() = runTest {
        every {
            submissionUploadHelper.startMediaSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                assignmentGroupCategoryId = any(),
                mediaFilePath = any(),
                attempt = any(),
                mediaType = any(),
                deleteBySubmissionTypeFilter = any(),
            )
        } returns 5L

        useCase(
            SubmitMediaUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Test Assignment",
                mediaFilePath = "/tmp/clip.mp4",
                mediaType = "video",
            )
        )

        verify {
            submissionUploadHelper.startMediaSubmission(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Test Assignment",
                assignmentGroupCategoryId = 0,
                mediaFilePath = "/tmp/clip.mp4",
                attempt = 1L,
                mediaType = "video",
                deleteBySubmissionTypeFilter = Assignment.SubmissionType.MEDIA_RECORDING,
            )
        }
    }

    @Test
    fun `invoke - forwards a provided attempt instead of defaulting to 1`() = runTest {
        every {
            submissionUploadHelper.startMediaSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                assignmentGroupCategoryId = any(),
                mediaFilePath = any(),
                attempt = any(),
                mediaType = any(),
                deleteBySubmissionTypeFilter = any(),
            )
        } returns 1L

        useCase(
            SubmitMediaUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Test",
                mediaFilePath = "/tmp/clip.m4a",
                mediaType = "audio",
                attempt = 3L,
            )
        )

        verify {
            submissionUploadHelper.startMediaSubmission(
                canvasContext = any(),
                assignmentId = any(),
                assignmentName = any(),
                assignmentGroupCategoryId = any(),
                mediaFilePath = any(),
                attempt = 3L,
                mediaType = "audio",
                deleteBySubmissionTypeFilter = Assignment.SubmissionType.MEDIA_RECORDING,
            )
        }
    }
}
