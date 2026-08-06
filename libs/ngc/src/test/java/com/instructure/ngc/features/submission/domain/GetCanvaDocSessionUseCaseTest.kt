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

import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.canvadocs.CanvaDocsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCanvaDocSessionUseCaseTest {

    private val canvaDocsRepository: CanvaDocsRepository = mockk()
    private val useCase = GetCanvaDocSessionUseCase(canvaDocsRepository)

    @Test
    fun `invoke - creates a draft session keyed by the given submission id`() = runTest {
        coEvery { canvaDocsRepository.createCanvaDocSession(555L, "draft") } returns DataResult.Success(
            CanvaDocSessionResponseBody(canvadocsSessionUrl = "https://canvadocs.example.com/session/abc")
        )

        val result = useCase(GetCanvaDocSessionUseCase.Params(submissionId = 555L))

        assertEquals("https://canvadocs.example.com/session/abc", result.sessionUrl)
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke - throws when no submission id is given`() = runTest {
        try {
            useCase(GetCanvaDocSessionUseCase.Params(submissionId = null))
        } finally {
            coVerify(exactly = 0) { canvaDocsRepository.createCanvaDocSession(any(), any()) }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke - throws when the response has no canvadocsSessionUrl`() = runTest {
        coEvery { canvaDocsRepository.createCanvaDocSession(555L, "draft") } returns DataResult.Success(
            CanvaDocSessionResponseBody(canvadocsSessionUrl = null)
        )

        useCase(GetCanvaDocSessionUseCase.Params(submissionId = 555L))
    }

    @Test(expected = IllegalStateException::class)
    fun `invoke - throws when the response has a blank canvadocsSessionUrl`() = runTest {
        coEvery { canvaDocsRepository.createCanvaDocSession(555L, "draft") } returns DataResult.Success(
            CanvaDocSessionResponseBody(canvadocsSessionUrl = "   ")
        )

        useCase(GetCanvaDocSessionUseCase.Params(submissionId = 555L))
    }
}
