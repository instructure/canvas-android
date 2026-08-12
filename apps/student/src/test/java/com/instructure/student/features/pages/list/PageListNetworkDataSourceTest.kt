/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.pages.list

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.pages.list.datasource.PageListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PageListNetworkDataSourceTest {

    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)

    private val dataSource = PageListNetworkDataSource(pageApi)

    @Before
    fun setup() = runTest {
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Fail()
    }

    @Test
    fun `Return pages successfully`() = runTest {
        val expected = listOf(
            Page(
                id = 1L,
                title = "Page 1"
            ), Page(
                id = 2L,
                title = "Page 2"
            )
        )
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get pages error throws exception`() = runTest {
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Fail()

        dataSource.loadPages(CanvasContext.defaultCanvasContext(), true)
    }

    @Test
    fun `Do not request front page when the list already has one`() = runTest {
        val expected = listOf(Page(id = 1L, title = "Page 1", frontPage = true), Page(id = 2L, title = "Page 2"))
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
        coVerify(exactly = 0) { pageApi.getFrontPage(any(), any(), any()) }
    }

    @Test
    fun `Flag the front page when the list returns it without the front page flag`() = runTest {
        val pages = listOf(Page(id = 1L, title = "Page 1"), Page(id = 2L, title = "Page 2"))
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(pages)
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Success(
            Page(id = 2L, title = "Page 2", frontPage = true, body = "Body")
        )

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(listOf(Page(id = 1L, title = "Page 1"), Page(id = 2L, title = "Page 2", frontPage = true)), result)
    }

    @Test
    fun `Add the front page when the list omits it entirely`() = runTest {
        val pages = listOf(Page(id = 1L, title = "Page 1"))
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(pages)
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Success(
            Page(id = 2L, title = "Page 2", frontPage = true, body = "Body")
        )

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(listOf(Page(id = 1L, title = "Page 1"), Page(id = 2L, title = "Page 2", frontPage = true)), result)
    }

    @Test
    fun `Keep the list unchanged when the course has no front page`() = runTest {
        val expected = listOf(Page(id = 1L, title = "Page 1"))
        coEvery { pageApi.getFirstPagePages(any(), any(), any()) } returns DataResult.Success(expected)
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadPages(CanvasContext.defaultCanvasContext(), false)

        assertEquals(expected, result)
    }
}