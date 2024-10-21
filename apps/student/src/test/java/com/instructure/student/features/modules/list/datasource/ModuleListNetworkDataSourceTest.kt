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
package com.instructure.student.features.modules.list.datasource

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ModuleListNetworkDataSourceTest {

    private val moduleApi: ModuleAPI.ModuleInterface = mockk(relaxed = true)
    private val tabApi: TabAPI.TabsInterface = mockk(relaxed = true)
    private val courseApi: CourseAPI.CoursesInterface = mockk(relaxed = true)

    private val dataSource = ModuleListNetworkDataSource(moduleApi, tabApi, courseApi)

    @Test
    fun `Return failed result when getAllModuleObjects fails`() = runTest {
        coEvery { moduleApi.getFirstPageModuleObjects(any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.getAllModuleObjects(Course(1), true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result with all pages when from getAllModuleObjects`() = runTest {
        val firstPageModules = listOf(ModuleObject(id = 1))
        val secondPageModules = listOf(ModuleObject(id = 2))
        coEvery { moduleApi.getFirstPageModuleObjects(any(), any(), any()) } returns DataResult.Success(firstPageModules, linkHeaders = LinkHeaders(nextUrl = "next"))
        coEvery { moduleApi.getNextPageModuleObjectList(any(), any()) } returns DataResult.Success(secondPageModules)

        val result = dataSource.getAllModuleObjects(Course(1), true)

        Assert.assertEquals(2, (result as DataResult.Success).data.size)
        Assert.assertEquals(firstPageModules.first(), result.data[0])
        Assert.assertEquals(secondPageModules.first(), result.data[1])
    }

    @Test
    fun `Return failed result when getFirstPageModuleObjects fails`() = runTest {
        coEvery { moduleApi.getFirstPageModuleObjects(any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.getFirstPageModuleObjects(Course(1), true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result from getFirstPageModuleObjects`() = runTest {
        val firstPageModules = listOf(ModuleObject(id = 1))
        coEvery { moduleApi.getFirstPageModuleObjects(any(), any(), any()) } returns DataResult.Success(firstPageModules)

        val result = dataSource.getFirstPageModuleObjects(Course(1), true)

        Assert.assertEquals(1, (result as DataResult.Success).data.size)
        Assert.assertEquals(firstPageModules.first(), result.data[0])
    }

    @Test
    fun `Return failed result when getNextPageModuleObjects fails`() = runTest {
        coEvery { moduleApi.getNextPageModuleObjectList(any(), any()) } returns DataResult.Fail()

        val result = dataSource.getNextPageModuleObjects("url", true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result with all pages when from getNextPageModuleObjects`() = runTest {
        val secondPageModules = listOf(ModuleObject(id = 2))
        coEvery { moduleApi.getNextPageModuleObjectList(any(), any()) } returns DataResult.Success(secondPageModules)

        val result = dataSource.getNextPageModuleObjects("url", true)

        Assert.assertEquals(1, (result as DataResult.Success).data.size)
        Assert.assertEquals(secondPageModules.first(), result.data[0])
    }

    @Test
    fun `Return failed result when getFirstPageModuleItems fails`() = runTest {
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.getFirstPageModuleItems(Course(1), 1, true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result from getFirstPageModuleItems`() = runTest {
        val firstPageModuleItems = listOf(ModuleItem(id = 1))
        coEvery { moduleApi.getFirstPageModuleItems(any(), any(), any(), any()) } returns DataResult.Success(firstPageModuleItems)

        val result = dataSource.getFirstPageModuleItems(Course(1), 1, true)

        Assert.assertEquals(1, (result as DataResult.Success).data.size)
        Assert.assertEquals(firstPageModuleItems.first(), result.data[0])
    }

    @Test
    fun `Return failed result when getNextPageModuleItems fails`() = runTest {
        coEvery { moduleApi.getNextPageModuleItemList(any(), any()) } returns DataResult.Fail()

        val result = dataSource.getNextPageModuleItems("url", true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result with all pages when from getNextPageModuleItems`() = runTest {
        val secondPageModuleItems = listOf(ModuleItem(id = 2))
        coEvery { moduleApi.getNextPageModuleItemList(any(), any()) } returns DataResult.Success(secondPageModuleItems)

        val result = dataSource.getNextPageModuleItems("url", true)

        Assert.assertEquals(1, (result as DataResult.Success).data.size)
        Assert.assertEquals(secondPageModuleItems.first(), result.data[0])
    }

    @Test
    fun `Return failed result when getTabs fails`() = runTest {
        coEvery { tabApi.getTabs(any(), any(), any()) } returns DataResult.Fail()

        val result = dataSource.getTabs(Course(1), true)

        Assert.assertEquals(DataResult.Fail(), result)
    }

    @Test
    fun `Return successful result from getTabs`() = runTest {
        val tabs = listOf(Tab(tabId = "modules"), Tab(tabId = "grades"))
        coEvery { tabApi.getTabs(any(), any(), any()) } returns DataResult.Success(tabs)

        val result = dataSource.getTabs(Course(1), true)

        Assert.assertEquals(2, (result as DataResult.Success).data.size)
        Assert.assertEquals(tabs.first(), result.data[0])
        Assert.assertEquals(tabs[1], result.data[1])
    }

    @Test
    fun `Load course settings returns succesful api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Success(expected)

        val result = dataSource.loadCourseSettings(1, true)

        Assert.assertEquals(expected, result)
    }

    @Test
    fun `Load course settings failure returns null`() = runTest {
        coEvery { courseApi.getCourseSettings(any(), any()) } returns DataResult.Fail()

        val result = dataSource.loadCourseSettings(1, true)

        Assert.assertNull(result)
    }
}