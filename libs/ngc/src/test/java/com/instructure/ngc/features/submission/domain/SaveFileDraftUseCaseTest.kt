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
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveFileDraftUseCaseTest {

    private val submissionUploadHelper: SubmissionUploadHelper = mockk()
    private val useCase = SaveFileDraftUseCase(submissionUploadHelper)

    private val canvasContext = CanvasContext.emptyCourseContext(id = 1L)
    private val assignmentId = 42L
    private val files = listOf(
        FileSubmitObject("essay.pdf", 1024L, "application/pdf", "/tmp/essay.pdf"),
        FileSubmitObject("notes.txt", 512L, "text/plain", "/tmp/notes.txt"),
    )

    @Test
    fun `invoke - delegates to saveDraftWithFiles with correct arguments`() = runTest {
        coEvery {
            submissionUploadHelper.saveDraftWithFiles(canvasContext, assignmentId, "Assignment", files)
        } returns 5L

        val result = useCase(
            SaveFileDraftUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = "Assignment",
                files = files,
            )
        )

        coVerify {
            submissionUploadHelper.saveDraftWithFiles(canvasContext, assignmentId, "Assignment", files)
        }
        assertEquals(5L, result)
    }

    @Test
    fun `invoke - forwards null assignmentName to helper`() = runTest {
        coEvery {
            submissionUploadHelper.saveDraftWithFiles(canvasContext, assignmentId, null, files)
        } returns 7L

        useCase(
            SaveFileDraftUseCase.Params(
                canvasContext = canvasContext,
                assignmentId = assignmentId,
                assignmentName = null,
                files = files,
            )
        )

        coVerify {
            submissionUploadHelper.saveDraftWithFiles(canvasContext, assignmentId, null, files)
        }
    }

    @Test
    fun `invoke - returns id from helper`() = runTest {
        coEvery { submissionUploadHelper.saveDraftWithFiles(any(), any(), any(), any()) } returns 99L

        val result = useCase(SaveFileDraftUseCase.Params(canvasContext, assignmentId, "Test", files))

        assertEquals(99L, result)
    }
}
