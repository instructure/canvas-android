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
package com.instructure.student.features.modules.list

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.list.datasource.ModuleListLocalDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListNetworkDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class ModuleListRepositoryTest {

    private val networkDataSource: ModuleListNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: ModuleListLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)

    private val repository = ModuleListRepository(localDataSource, networkDataSource, networkStateProvider)

    @Test
    fun `Get all modules for course from network data source when device is online`() = runTest {
        val offlineModules = listOf(ModuleObject(id = 1, name = "Offline"), ModuleObject(id = 2, name = "Offline 2"))
        val onlineModules = listOf(ModuleObject(id = 3, name = "Online"), ModuleObject(id = 4, name = "Online 2"))
        coEvery { networkDataSource.getAllModuleObjects(any(), any()) } returns DataResult.Success(onlineModules)
        coEvery { localDataSource.getAllModuleObjects(any(), any()) } returns DataResult.Success(offlineModules)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getAllModuleObjects(Course(1), true)

        Assert.assertEquals(onlineModules, (result as DataResult.Success).data)
    }

    @Test
    fun `Get all modules for course from local data source when device is offline`() = runTest {
        val offlineModules = listOf(ModuleObject(id = 1, name = "Offline"), ModuleObject(id = 2, name = "Offline 2"))
        val onlineModules = listOf(ModuleObject(id = 3, name = "Online"), ModuleObject(id = 4, name = "Online 2"))
        coEvery { networkDataSource.getAllModuleObjects(any(), any()) } returns DataResult.Success(onlineModules)
        coEvery { localDataSource.getAllModuleObjects(any(), any()) } returns DataResult.Success(offlineModules)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getAllModuleObjects(Course(1), true)

        Assert.assertEquals(offlineModules, (result as DataResult.Success).data)
    }

    @Test
    fun `Get first page modules for course from network data source when device is online`() = runTest {
        val offlineModules = listOf(ModuleObject(id = 1, name = "Offline"), ModuleObject(id = 2, name = "Offline 2"))
        val onlineModules = listOf(ModuleObject(id = 3, name = "Online"), ModuleObject(id = 4, name = "Online 2"))
        coEvery { networkDataSource.getFirstPageModuleObjects(any(), any()) } returns DataResult.Success(onlineModules)
        coEvery { localDataSource.getFirstPageModuleObjects(any(), any()) } returns DataResult.Success(offlineModules)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getFirstPageModuleObjects(Course(1), true)

        Assert.assertEquals(onlineModules, (result as DataResult.Success).data)
    }

    @Test
    fun `Get first page modules for course from local data source when device is offline`() = runTest {
        val offlineModules = listOf(ModuleObject(id = 1, name = "Offline"), ModuleObject(id = 2, name = "Offline 2"))
        val onlineModules = listOf(ModuleObject(id = 3, name = "Online"), ModuleObject(id = 4, name = "Online 2"))
        coEvery { networkDataSource.getFirstPageModuleObjects(any(), any()) } returns DataResult.Success(onlineModules)
        coEvery { localDataSource.getFirstPageModuleObjects(any(), any()) } returns DataResult.Success(offlineModules)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getFirstPageModuleObjects(Course(1), true)

        Assert.assertEquals(offlineModules, (result as DataResult.Success).data)
    }

    @Test
    fun `Get next page modules for course from network data source`() = runTest {
        val onlineModules = listOf(ModuleObject(id = 3, name = "Online"), ModuleObject(id = 4, name = "Online 2"))
        coEvery { networkDataSource.getNextPageModuleObjects(any(), any()) } returns DataResult.Success(onlineModules)

        val result = repository.getNextPageModuleObjects("", true)

        Assert.assertEquals(onlineModules, (result as DataResult.Success).data)
    }

    @Test
    fun `Get first page module items for module from network data source when device is online`() = runTest {
        val offlineItems = listOf(ModuleItem(id = 1, title = "Offline"), ModuleItem(id = 2, title = "Offline 2"))
        val onlineItems = listOf(ModuleItem(id = 3, title = "Online"), ModuleItem(id = 4, title = "Online 2"))
        coEvery { networkDataSource.getFirstPageModuleItems(any(), any(), any()) } returns DataResult.Success(onlineItems)
        coEvery { localDataSource.getFirstPageModuleItems(any(), any(), any()) } returns DataResult.Success(offlineItems)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getFirstPageModuleItems(Course(1), 2, true)

        Assert.assertEquals(onlineItems, (result as DataResult.Success).data)
    }

    @Test
    fun `Get first page module items for module from local data source when device is online`() = runTest {
        val offlineItems = listOf(ModuleItem(id = 1, title = "Offline"), ModuleItem(id = 2, title = "Offline 2"))
        val onlineItems = listOf(ModuleItem(id = 3, title = "Online"), ModuleItem(id = 4, title = "Online 2"))
        coEvery { networkDataSource.getFirstPageModuleItems(any(), any(), any()) } returns DataResult.Success(onlineItems)
        coEvery { localDataSource.getFirstPageModuleItems(any(), any(), any()) } returns DataResult.Success(offlineItems)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getFirstPageModuleItems(Course(1), 2, true)

        Assert.assertEquals(offlineItems, (result as DataResult.Success).data)
    }

    @Test
    fun `Get next page module items for module from network data source`() = runTest {
        val onlineItems = listOf(ModuleItem(id = 3, title = "Online"), ModuleItem(id = 4, title = "Online 2"))
        coEvery { networkDataSource.getNextPageModuleItems(any(), any()) } returns DataResult.Success(onlineItems)

        val result = repository.getNextPageModuleItems("", true)

        Assert.assertEquals(onlineItems, (result as DataResult.Success).data)
    }

    @Test
    fun `Get tabs from network data source when device is online`() = runTest {
        val offlineTabs = listOf(Tab(tabId = "grades"), Tab(tabId = "modules"))
        val onlineTabs = listOf(Tab(tabId = "grades online"), Tab(tabId = "modules online"))
        coEvery { networkDataSource.getTabs(any(), any()) } returns DataResult.Success(onlineTabs)
        coEvery { localDataSource.getTabs(any(), any()) } returns DataResult.Success(offlineTabs)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getTabs(Course(1), true)

        Assert.assertEquals(onlineTabs, result)
    }

    @Test
    fun `Get tabs from local data source when device is offlibe`() = runTest {
        val offlineTabs = listOf(Tab(tabId = "grades"), Tab(tabId = "modules"))
        val onlineTabs = listOf(Tab(tabId = "grades online"), Tab(tabId = "modules online"))
        coEvery { networkDataSource.getTabs(any(), any()) } returns DataResult.Success(onlineTabs)
        coEvery { localDataSource.getTabs(any(), any()) } returns DataResult.Success(offlineTabs)
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getTabs(Course(1), true)

        Assert.assertEquals(offlineTabs, result)
    }

    @Test
    fun `Filter out hidden external tabs`() = runTest {
        val onlineTabs = listOf(
            Tab(tabId = "grades", isHidden = true),
            Tab(tabId = "modules", type = Tab.TYPE_EXTERNAL),
            Tab(tabId = "filtered out", type = Tab.TYPE_EXTERNAL, isHidden = true)
        )
        coEvery { networkDataSource.getTabs(any(), any()) } returns DataResult.Success(onlineTabs)
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getTabs(Course(1), true)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(onlineTabs.first(), result.first())
        Assert.assertEquals(onlineTabs[1], result[1])
    }
}