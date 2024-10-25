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
package com.instructure.student.features.offline.coursebrowser

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class CourseBrowserNetworkDataSourceTest {

    private val tabApi: TabAPI.TabsInterface = mockk(relaxed = true)
    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)

    private val dataSource = CourseBrowserNetworkDataSource(tabApi, pageApi)

    @Test
    fun `Get tabs successfully returns data`() = runTest {
        coEvery { tabApi.getTabs(any(), any(), any()) } returns DataResult.Success(listOf(Tab(label = "Tab")))

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), true)

        Assert.assertEquals(listOf(Tab(label = "Tab")), tabs)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get tabs failure throws exception`() = runTest {
        coEvery { tabApi.getTabs(any(), any(), any()) } returns DataResult.Fail()

        dataSource.getTabs(CanvasContext.emptyCourseContext(1), true)
    }

    @Test
    fun `Get front page successfully returns data`() = runTest {
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Success(Page(title = "Front page"))

        val frontPage = dataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        Assert.assertEquals(Page(title = "Front page"), frontPage)
    }

    @Test
    fun `Get front page failure returns null`() = runTest {
        coEvery { pageApi.getFrontPage(any(), any(), any()) } returns DataResult.Fail()

        val frontPage = dataSource.getFrontPage(CanvasContext.emptyCourseContext(1), true)

        Assert.assertNull(frontPage)
    }
}