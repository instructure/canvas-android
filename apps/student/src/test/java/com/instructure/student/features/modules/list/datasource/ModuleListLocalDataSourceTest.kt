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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class ModuleListLocalDataSourceTest {

    private val tabDao = mockk<TabDao>(relaxed = true)
    private val moduleFacade = mockk<ModuleFacade>(relaxed = true)
    private val courseSettingsDao: CourseSettingsDao = mockk(relaxed = true)

    private val dataSource = ModuleListLocalDataSource(tabDao, moduleFacade, courseSettingsDao)

    @Test
    fun `getAllModuleObjects returns all module objects with DB api type`() = runTest {
        val moduleObjects = listOf(ModuleObject(1, 1, "Module 1"), ModuleObject(2, 1, "Module 2"))
        coEvery { moduleFacade.getModuleObjects(any()) } returns moduleObjects

        val result = dataSource.getAllModuleObjects(Course(1), true)

        Assert.assertEquals(ApiType.DB, (result as DataResult.Success).apiType)
        Assert.assertEquals(moduleObjects, result.dataOrNull)
    }

    @Test
    fun `getFirstPageModuleObjects returns all module objects with DB api type`() = runTest {
        val moduleObjects = listOf(ModuleObject(1, 1, "Module 1"), ModuleObject(2, 1, "Module 2"))
        coEvery { moduleFacade.getModuleObjects(any()) } returns moduleObjects

        val result = dataSource.getFirstPageModuleObjects(Course(1), true)

        Assert.assertEquals(ApiType.DB, (result as DataResult.Success).apiType)
        Assert.assertEquals(moduleObjects, result.dataOrNull)
    }

    @Test
    fun `getFirstPageModuleItems returns all module items with DB api type`() = runTest {
        val moduleItems = listOf(ModuleItem(1, 1, 1, "Item 1"), ModuleItem(2, 1, 2, "Item 2"))
        coEvery { moduleFacade.getModuleItems(any()) } returns moduleItems

        val result = dataSource.getFirstPageModuleItems(Course(1), 1, true)

        Assert.assertEquals(ApiType.DB, (result as DataResult.Success).apiType)
        Assert.assertEquals(moduleItems, result.dataOrNull)
    }

    @Test
    fun `Convert tab entities to tab api models`() = runTest {
        val tabEntities = listOf(TabEntity(Tab("modules"), 1), TabEntity(Tab("grades"), 1))
        coEvery { tabDao.findByCourseId(1) } returns tabEntities

        val result = dataSource.getTabs(Course(1), true)

        Assert.assertEquals(ApiType.DB, (result as DataResult.Success).apiType)
        Assert.assertEquals(2, result.dataOrNull?.size)
        Assert.assertEquals("modules", result.dataOrNull!![0].tabId)
        Assert.assertEquals("grades", result.dataOrNull!![1].tabId)
    }

    @Test
    fun `Load course settings successfully returns api model`() = runTest {
        val expected = CourseSettings(restrictQuantitativeData = true)

        coEvery { courseSettingsDao.findByCourseId(any()) } returns CourseSettingsEntity(expected, 1L)

        val result = dataSource.loadCourseSettings(1, true)

        assertEquals(expected, result)
    }
}