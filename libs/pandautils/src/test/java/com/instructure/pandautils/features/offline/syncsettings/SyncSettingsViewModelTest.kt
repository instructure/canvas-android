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

package com.instructure.pandautils.features.offline.syncsettings

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.features.offline.sync.settings.SyncSettingsAction
import com.instructure.pandautils.features.offline.sync.settings.SyncSettingsViewData
import com.instructure.pandautils.features.offline.sync.settings.SyncSettingsViewModel
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SyncSettingsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SyncSettingsViewModel

    private val syncSettingsFacade: SyncSettingsFacade = mockk(relaxed = true)
    private val offlineSyncHelper: OfflineSyncHelper = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val offlineAnalyticsManager: OfflineAnalyticsManager = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        setupStrings()

        coEvery { syncSettingsFacade.update(any()) } just runs
        coEvery { offlineSyncHelper.updateWork() } just runs

        viewModel = SyncSettingsViewModel(syncSettingsFacade, offlineSyncHelper, resources, offlineAnalyticsManager)
    }

    @Test
    fun `loadData updates ViewData`() = runTest {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.DAILY, wifiOnly = false)
        val expectedData = SyncSettingsViewData(true, "Daily", false)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()

        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Changing auto sync updates entity`() = runTest {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = false, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)
        val updated = syncSettings.copy(autoSyncEnabled = true)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onAutoSyncChanged(true)

        coVerify {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.scheduleWork()
            offlineAnalyticsManager.reportOfflineAutoSyncSwitchChanged(true)
        }
    }

    @Test
    fun `Frequency selector event is fired`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.showFrequencySelector()

        val event = viewModel.events.value?.peekContent()

        assert(event is SyncSettingsAction.ShowFrequencySelector)
    }

    @Test
    fun `Changing frequency updates entity`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)
        val updated = syncSettings.copy(syncFrequency = SyncFrequency.DAILY)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.showFrequencySelector()

        val event = viewModel.events.value?.peekContent()

        (event as? SyncSettingsAction.ShowFrequencySelector)?.onItemSelected?.invoke(
            SyncFrequency.values().indexOf(updated.syncFrequency)
        )

        coVerify {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
        }
    }

    @Test
    fun `Turning off wifi only shows confirmation`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = true)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onWifiOnlyChanged(false)

        val event = viewModel.events.value?.peekContent()
        assert(event is SyncSettingsAction.ShowWifiConfirmation)
    }

    @Test
    fun `Turning off wifi only updates entity`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = true)
        val updated = syncSettings.copy(wifiOnly = false)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onWifiOnlyChanged(false)

        val event = viewModel.events.value?.peekContent()

        (event as? SyncSettingsAction.ShowWifiConfirmation)?.confirmationCallback?.invoke(true)

        coVerify {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
        }
    }

    @Test
    fun `Canceling wifi confirmation does not update entity`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = true)
        val updated = syncSettings.copy(wifiOnly = false)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onWifiOnlyChanged(false)

        val event = viewModel.events.value?.peekContent()

        (event as? SyncSettingsAction.ShowWifiConfirmation)?.confirmationCallback?.invoke(false)

        coVerify(exactly = 0) {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
        }
    }

    @Test
    fun `Turning on wifi only updates entity`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)
        val updated = syncSettings.copy(wifiOnly = true)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onWifiOnlyChanged(true)

        coVerify {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.updateWork()
        }
    }

    @Test
    fun `Disabling auto sync cancels work`() {
        val syncSettings =
            SyncSettingsEntity(autoSyncEnabled = true, syncFrequency = SyncFrequency.WEEKLY, wifiOnly = false)
        val updated = syncSettings.copy(autoSyncEnabled = false)

        coEvery { syncSettingsFacade.getSyncSettings() } returns syncSettings

        viewModel.loadData()
        viewModel.onAutoSyncChanged(false)

        coVerify {
            syncSettingsFacade.update(updated)
            offlineSyncHelper.cancelWork()
            offlineAnalyticsManager.reportOfflineAutoSyncSwitchChanged(false)
        }
    }

    private fun setupStrings() {
        every { resources.getString(R.string.daily) } returns "Daily"
        every { resources.getString(R.string.weekly) } returns "Weekly"
    }
}