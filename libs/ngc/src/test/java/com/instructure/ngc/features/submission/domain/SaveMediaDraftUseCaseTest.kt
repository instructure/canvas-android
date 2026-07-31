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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveMediaDraftUseCaseTest {

    private val submissionUploadHelper: SubmissionUploadHelper = mockk()
    private val useCase = SaveMediaDraftUseCase(submissionUploadHelper)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)
    private val assignmentId = 42L
    private val clip = FileSubmitObject("clip.mp4", 4096L, "video/mp4", "/tmp/clip.mp4")

    @Test
    fun `invoke - delegates to saveDraftWithFiles with the clip and MEDIA_RECORDING type`() = runTest {
        coEvery {
            submissionUploadHelper.saveDraftWithFiles(
                canvasContext,
                assignmentId,
                "Assignment",
                listOf(clip),
                Assignment.SubmissionType.MEDIA_RECORDING,
            )
        } returns 5L

        val result = useCase(
            SaveMediaDraftUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Assignment",
                clip = clip,
            )
        )

        coVerify {
            submissionUploadHelper.saveDraftWithFiles(
                canvasContext,
                assignmentId,
                "Assignment",
                listOf(clip),
                Assignment.SubmissionType.MEDIA_RECORDING,
            )
        }
        assertEquals(5L, result)
    }

    @Test
    fun `invoke - forwards null assignmentName to helper`() = runTest {
        coEvery {
            submissionUploadHelper.saveDraftWithFiles(
                canvasContext,
                assignmentId,
                null,
                listOf(clip),
                Assignment.SubmissionType.MEDIA_RECORDING,
            )
        } returns 7L

        useCase(
            SaveMediaDraftUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = null,
                clip = clip,
            )
        )

        coVerify {
            submissionUploadHelper.saveDraftWithFiles(
                canvasContext,
                assignmentId,
                null,
                listOf(clip),
                Assignment.SubmissionType.MEDIA_RECORDING,
            )
        }
    }
}
