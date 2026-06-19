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
package com.instructure.horizon.features.account.offlinesettings

import com.instructure.horizon.database.dao.HorizonSyncSettingsDao
import com.instructure.horizon.database.entity.HorizonSyncSettingsEntity
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import com.instructure.horizon.offline.sync.HorizonProgressState
import com.instructure.horizon.offline.sync.HorizonSyncProgressData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineSettingsViewModelTest {

    private val syncSettingsDao: HorizonSyncSettingsDao = mockk(relaxed = true)
    private val syncHelper: HorizonOfflineSyncHelper = mockk(relaxed = true)
    private val aggregateProgressObserver: HorizonAggregateProgressObserver = mockk(relaxed = true)
    private val progressFlow = MutableStateFlow(HorizonSyncProgressData())
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { aggregateProgressObserver.progressData } returns progressFlow
        coEvery { syncSettingsDao.getSettingsOnce() } returns HorizonSyncSettingsEntity()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun viewModel() = OfflineSettingsViewModel(
        syncSettingsDao = syncSettingsDao,
        syncHelper = syncHelper,
        aggregateProgressObserver = aggregateProgressObserver,
    )

    @Test
    fun `isSyncActive is false when no sync is running`() = runTest {
        progressFlow.value = HorizonSyncProgressData(isActive = false)

        val vm = viewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSyncActive)
    }

    @Test
    fun `isSyncActive becomes true when sync starts`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSyncActive)

        progressFlow.value = HorizonSyncProgressData(
            progressState = HorizonProgressState.IN_PROGRESS,
            isActive = true,
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isSyncActive)
    }

    @Test
    fun `isSyncActive returns to false when sync completes`() = runTest {
        progressFlow.value = HorizonSyncProgressData(
            progressState = HorizonProgressState.IN_PROGRESS,
            isActive = true,
        )

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSyncActive)

        progressFlow.value = HorizonSyncProgressData(
            progressState = HorizonProgressState.COMPLETED,
            isActive = false,
        )
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSyncActive)
    }

    @Test
    fun `loadSettings populates user-visible fields without touching isSyncActive`() = runTest {
        coEvery { syncSettingsDao.getSettingsOnce() } returns HorizonSyncSettingsEntity(
            autoSyncEnabled = false,
            syncFrequency = SyncFrequency.WEEKLY,
            wifiOnly = false,
        )

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(false, state.autoSyncEnabled)
        assertEquals(SyncFrequency.WEEKLY, state.syncFrequency)
        assertEquals(false, state.wifiOnlyEnabled)
        assertFalse(state.isSyncActive)
    }
}
