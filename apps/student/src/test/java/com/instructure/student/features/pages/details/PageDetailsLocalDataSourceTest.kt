/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.features.pages.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.student.features.pages.details.datasource.PageDetailsLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PageDetailsLocalDataSourceTest {

    private val pageFacade: PageFacade = mockk(relaxed = true)

    private lateinit var localDataSource: PageDetailsLocalDataSource

    @Before
    fun setup() {
        localDataSource = PageDetailsLocalDataSource(pageFacade)
    }

    @Test
    fun `Return front page data result`() = runTest {
        val expected = Page(id = 1, title = "Page")

        coEvery { pageFacade.getFrontPage(1) } returns expected

        val result = localDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            pageFacade.getFrontPage(1)
        }
    }

    @Test
    fun `Return failed data result if front page not found`() = runTest {
        coEvery { pageFacade.getFrontPage(1) } returns null

        val result = localDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            pageFacade.getFrontPage(1)
        }
    }

    @Test
    fun `Return page details data result`() = runTest {
        val expected = Page(id = 1, title = "Page")

        coEvery { pageFacade.getPageDetails(1, "id") } returns expected

        val result = localDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        TestCase.assertEquals(DataResult.Success(expected), result)
        coVerify(exactly = 1) {
            pageFacade.getPageDetails(1, "id")
        }
    }

    @Test
    fun `Return failed data result if page details not found`() = runTest {
        coEvery { pageFacade.getPageDetails(1, "id") } returns null

        val result = localDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        TestCase.assertEquals(DataResult.Fail(), result)
        coVerify(exactly = 1) {
            pageFacade.getPageDetails(1, "id")
        }
    }
}
