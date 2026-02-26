/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.offline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveOfflineSyncUpdatesUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val workManager: WorkManager = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk()
    private lateinit var useCase: ObserveOfflineSyncUpdatesUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = ObserveOfflineSyncUpdatesUseCase(workManager, featureFlagProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `returns flow without emissions when offline is disabled`() = runTest(testDispatcher) {
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        val workInfosLiveData = MutableLiveData<List<WorkInfo>>()
        every { workManager.getWorkInfosLiveData(any()) } returns workInfosLiveData

        val emissions = mutableListOf<Unit>()
        val job = launch {
            useCase(Unit).collect { emissions.add(it) }
        }

        // Give the flow time to complete
        withTimeout(100) {
            job.join()
        }

        // Should complete immediately without emissions since offline is disabled
        assertTrue(emissions.isEmpty())
        verify(exactly = 0) { workManager.getWorkInfosLiveData(any()) }
    }

    @Test
    fun `sets up WorkManager observation when offline is enabled`() = runTest(testDispatcher) {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val workInfosLiveData = MutableLiveData<List<WorkInfo>>()
        every { workManager.getWorkInfosLiveData(any()) } returns workInfosLiveData

        // Start collecting the flow
        val job = launch {
            useCase(Unit).collect { /* collect emissions */ }
        }

        // Small delay to allow flow to start
        kotlinx.coroutines.delay(50)
        job.cancel()

        // Verify WorkManager was queried
        verify { workManager.getWorkInfosLiveData(any()) }
    }

    @Test
    fun `emits when tracked worker succeeds`() = runTest(testDispatcher) {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val workInfosLiveData = MutableLiveData<List<WorkInfo>>()
        every { workManager.getWorkInfosLiveData(any()) } returns workInfosLiveData

        val workId = UUID.randomUUID()

        // Collect emissions
        val emissions = mutableListOf<Unit>()
        val job = launch(testDispatcher) {
            useCase(Unit).take(1).toList(emissions)
        }

        // Emit RUNNING state
        workInfosLiveData.value = listOf(createWorkInfo(WorkInfo.State.RUNNING, workId))

        // Emit SUCCEEDED state
        workInfosLiveData.value = listOf(createWorkInfo(WorkInfo.State.SUCCEEDED, workId))

        job.join()

        // Should emit once when worker completes
        assertEquals(1, emissions.size)
    }

    @Test
    fun `does not emit when untracked worker succeeds`() = runTest(testDispatcher) {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val workInfosLiveData = MutableLiveData<List<WorkInfo>>()
        every { workManager.getWorkInfosLiveData(any()) } returns workInfosLiveData

        val workId = UUID.randomUUID()

        // Collect emissions with timeout
        val emissions = mutableListOf<Unit>()
        val job = launch(testDispatcher) {
            useCase(Unit).collect { emissions.add(it) }
        }

        // Emit SUCCEEDED state without RUNNING first
        workInfosLiveData.value = listOf(createWorkInfo(WorkInfo.State.SUCCEEDED, workId))

        kotlinx.coroutines.delay(100)
        job.cancel()

        // Should not emit since worker was never tracked as running
        assertTrue(emissions.isEmpty())
    }

    private fun createWorkInfo(state: WorkInfo.State, id: UUID): WorkInfo {
        return mockk {
            every { this@mockk.state } returns state
            every { this@mockk.id } returns id
        }
    }
}