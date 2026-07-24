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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineActionDrainWorkerTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    @Before
    fun setUp() {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { apiPrefs.user } returns User(id = 1L)
    }

    @Test
    fun `drains every action syncer and succeeds`() = runTest {
        val syncerA: OfflineActionSyncer = mockk(relaxed = true)
        val syncerB: OfflineActionSyncer = mockk(relaxed = true)

        val result = buildWorker(setOf(syncerA, syncerB)).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { syncerA.drain() }
        coVerify { syncerB.drain() }
    }

    @Test
    fun `succeeds when there are no syncers`() = runTest {
        val result = buildWorker(emptySet()).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `retries when a syncer fails unexpectedly`() = runTest {
        val syncer: OfflineActionSyncer = mockk(relaxed = true)
        coEvery { syncer.drain() } throws RuntimeException("boom")

        val result = buildWorker(setOf(syncer)).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `keeps retrying regardless of attempt count so pending actions are never abandoned`() = runTest {
        val syncer: OfflineActionSyncer = mockk(relaxed = true)
        coEvery { syncer.drain() } throws RuntimeException("boom")

        val result = buildWorker(setOf(syncer), runAttemptCount = 100).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun `does nothing when offline is disabled`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        val syncer: OfflineActionSyncer = mockk(relaxed = true)

        val result = buildWorker(setOf(syncer)).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 0) { syncer.drain() }
    }

    private fun buildWorker(
        syncers: Set<OfflineActionSyncer>,
        runAttemptCount: Int = 0,
    ): OfflineActionDrainWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return TestListenableWorkerBuilder<OfflineActionDrainWorker>(context)
            .setInputData(Data.EMPTY)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ) = OfflineActionDrainWorker(appContext, workerParameters, featureFlagProvider, apiPrefs, syncers)
            })
            .build()
    }
}
