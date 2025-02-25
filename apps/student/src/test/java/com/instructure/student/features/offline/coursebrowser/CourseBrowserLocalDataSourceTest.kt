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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserLocalDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class CourseBrowserLocalDataSourceTest {

    private val tabDao: TabDao = mockk(relaxed = true)
    private val pageFacade: PageFacade = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val fileSyncSettingsDao: FileSyncSettingsDao = mockk(relaxed = true)

    private val dataSource = CourseBrowserLocalDataSource(tabDao, pageFacade, courseSyncSettingsDao, fileSyncSettingsDao)

    @Test
    fun `Get tabs successfully returns api model`() = runTest {
        coEvery { tabDao.findByCourseId(any()) } returns listOf(TabEntity(Tab(label = "Tab", tabId = "123"), 1))
        coEvery { courseSyncSettingsDao.findById(any()) } returns CourseSyncSettingsEntity(1, "Course", false, tabs = mapOf("123" to true))

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(listOf(Tab(label = "Tab", tabId = "123")), tabs)
    }

    @Test
    fun `Get tabs with correct enabled state`() = runTest {
        coEvery { tabDao.findByCourseId(any()) } returns listOf(
            TabEntity(Tab(label = "Tab 1", tabId = "1"), 1),
            TabEntity(Tab(label = "Tab 2", tabId = "2"), 1),
            TabEntity(Tab(label = "Tab 3", tabId = "3"), 1)
        )
        coEvery { courseSyncSettingsDao.findById(any()) } returns CourseSyncSettingsEntity(
            1, "Course", false, tabs = mapOf("1" to true, "2" to false)
        )

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(
            listOf(
                Tab(label = "Tab 1", tabId = "1", enabled = true),
                Tab(label = "Tab 2", tabId = "2", enabled = false),
                Tab(label = "Tab 3", tabId = "3", enabled = false)
            ), tabs
        )
    }

    @Test
    fun `Enable files tab if full file sync is on`() = runTest {
        coEvery { tabDao.findByCourseId(any()) } returns listOf(
            TabEntity(Tab(label = "Tab 1", tabId = "1"), 1),
            TabEntity(Tab(label = "Files", tabId = Tab.FILES_ID), 1),
        )
        coEvery { courseSyncSettingsDao.findById(any()) } returns CourseSyncSettingsEntity(
            1, "Course", false, tabs = mapOf("1" to true), fullFileSync = true
        )

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(
            listOf(
                Tab(label = "Tab 1", tabId = "1", enabled = true),
                Tab(label = "Files", tabId = Tab.FILES_ID, enabled = true)
            ), tabs
        )
    }

    @Test
    fun `Enable files tab if any file is synced`() = runTest {
        coEvery { tabDao.findByCourseId(any()) } returns listOf(
            TabEntity(Tab(label = "Tab 1", tabId = "1"), 1),
            TabEntity(Tab(label = "Files", tabId = Tab.FILES_ID), 1),
        )
        coEvery { courseSyncSettingsDao.findById(any()) } returns CourseSyncSettingsEntity(
            1, "Course", false, tabs = mapOf("1" to true)
        )
        coEvery { fileSyncSettingsDao.findByCourseId(1) } returns listOf(FileSyncSettingsEntity(1, "", 1, ""))

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(
            listOf(
                Tab(label = "Tab 1", tabId = "1", enabled = true),
                Tab(label = "Files", tabId = Tab.FILES_ID, enabled = true)
            ), tabs
        )
    }

    @Test
    fun `Do not enable files tab if no file is synced`() = runTest {
        coEvery { tabDao.findByCourseId(any()) } returns listOf(
            TabEntity(Tab(label = "Tab 1", tabId = "1"), 1),
            TabEntity(Tab(label = "Files", tabId = Tab.FILES_ID), 1),
        )
        coEvery { courseSyncSettingsDao.findById(any()) } returns CourseSyncSettingsEntity(
            1, "Course", false, tabs = mapOf("1" to true)
        )
        coEvery { fileSyncSettingsDao.findByCourseId(1) } returns emptyList()

        val tabs = dataSource.getTabs(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(
            listOf(
                Tab(label = "Tab 1", tabId = "1", enabled = true),
                Tab(label = "Files", tabId = Tab.FILES_ID, enabled = false)
            ), tabs
        )
    }

    @Test
    fun `Get front page successfully returns api model`() = runTest {
        coEvery { pageFacade.getFrontPage(any()) } returns Page(id = 12, title = "Page title", frontPage = true)

        val frontPage = dataSource.getFrontPage(CanvasContext.emptyCourseContext(1), false)

        Assert.assertEquals(Page(id = 12, title = "Page title", frontPage = true), frontPage)
    }
}