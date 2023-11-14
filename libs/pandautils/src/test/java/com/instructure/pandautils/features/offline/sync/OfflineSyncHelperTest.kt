/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.offline.sync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.impl.OperationImpl
import com.google.common.util.concurrent.Futures
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration

@ExperimentalCoroutinesApi
class OfflineSyncHelperTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk(relaxed = true)
    private val syncSettingsFacade: SyncSettingsFacade = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var offlineSyncHelper: OfflineSyncHelper

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { apiPrefs.user } returns User(1L)

        every { workManager.enqueue(any<WorkRequest>()) } returns OperationImpl()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns OperationImpl()

        offlineSyncHelper = OfflineSyncHelper(workManager, syncSettingsFacade, apiPrefs)
    }

    @Test
    fun `Only one time sync if worker is already scheduled`() = runTest {
        val courseIds = listOf(1L, 2L, 3L)

        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(
            1L,
            false,
            SyncFrequency.DAILY,
            true
        )
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(mockk(relaxed = true))

        offlineSyncHelper.syncCourses(courseIds)

        coVerify(exactly = 1) { workManager.enqueue(any<WorkRequest>()) }
        coVerify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `Schedule worker if it's not scheduled yet`() = runTest {
        val courseIds = listOf(1L, 2L, 3L)

        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(emptyList())

        offlineSyncHelper.syncCourses(courseIds)

        coVerify(exactly = 0) { workManager.enqueue(any<WorkRequest>()) }
        coVerify(exactly = 1) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `Do not schedule worker if auto sync is disabled`() = runTest {
        val courseIds = listOf(1L, 2L, 3L)

        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(
            1L,
            false,
            SyncFrequency.DAILY,
            true
        )
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(emptyList())

        offlineSyncHelper.syncCourses(courseIds)

        coVerify(exactly = 1) { workManager.enqueue(any<OneTimeWorkRequest>()) }
        coVerify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `Cancel should cancel work with correct id`() {
        every { workManager.cancelUniqueWork(any()) } returns OperationImpl()

        offlineSyncHelper.cancelWork()

        coVerify { workManager.cancelUniqueWork("1") }
    }

    @Test
    fun `One time sync is called with given ids`() = runTest {
        val courseIds = listOf(1L, 2L, 3L)

        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(
            1L,
            false,
            SyncFrequency.DAILY,
            true
        )
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(emptyList())

        offlineSyncHelper.syncCourses(courseIds)

        val captor = slot<OneTimeWorkRequest>()
        coVerify(exactly = 1) {
            workManager.enqueue(capture(captor))
        }
        coVerify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }

        val workRequest = captor.captured

        courseIds.forEachIndexed { index, l ->
            assertEquals(l, workRequest.workSpec.input.getLongArray(COURSE_IDS)?.get(index))
        }
    }

    @Test
    fun `Wifi only enabled maps correctly`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)

        offlineSyncHelper.scheduleWork()

        val captor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(captor)) }

        val workRequest = captor.captured

        assertEquals(NetworkType.UNMETERED, workRequest.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun `Wifi only disabled maps correctly`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(
            1L,
            true,
            SyncFrequency.DAILY,
            false
        )

        offlineSyncHelper.scheduleWork()

        val captor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(captor)) }

        val workRequest = captor.captured

        assertEquals(NetworkType.CONNECTED, workRequest.workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun `Daily sync maps correctly`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)

        offlineSyncHelper.scheduleWork()

        val captor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(captor)) }

        val workRequest = captor.captured

        assertEquals(Duration.ofDays(1).toMillis(), workRequest.workSpec.intervalDuration)
    }

    @Test
    fun `Weekly sync maps correctly`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(
            1L,
            true,
            SyncFrequency.WEEKLY,
            true
        )

        offlineSyncHelper.scheduleWork()

        val captor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(captor)) }

        val workRequest = captor.captured

        assertEquals(Duration.ofDays(7).toMillis(), workRequest.workSpec.intervalDuration)
    }

    @Test
    fun `Update sets the correct id`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)

        offlineSyncHelper.scheduleWork()

        val originalCaptor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(originalCaptor)) }
        val originalRequest = originalCaptor.captured

        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(
            listOf(
                WorkInfo(originalRequest.id, WorkInfo.State.ENQUEUED, Data.EMPTY, emptyList(), Data.EMPTY, 1, 1)
            )
        )

        offlineSyncHelper.updateWork()

        val updatedCaptor = slot<PeriodicWorkRequest>()
        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), capture(updatedCaptor)) }
        val updatedRequest = updatedCaptor.captured

        assertEquals(originalRequest.id, updatedRequest.id)
    }

    @Test
    fun `Cancel running workers`() {
        offlineSyncHelper.cancelRunningWorkers()

        verify {
            workManager.cancelAllWorkByTag(OfflineSyncWorker.TAG)
        }
    }

    @Test
    fun `scheduleWorkAfterLogin should schedule work when auto sync is enabled and no work is already scheduled`() = runTest {
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(emptyList())
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.DAILY, wifiOnly = true)

        offlineSyncHelper.scheduleWorkAfterLogin()

        coVerify { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `scheduleWorkAfterLogin should not schedule work when auto sync is disabled`() = runTest {
        coEvery { syncSettingsFacade.getSyncSettings() } returns SyncSettingsEntity(autoSyncEnabled = false, syncFrequency = SyncFrequency.DAILY, wifiOnly = true)

        offlineSyncHelper.scheduleWorkAfterLogin()

        coVerify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `scheduleWorkAfterLogin should not schedule work when work is already scheduled`() = runTest {
        every { workManager.getWorkInfosForUniqueWork(any()) } returns Futures.immediateFuture(mockk(relaxed = true))

        offlineSyncHelper.scheduleWorkAfterLogin()

        coVerify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

}