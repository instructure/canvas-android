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
package com.instructure.pandautils.domain.usecase.pages

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.pandautils.data.repository.page.PageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoadFrontPageUseCaseTest {

    private val pageRepository: PageRepository = mockk(relaxed = true)
    private lateinit var useCase: LoadFrontPageUseCase

    private val canvasContext: CanvasContext = Course(id = 1L)

    @Before
    fun setup() {
        useCase = LoadFrontPageUseCase(pageRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute returns the front page on success and passes params through`() = runTest {
        val page = Page(id = 1L, title = "Front Page")
        coEvery { pageRepository.getFrontPage(any(), any()) } returns DataResult.Success(page)

        val result = useCase(LoadFrontPageUseCase.Params(canvasContext, forceNetwork = false))

        assertEquals(DataResult.Success(page), result)
        coVerify { pageRepository.getFrontPage(canvasContext, false) }
    }

    @Test
    fun `execute defaults forceNetwork to false when not specified`() = runTest {
        coEvery { pageRepository.getFrontPage(any(), any()) } returns DataResult.Success(Page())

        useCase(LoadFrontPageUseCase.Params(canvasContext))

        coVerify { pageRepository.getFrontPage(canvasContext, false) }
    }

    @Test
    fun `execute returns the failure result as-is without throwing`() = runTest {
        val failure = DataResult.Fail(Failure.Network(errorCode = 404))
        coEvery { pageRepository.getFrontPage(any(), any()) } returns failure

        val result = useCase(LoadFrontPageUseCase.Params(canvasContext))

        assertEquals(failure, result)
    }
}
