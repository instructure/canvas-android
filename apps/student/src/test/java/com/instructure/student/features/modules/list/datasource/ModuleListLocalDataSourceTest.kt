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
import com.instructure.pandautils.room.offline.daos.CheckpointDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.CheckpointEntity
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleListLocalDataSourceTest {

    private val tabDao = mockk<TabDao>(relaxed = true)
    private val moduleFacade = mockk<ModuleFacade>(relaxed = true)
    private val courseSettingsDao = mockk<CourseSettingsDao>(relaxed = true)
    private val checkpointDao = mockk<CheckpointDao>(relaxed = true)

    private val dataSource = ModuleListLocalDataSource(tabDao, moduleFacade, courseSettingsDao, checkpointDao)

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

    @Test
    fun `Get module item checkpoints groups by module item id`() = runTest {
        val checkpointEntities = listOf(
            CheckpointEntity(
                id = 1,
                assignmentId = null,
                name = null,
                tag = "reply_to_topic",
                pointsPossible = 5.0,
                dueAt = "2025-10-15T23:59:59Z",
                onlyVisibleToOverrides = false,
                lockAt = null,
                unlockAt = null,
                moduleItemId = 100L,
                courseId = 1L
            ),
            CheckpointEntity(
                id = 2,
                assignmentId = null,
                name = null,
                tag = "reply_to_entry",
                pointsPossible = 5.0,
                dueAt = "2025-10-20T23:59:59Z",
                onlyVisibleToOverrides = false,
                lockAt = null,
                unlockAt = null,
                moduleItemId = 100L,
                courseId = 1L
            )
        )
        coEvery { checkpointDao.findByCourseIdWithModuleItem(any()) } returns checkpointEntities

        val result = dataSource.getModuleItemCheckpoints("1", false)

        assertEquals(1, result.size)
        assertEquals("100", result[0].moduleItemId)
        assertEquals(2, result[0].checkpoints.size)
    }

    @Test
    fun `Get module item checkpoints filters out null module item ids`() = runTest {
        val checkpointEntities = listOf(
            CheckpointEntity(
                id = 1,
                assignmentId = 1L,
                name = null,
                tag = "reply_to_topic",
                pointsPossible = 5.0,
                dueAt = "2025-10-15T23:59:59Z",
                onlyVisibleToOverrides = false,
                lockAt = null,
                unlockAt = null,
                moduleItemId = null,
                courseId = 1L
            )
        )
        coEvery { checkpointDao.findByCourseIdWithModuleItem(any()) } returns checkpointEntities

        val result = dataSource.getModuleItemCheckpoints("1", false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Get module item checkpoints returns empty list when no checkpoints found`() = runTest {
        coEvery { checkpointDao.findByCourseIdWithModuleItem(any()) } returns emptyList()

        val result = dataSource.getModuleItemCheckpoints("1", false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Get module item checkpoints converts checkpoint entities to api models`() = runTest {
        val checkpointEntity = CheckpointEntity(
            id = 1,
            assignmentId = null,
            name = null,
            tag = "reply_to_topic",
            pointsPossible = 10.0,
            dueAt = "2025-10-15T23:59:59Z",
            onlyVisibleToOverrides = false,
            lockAt = null,
            unlockAt = null,
            moduleItemId = 100L,
            courseId = 1L
        )
        coEvery { checkpointDao.findByCourseIdWithModuleItem(any()) } returns listOf(checkpointEntity)

        val result = dataSource.getModuleItemCheckpoints("1", false)

        assertEquals(1, result.size)
        assertEquals("reply_to_topic", result[0].checkpoints[0].tag)
        assertEquals(10.0, result[0].checkpoints[0].pointsPossible, 0.01)
    }
}