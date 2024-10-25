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

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.pages.details.datasource.PageDetailsNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PageDetailsNetworkDataSourceTest {

    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)

    private lateinit var networkDataSource: PageDetailsNetworkDataSource

    @Before
    fun setup() {
        networkDataSource = PageDetailsNetworkDataSource(pageApi)
    }

    @Test
    fun `Return front page data result`() = runTest {
        val expected = DataResult.Success(Page(id = 1, title = "Page"))

        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns expected

        val result = networkDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            pageApi.getFrontPage(CanvasContext.emptyCourseContext(1).apiContext(), 1, RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if front page call fails`() = runTest {
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return page details data result`() = runTest {
        val expected = DataResult.Success(Page(id = 1, title = "Page"))

        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns expected

        val result = networkDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        TestCase.assertEquals(expected, result)
        coVerify(exactly = 1) {
            pageApi.getDetailedPage(CanvasContext.emptyCourseContext(1).apiContext(), 1, "id", RestParams(isForceReadFromNetwork = true))
        }
    }

    @Test
    fun `Return failed data result if page details call fails`() = runTest {
        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = networkDataSource.getPageDetails(CanvasContext.emptyCourseContext(1), "id", true)

        TestCase.assertEquals(DataResult.Fail(), result)
    }
}
