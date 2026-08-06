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

package com.instructure.pandautils.data.repository.page

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PageNetworkDataSourceTest {

    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)

    private lateinit var networkDataSource: PageNetworkDataSource

    @Before
    fun setup() {
        networkDataSource = PageNetworkDataSource(pageApi)
    }

    @Test
    fun `Return front page data result`() = runTest {
        val expected = DataResult.Success(Page(id = 1, title = "Page"))

        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns expected

        val result = networkDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        assertEquals(expected, result)
        coVerify(exactly = 1) {
            pageApi.getFrontPage(CanvasContext.emptyCourseContext(1).apiContext(), 1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if front page call fails`() = runTest {
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return page details data result`() = runTest {
        val expected = DataResult.Success(Page(id = 1, title = "Page"))

        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns expected

        val result = networkDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        assertEquals(expected, result)
        coVerify(exactly = 1) {
            pageApi.getDetailedPage(CanvasContext.emptyCourseContext(1).apiContext(), 1, "id", RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if page details call fails`() = runTest {
        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        assertEquals(DataResult.Fail(), result)
    }
}
