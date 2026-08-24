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
package com.instructure.pandautils.domain.usecase.notes

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

class GetPageTitlesUseCaseTest {

    private val pageRepository: PageRepository = mockk(relaxed = true)
    private lateinit var useCase: GetPageTitlesUseCase

    @Before
    fun setup() {
        useCase = GetPageTitlesUseCase(pageRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute builds a page id to title map`() = runTest {
        val pages = listOf(Page(id = 1L, title = "Page One"), Page(id = 2L, title = "Page Two"))
        coEvery { pageRepository.getCoursePages(any(), any()) } returns DataResult.Success(pages)

        val result = useCase(GetPageTitlesUseCase.Params(courseId = 100L))

        assertEquals(mapOf("1" to "Page One", "2" to "Page Two"), result)
    }

    @Test
    fun `execute drops pages with a blank or null title`() = runTest {
        val pages = listOf(Page(id = 1L, title = "Page One"), Page(id = 2L, title = null), Page(id = 3L, title = "  "))
        coEvery { pageRepository.getCoursePages(any(), any()) } returns DataResult.Success(pages)

        val result = useCase(GetPageTitlesUseCase.Params(courseId = 100L))

        assertEquals(mapOf("1" to "Page One"), result)
    }

    @Test
    fun `execute drops pages with an unset id`() = runTest {
        val pages = listOf(Page(id = 0L, title = "No id"), Page(id = 5L, title = "Has id"))
        coEvery { pageRepository.getCoursePages(any(), any()) } returns DataResult.Success(pages)

        val result = useCase(GetPageTitlesUseCase.Params(courseId = 100L))

        assertEquals(mapOf("5" to "Has id"), result)
    }

    @Test
    fun `execute always reads cache-first from the repository`() = runTest {
        coEvery { pageRepository.getCoursePages(any(), any()) } returns DataResult.Success(emptyList())

        useCase(GetPageTitlesUseCase.Params(courseId = 100L))

        coVerify { pageRepository.getCoursePages(100L, forceRefresh = false) }
    }

    @Test(expected = IllegalStateException::class)
    fun `execute throws when the repository fails`() = runTest {
        coEvery { pageRepository.getCoursePages(any(), any()) } returns DataResult.Fail(Failure.Network())

        useCase(GetPageTitlesUseCase.Params(courseId = 100L))
    }
}
