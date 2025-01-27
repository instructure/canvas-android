/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.settings

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.utils.AppTheme
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val settingsBehaviour: SettingsBehaviour = mockk(relaxed = true)
    private val syncSettingsFacade: SyncSettingsFacade = mockk(relaxed = true)
    private val themePrefs: ThemePrefs = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<Boolean>(OFFLINE_ENABLED) } returns true
        every { savedStateHandle.get<Int>("scrollValue") } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `Behaviour maps correctly`() {
        val items = mapOf(
            R.string.preferences to listOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS
            ),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )
        every { settingsBehaviour.settingsItems } returns items

        every { themePrefs.appTheme } returns 0

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(AppTheme.LIGHT.ordinal, uiState.appTheme)
        assertEquals(items, uiState.items)
    }

    @Test
    fun `Change app theme`() = runTest {
        val items = mapOf(
            R.string.preferences to listOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS
            ),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )
        every { settingsBehaviour.settingsItems } returns items

        every { themePrefs.appTheme } returns 0

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value

        uiState.actionHandler(SettingsAction.SetAppTheme(AppTheme.DARK, 0, 0, 0))

        verify {
            themePrefs.appTheme = AppTheme.DARK.ordinal
        }
        assertEquals(AppTheme.DARK.ordinal, viewModel.uiState.value.appTheme)

        verify { analytics.logEvent(AnalyticsEventConstants.DARK_MODE_ON) }

        val events = mutableListOf<SettingsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(SettingsViewModelAction.AppThemeClickPosition(0, 0, 0), events.last())
        }
    }

    @Test
    fun `Set homeroom view action`() = runTest {
        val items = mapOf(
            R.string.preferences to listOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS,
                SettingsItem.HOMEROOM_VIEW
            ),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )
        every { settingsBehaviour.settingsItems } returns items

        every { themePrefs.appTheme } returns 0

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value

        uiState.actionHandler(SettingsAction.SetHomeroomView(true))
        assertEquals(true, viewModel.uiState.value.homeroomView)

        verify {
            apiPrefs.elementaryDashboardEnabledOverride = true
        }

    }

    @Test
    fun `Offline sync settings`() = runTest {
        val syncSettingsLiveData =
            MutableLiveData(SyncSettingsEntity(1L, false, SyncFrequency.DAILY, false))
        coEvery { syncSettingsFacade.getSyncSettingsListenable() } returns syncSettingsLiveData

        val items = mapOf(
            R.string.offlineSyncSettingsTitle to listOf(
                SettingsItem.OFFLINE_SYNCHRONIZATION
            )
        )

        every { settingsBehaviour.settingsItems } returns items

        every { themePrefs.appTheme } returns 0

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value

        assertEquals(R.string.syncSettings_manualDescription, uiState.offlineState)

        syncSettingsLiveData.value = SyncSettingsEntity(1L, true, SyncFrequency.DAILY, false)
        assertEquals(SyncFrequency.DAILY.readable, viewModel.uiState.value.offlineState)

        syncSettingsLiveData.value = SyncSettingsEntity(1L, true, SyncFrequency.WEEKLY, false)
        assertEquals(SyncFrequency.WEEKLY.readable, viewModel.uiState.value.offlineState)
    }

    @Test
    fun `item click`() = runTest {
        val items = mapOf(
            R.string.preferences to listOf(
                SettingsItem.APP_THEME,
                SettingsItem.PROFILE_SETTINGS,
                SettingsItem.PUSH_NOTIFICATIONS,
                SettingsItem.EMAIL_NOTIFICATIONS
            ),
            R.string.legal to listOf(SettingsItem.ABOUT, SettingsItem.LEGAL)
        )
        every { settingsBehaviour.settingsItems } returns items

        every { themePrefs.appTheme } returns 0

        val viewModel = createViewModel()

        viewModel.uiState.value.items.flatMap { it.value }.forEach { item ->
            viewModel.uiState.value.actionHandler(SettingsAction.ItemClicked(item))
            val events = mutableListOf<SettingsViewModelAction>()
            backgroundScope.launch(testDispatcher) {
                viewModel.events.toList(events)
                assertEquals(SettingsViewModelAction.Navigate(item), events.last())
            }
        }
    }


    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(savedStateHandle, settingsBehaviour, context, syncSettingsFacade, colorKeeper, themePrefs, apiPrefs, analytics)
    }
}