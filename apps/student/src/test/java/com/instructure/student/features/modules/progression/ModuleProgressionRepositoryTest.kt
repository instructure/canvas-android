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
package com.instructure.student.features.modules.progression

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionLocalDataSource
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class ModuleProgressionRepositoryTest {

    private val localDataSource: ModuleProgressionLocalDataSource = mockk()
    private val networkDataSource: ModuleProgressionNetworkDataSource = mockk()
    private val networkStateProvider: NetworkStateProvider = mockk()
    private val featureFlagProvider: FeatureFlagProvider = mockk()
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk()
    private val localFileDao: LocalFileDao = mockk()

    private val repository = ModuleProgressionRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider, courseSyncSettingsDao, localFileDao)

    @Before
    fun setup() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
    }

    @Test
    fun `Get all module items from network when device is online`() = runTest {
        val offlineItems = listOf(ModuleItem(id = 1, title = "Offline"), ModuleItem(id = 2, title = "Offline 2"))
        val onlineItems = listOf(ModuleItem(id = 3, title = "Online"), ModuleItem(id = 4, title = "Online 2"))
        coEvery { networkDataSource.getAllModuleItems(any(), any(), any()) } returns onlineItems
        coEvery { localDataSource.getAllModuleItems(any(), any(), any()) } returns offlineItems
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getAllModuleItems(Course(1), 2, true)

        Assert.assertEquals(onlineItems, result)
    }

    @Test
    fun `Get all module items from local storage when device is offline`() = runTest {
        val offlineItems = listOf(ModuleItem(id = 1, title = "Offline"), ModuleItem(id = 2, title = "Offline 2"))
        val onlineItems = listOf(ModuleItem(id = 3, title = "Online"), ModuleItem(id = 4, title = "Online 2"))
        coEvery { networkDataSource.getAllModuleItems(any(), any(), any()) } returns onlineItems
        coEvery { localDataSource.getAllModuleItems(any(), any(), any()) } returns offlineItems
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getAllModuleItems(Course(1), 2, true)

        Assert.assertEquals(offlineItems, result)
    }

    @Test
    fun `Get module item sequence from network when device is online`() = runTest {
        val offlineItems = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 1, title = "offline"))))
        val onlineItems = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 2, title = "online"))))
        coEvery { networkDataSource.getModuleItemSequence(any(), any(), any(), any()) } returns onlineItems
        coEvery { localDataSource.getModuleItemSequence(any(), any(), any(), any()) } returns offlineItems
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getModuleItemSequence(Course(1), "Quiz", "1", true)

        Assert.assertEquals(onlineItems, result)
    }

    @Test
    fun `Get module item sequence from local storage when device is offline`() = runTest {
        val offlineItems = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 1, title = "offline"))))
        val onlineItems = ModuleItemSequence(items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = 2, title = "online"))))
        coEvery { networkDataSource.getModuleItemSequence(any(), any(), any(), any()) } returns onlineItems
        coEvery { localDataSource.getModuleItemSequence(any(), any(), any(), any()) } returns offlineItems
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getModuleItemSequence(Course(1), "Quiz", "1", true)

        Assert.assertEquals(offlineItems, result)
    }

    @Test
    fun `Get detailed quiz from network when device is online`() = runTest {
        val offlineQuiz = Quiz(id = 1, title = "offline")
        val onlineQuiz = Quiz(id = 2, title = "online")
        coEvery { networkDataSource.getDetailedQuiz(any(), any(), any()) } returns onlineQuiz
        coEvery { localDataSource.getDetailedQuiz(any(), any(), any()) } returns offlineQuiz
        coEvery { networkStateProvider.isOnline() } returns true

        val result = repository.getDetailedQuiz("url", 1, true)

        Assert.assertEquals(onlineQuiz, result)
    }

    @Test
    fun `Get detailed quiz from local storage when device is offline`() = runTest {
        val offlineQuiz = Quiz(id = 1, title = "offline")
        val onlineQuiz = Quiz(id = 2, title = "online")
        coEvery { networkDataSource.getDetailedQuiz(any(), any(), any()) } returns onlineQuiz
        coEvery { localDataSource.getDetailedQuiz(any(), any(), any()) } returns offlineQuiz
        coEvery { networkStateProvider.isOnline() } returns false

        val result = repository.getDetailedQuiz("url", 1, true)

        Assert.assertEquals(offlineQuiz, result)
    }

    @Test
    fun `Mark as not done calls network data source and returns it's successful result`() = runTest {
        coEvery { networkDataSource.markAsNotDone(any(), any()) } returns DataResult.Success(mockk())

        val result = repository.markAsNotDone(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsNotDone(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `Mark as not done calls network data source and returns it's failed result`() = runTest {
        coEvery { networkDataSource.markAsNotDone(any(), any()) } returns DataResult.Fail()

        val result = repository.markAsNotDone(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsNotDone(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isFail)
    }

    @Test
    fun `Mark as done calls network data source and returns it's successful result`() = runTest {
        coEvery { networkDataSource.markAsDone(any(), any()) } returns DataResult.Success(mockk())

        val result = repository.markAsDone(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsDone(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `Mark done calls network data source and returns it's failed result`() = runTest {
        coEvery { networkDataSource.markAsDone(any(), any()) } returns DataResult.Fail()

        val result = repository.markAsDone(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsDone(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isFail)
    }

    @Test
    fun `Mark as read calls network data source and returns it's successful result`() = runTest {
        coEvery { networkDataSource.markAsRead(any(), any()) } returns DataResult.Success(mockk())

        val result = repository.markAsRead(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsRead(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `Mark as read calls network data source and returns it's failed result`() = runTest {
        coEvery { networkDataSource.markAsRead(any(), any()) } returns DataResult.Fail()

        val result = repository.markAsRead(Course(1), ModuleItem(1))

        coVerify { networkDataSource.markAsRead(Course(1), ModuleItem(1)) }
        Assert.assertTrue(result.isFail)
    }

    @Test
    fun `getSyncedTabs returns only the synced tabs from dao`() = runTest {
        coEvery { courseSyncSettingsDao.findById(1) } returns CourseSyncSettingsEntity(1, "Course", false, mapOf(
            "Page" to true,
            "Quiz" to false,
            "Assignment" to true,
            "Files" to false
        ))

        val result = repository.getSyncedTabs(1)

        Assert.assertEquals(setOf("Page", "Assignment"), result)
    }

    @Test
    fun `getSyncedFileIds returns only the synced file ids from dao`() = runTest {
        coEvery { localFileDao.findByCourseId(1) } returns listOf(
            LocalFileEntity(1, 1, Date(), "path"), LocalFileEntity(2, 1, Date(), "path2")
        )

        val result = repository.getSyncedFileIds(1)

        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result.contains(1))
        Assert.assertTrue(result.contains(2))
    }

    @Test
    fun `getSyncedFileIds returns empty list if no file is found`() = runTest {
        coEvery { localFileDao.findByCourseId(1) } returns emptyList()

        val result = repository.getSyncedFileIds(1)

        Assert.assertEquals(emptyList<Long>(), result)
    }
}