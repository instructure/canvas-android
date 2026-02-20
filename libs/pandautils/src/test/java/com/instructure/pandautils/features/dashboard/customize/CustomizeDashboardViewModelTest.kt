/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.customize

import android.content.res.Resources
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.SwapWidgetPositionsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.UpdateNewDashboardPreferenceUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ToggleWidgetVisibilityUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.UpdateWidgetConfigUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CustomizeDashboardViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val observeWidgetMetadataUseCase: ObserveWidgetMetadataUseCase = mockk(relaxed = true)
    private val swapWidgetPositionsUseCase: SwapWidgetPositionsUseCase = mockk(relaxed = true)
    private val toggleWidgetVisibilityUseCase: ToggleWidgetVisibilityUseCase = mockk(relaxed = true)
    private val observeWidgetConfigUseCase: ObserveWidgetConfigUseCase = mockk(relaxed = true)
    private val updateWidgetConfigUseCase: UpdateWidgetConfigUseCase = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)
    private val updateNewDashboardPreferenceUseCase: UpdateNewDashboardPreferenceUseCase = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val remoteConfigPrefs: RemoteConfigPrefs = mockk(relaxed = true)
    private val bundleStorage = mutableMapOf<String, String?>()

    private lateinit var viewModel: CustomizeDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Clear bundle storage for each test
        bundleStorage.clear()

        // Mock Bundle constructor for analytics tests
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } answers {
            val key = firstArg<String>()
            val value = secondArg<String>()
            bundleStorage[key] = value
        }
        every { anyConstructed<Bundle>().getString(any()) } answers {
            val key = firstArg<String>()
            bundleStorage[key]
        }

        val user = User(id = 1, shortName = "Test User")
        every { apiPrefs.user } returns user
        every { resources.getString(R.string.widget_hello, any()) } returns "Hello, Test User"
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig())
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(globalConfig: GlobalConfig = GlobalConfig()): CustomizeDashboardViewModel {
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(globalConfig)
        return CustomizeDashboardViewModel(
            observeWidgetMetadataUseCase,
            swapWidgetPositionsUseCase,
            toggleWidgetVisibilityUseCase,
            observeWidgetConfigUseCase,
            updateWidgetConfigUseCase,
            resources,
            apiPrefs,
            observeGlobalConfigUseCase,
            updateNewDashboardPreferenceUseCase,
            analytics,
            remoteConfigPrefs
        )
    }

    @Test
    fun testInitialState() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals(null, state.error)
        assertEquals(0, state.widgets.size)
    }

    @Test
    fun testLoadWidgetsSuccess() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase("widget1") } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase("widget2") } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals(null, state.error)
        assertEquals(2, state.widgets.size)
        assertEquals("widget1", state.widgets[0].metadata.id)
        assertEquals("widget2", state.widgets[1].metadata.id)
    }

    @Test
    fun testOnlyEditableWidgetsLoaded() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = false),
            WidgetMetadata(id = "widget3", position = 2, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase("widget1") } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase("widget3") } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.widgets.size)
        assertEquals("widget1", state.widgets[0].metadata.id)
        assertEquals("widget3", state.widgets[1].metadata.id)
    }

    @Test
    fun testWidgetsSortedByPosition() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget3", position = 2, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(3, state.widgets.size)
        assertEquals("widget1", state.widgets[0].metadata.id)
        assertEquals("widget2", state.widgets[1].metadata.id)
        assertEquals("widget3", state.widgets[2].metadata.id)
    }

    @Test
    fun testWidgetConfigsLoaded() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true)
        )
        val settings = listOf(
            WidgetSettingItem(key = "setting1", value = true, type = SettingType.BOOLEAN)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase("widget1") } returns flowOf(settings)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.widgets.size)
        assertEquals(1, state.widgets[0].settings.size)
        assertEquals("setting1", state.widgets[0].settings[0].key)
        assertEquals(true, state.widgets[0].settings[0].value)
    }

    @Test
    fun testLoadWidgetsError() = runTest {
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flow {
            throw Exception("Test error")
        }

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Test error", state.error)
    }

    @Test
    fun testMoveWidgetUp() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onMoveUp("widget2")

        coVerify {
            swapWidgetPositionsUseCase(SwapWidgetPositionsUseCase.Params("widget2", "widget1"))
        }
    }

    @Test
    fun testMoveWidgetUpWhenAlreadyFirst() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onMoveUp("widget1")

        coVerify(exactly = 0) { swapWidgetPositionsUseCase(any()) }
    }

    @Test
    fun testMoveWidgetDown() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onMoveDown("widget1")

        coVerify {
            swapWidgetPositionsUseCase(SwapWidgetPositionsUseCase.Params("widget1", "widget2"))
        }
    }

    @Test
    fun testMoveWidgetDownWhenAlreadyLast() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true),
            WidgetMetadata(id = "widget2", position = 1, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onMoveDown("widget2")

        coVerify(exactly = 0) { swapWidgetPositionsUseCase(any()) }
    }

    @Test
    fun testToggleVisibility() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onToggleVisibility("widget1")

        coVerify {
            toggleWidgetVisibilityUseCase(
                match { params ->
                    params.widgetId == "widget1" &&
                    params.widgets.size == 1 &&
                    params.widgets[0].id == "widget1"
                }
            )
        }
    }

    @Test
    fun testToggleVisibilityWhenHidden() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = false, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onToggleVisibility("widget1")

        coVerify {
            toggleWidgetVisibilityUseCase(
                match { params ->
                    params.widgetId == "widget1" &&
                    params.widgets.size == 1 &&
                    params.widgets[0].id == "widget1"
                }
            )
        }
    }

    @Test
    fun testUpdateSetting() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = "widget1", position = 0, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()
        viewModel.uiState.value.onUpdateSetting("widget1", "key1", "value1")

        coVerify {
            updateWidgetConfigUseCase(
                UpdateWidgetConfigUseCase.Params("widget1", "key1", "value1")
            )
        }
    }

    @Test
    fun testDashboardRedesignEnabledWhenGlobalConfigIsTrue() = runTest {
        viewModel = createViewModel(GlobalConfig(newDashboardEnabled = true))

        assertTrue(viewModel.uiState.value.isDashboardRedesignEnabled)
    }

    @Test
    fun testDashboardRedesignDisabledWhenGlobalConfigIsFalse() = runTest {
        viewModel = createViewModel(GlobalConfig(newDashboardEnabled = false))

        assertFalse(viewModel.uiState.value.isDashboardRedesignEnabled)
    }

    @Test
    fun testDashboardRedesignDefaultsToTrue() = runTest {
        viewModel = createViewModel(GlobalConfig())

        assertTrue(viewModel.uiState.value.isDashboardRedesignEnabled)
    }

    @Test
    fun testToggleDashboardRedesignCallsUseCase() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.value.onToggleDashboardRedesign(false)

        coVerify {
            updateNewDashboardPreferenceUseCase(UpdateNewDashboardPreferenceUseCase.Params(false))
        }
    }

    @Test
    fun testToggleDashboardRedesignEnableCallsUseCase() = runTest {
        viewModel = createViewModel(GlobalConfig(newDashboardEnabled = false))
        viewModel.uiState.value.onToggleDashboardRedesign(true)

        coVerify {
            updateNewDashboardPreferenceUseCase(UpdateNewDashboardPreferenceUseCase.Params(true))
        }
    }

    @Test
    fun testGetDisplayNameForWelcomeWidget() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = WidgetMetadata.WIDGET_ID_WELCOME, position = 0, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Hello, Test User", state.widgets[0].displayName)
    }

    @Test
    fun testGetDisplayNameForTodoWidget() = runTest {
        val metadata = listOf(
            WidgetMetadata(id = WidgetMetadata.WIDGET_ID_TODO, position = 0, isVisible = true, isEditable = true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(metadata)
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())
        every { resources.getString(R.string.widget_toDo) } returns "To Do"

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("To Do", state.widgets[0].displayName)
    }

    @Test
    fun testGlobalSettingsLoaded() = runTest {
        val globalSettings = listOf(
            WidgetSettingItem(key = "globalSetting1", value = 0xFF0000FF.toInt(), type = SettingType.COLOR),
            WidgetSettingItem(key = "globalSetting2", value = true, type = SettingType.BOOLEAN)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase(WidgetMetadata.WIDGET_ID_GLOBAL) } returns flowOf(globalSettings)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.globalSettings.size)
        assertEquals("globalSetting1", state.globalSettings[0].key)
        assertEquals(0xFF0000FF.toInt(), state.globalSettings[0].value)
        assertEquals("globalSetting2", state.globalSettings[1].key)
        assertEquals(true, state.globalSettings[1].value)
    }

    @Test
    fun testGlobalSettingsEmptyByDefault() = runTest {
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase(WidgetMetadata.WIDGET_ID_GLOBAL) } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(0, state.globalSettings.size)
    }

    @Test
    fun testTrackDashboardSurvey() = runTest {
        viewModel = createViewModel()
        viewModel.trackDashboardSurvey(AnalyticsEventConstants.SURVEY_OPTION_HARD_TO_FIND)

        val bundleSlot = slot<Bundle>()
        verify {
            analytics.logEvent(
                AnalyticsEventConstants.DASHBOARD_SURVEY_SUBMITTED,
                capture(bundleSlot)
            )
        }

        assertEquals(AnalyticsEventConstants.SURVEY_OPTION_HARD_TO_FIND, bundleSlot.captured.getString(AnalyticsParamConstants.SELECTED_REASON))
    }

    @Test
    fun testTrackDashboardSurveyWithEmptyFeedback() = runTest {
        viewModel = createViewModel()
        viewModel.trackDashboardSurvey(AnalyticsEventConstants.SURVEY_OPTION_PREFER_OLD_LAYOUT)

        val bundleSlot = slot<Bundle>()
        verify {
            analytics.logEvent(
                AnalyticsEventConstants.DASHBOARD_SURVEY_SUBMITTED,
                capture(bundleSlot)
            )
        }

        assertEquals(AnalyticsEventConstants.SURVEY_OPTION_PREFER_OLD_LAYOUT, bundleSlot.captured.getString(AnalyticsParamConstants.SELECTED_REASON))
    }
}