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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.PageFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PageLocalDataSourceTest {

    private val pageFacade: PageFacade = mockk(relaxed = true)

    private lateinit var localDataSource: PageLocalDataSource

    @Before
    fun setup() {
        localDataSource = PageLocalDataSource(pageFacade)
    }

    @Test
    fun `Return front page data result`() = runTest {
        val expected = Page(id = 1, title = "Page")

        coEvery { pageFacade.getFrontPage(1) } returns expected

        val result = localDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            pageFacade.getFrontPage(1)
        }
    }

    @Test
    fun `Return failed data result if front page not found`() = runTest {
        coEvery { pageFacade.getFrontPage(1) } returns null

        val result = localDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            pageFacade.getFrontPage(1)
        }
    }

    @Test
    fun `Return page details data result`() = runTest {
        val expected = Page(id = 1, title = "Page")

        coEvery { pageFacade.getPageDetails(1, "id") } returns expected

        val result = localDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            pageFacade.getPageDetails(1, "id")
        }
    }

    @Test
    fun `Return failed data result if page details not found`() = runTest {
        coEvery { pageFacade.getPageDetails(1, "id") } returns null

        val result = localDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            pageFacade.getPageDetails(1, "id")
        }
    }
}
