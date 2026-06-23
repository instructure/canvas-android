/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.offline.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.features.account.offlinesettings.SyncFrequency
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HorizonOfflineSyncHelperTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val syncSettingsDao: HorizonSyncSettingsDao = mockk(relaxed = true)
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val helper = HorizonOfflineSyncHelper(
        workManager = workManager,
        syncSettingsDao = syncSettingsDao,
        courseSyncPlanDao = courseSyncPlanDao,
        apiPrefs = apiPrefs,
        featureFlagProvider = featureFlagProvider,
    )

    @Before
    fun setUp() {
        every { apiPrefs.user } returns User(id = USER_ID)
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { syncSettingsDao.getSettingsOnce() } returns HorizonSyncSettingsEntity()
        coEvery { courseSyncPlanDao.findAll() } returns listOf(plan())
    }

    @Test
    fun `applyPeriodicSchedule no-ops when there is no current user`() = runTest {
        every { apiPrefs.user } returns null

        helper.applyPeriodicSchedule()

        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
        verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
    }

    @Test
    fun `applyPeriodicSchedule cancels periodic when feature flag is off`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        helper.applyPeriodicSchedule()

        verify { workManager.cancelUniqueWork(PERIODIC_NAME) }
        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `applyPeriodicSchedule cancels periodic when auto-sync is disabled`() = runTest {
        coEvery { syncSettingsDao.getSettingsOnce() } returns
            HorizonSyncSettingsEntity(autoSyncEnabled = false)

        helper.applyPeriodicSchedule()

        verify { workManager.cancelUniqueWork(PERIODIC_NAME) }
        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `applyPeriodicSchedule cancels periodic when no courses are selected`() = runTest {
        coEvery { courseSyncPlanDao.findAll() } returns emptyList()

        helper.applyPeriodicSchedule()

        verify { workManager.cancelUniqueWork(PERIODIC_NAME) }
        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }

    @Test
    fun `applyPeriodicSchedule enqueues with UPDATE policy when auto-sync is on and courses exist`() = runTest {
        helper.applyPeriodicSchedule()

        verify {
            workManager.enqueueUniquePeriodicWork(
                eq(PERIODIC_NAME),
                eq(ExistingPeriodicWorkPolicy.UPDATE),
                any<PeriodicWorkRequest>(),
            )
        }
        verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
    }

    @Test
    fun `applyPeriodicSchedule does not re-enqueue when called twice with identical settings`() = runTest {
        helper.applyPeriodicSchedule()
        helper.applyPeriodicSchedule()

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                eq(PERIODIC_NAME),
                eq(ExistingPeriodicWorkPolicy.UPDATE),
                any<PeriodicWorkRequest>(),
            )
        }
    }

    @Test
    fun `applyPeriodicSchedule re-enqueues when frequency changes`() = runTest {
        helper.applyPeriodicSchedule()

        coEvery { syncSettingsDao.getSettingsOnce() } returns
            HorizonSyncSettingsEntity(syncFrequency = SyncFrequency.WEEKLY)

        helper.applyPeriodicSchedule()

        verify(exactly = 2) {
            workManager.enqueueUniquePeriodicWork(
                eq(PERIODIC_NAME),
                eq(ExistingPeriodicWorkPolicy.UPDATE),
                any<PeriodicWorkRequest>(),
            )
        }
    }

    @Test
    fun `syncCoursesNow enqueues a one-time job with REPLACE policy`() = runTest {
        helper.syncCoursesNow(listOf(11L, 22L))

        verify {
            workManager.enqueueUniqueWork(
                eq(ONE_TIME_NAME),
                eq(ExistingWorkPolicy.REPLACE),
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `syncCoursesNow no-ops when feature flag is off`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        helper.syncCoursesNow(listOf(11L))

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `syncCoursesNow no-ops when there is no current user`() = runTest {
        every { apiPrefs.user } returns null

        helper.syncCoursesNow(listOf(11L))

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `cancelCurrentSync stops in-flight work but preserves plans for upcoming periodic syncs`() = runTest {
        coEvery { workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_NAME) } returns
            flowOf(emptyList())

        helper.cancelCurrentSync()

        coVerify { workManager.cancelAllWorkByTag(ONE_TIME_TAG) }
        coVerify(exactly = 0) { courseSyncPlanDao.deleteAll() }
        verify(exactly = 0) { workManager.cancelUniqueWork(PERIODIC_NAME) }
    }

    @Test
    fun `cancelCurrentSync cancels a running periodic instance without dropping its schedule`() = runTest {
        val runningId = UUID.randomUUID()
        val runningInfo = mockk<WorkInfo> {
            every { state } returns WorkInfo.State.RUNNING
            every { id } returns runningId
        }
        coEvery { workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_NAME) } returns
            flowOf(listOf(runningInfo))

        helper.cancelCurrentSync()

        verify { workManager.cancelWorkById(runningId) }
        verify(exactly = 0) { workManager.cancelUniqueWork(PERIODIC_NAME) }
    }

    @Test
    fun `cancelCurrentSync preserves the applied fingerprint so settings tweaks still short-circuit`() = runTest {
        coEvery { workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_NAME) } returns
            flowOf(emptyList())

        helper.applyPeriodicSchedule()
        helper.cancelCurrentSync()
        helper.applyPeriodicSchedule()

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                eq(PERIODIC_NAME),
                eq(ExistingPeriodicWorkPolicy.UPDATE),
                any<PeriodicWorkRequest>(),
            )
        }
    }

    @Test
    fun `fullStop cancels both one-time and periodic work`() = runTest {
        coEvery { workManager.getWorkInfosByTagFlow(ONE_TIME_TAG) } returns
            flowOf(emptyList())
        coEvery { workManager.getWorkInfosByTagFlow(PERIODIC_TAG) } returns
            flowOf(emptyList())

        helper.fullStop()

        coVerify { workManager.cancelAllWorkByTag(ONE_TIME_TAG) }
        verify { workManager.cancelUniqueWork(PERIODIC_NAME) }
    }

    @Test
    fun `fullStop resets the applied fingerprint so re-selecting the same courses re-enqueues periodic work`() = runTest {
        coEvery { workManager.getWorkInfosByTagFlow(ONE_TIME_TAG) } returns
            flowOf(emptyList())
        coEvery { workManager.getWorkInfosByTagFlow(PERIODIC_TAG) } returns
            flowOf(emptyList())

        helper.applyPeriodicSchedule()
        helper.fullStop()
        helper.applyPeriodicSchedule()

        verify(exactly = 2) {
            workManager.enqueueUniquePeriodicWork(
                eq(PERIODIC_NAME),
                eq(ExistingPeriodicWorkPolicy.UPDATE),
                any<PeriodicWorkRequest>(),
            )
        }
    }

    private fun plan() = HorizonCourseSyncPlanEntity(
        courseId = 11L,
        courseName = "Course 11",
    )

    companion object {
        private const val USER_ID = 42L
        private const val PERIODIC_NAME = "HorizonOfflinePeriodicSync-$USER_ID"
        private const val ONE_TIME_NAME = "HorizonOfflineSyncOneTime-$USER_ID"
        private const val PERIODIC_TAG = "${HorizonOfflineSyncWorker.PERIODIC_TAG}-$USER_ID"
        private const val ONE_TIME_TAG = "${HorizonOfflineSyncWorker.ONE_TIME_TAG}-$USER_ID"
    }
}
