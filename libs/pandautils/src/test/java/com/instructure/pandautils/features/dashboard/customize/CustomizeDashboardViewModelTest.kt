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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.SwapWidgetPositionsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.UpdateWidgetSettingUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.UpdateWidgetVisibilityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    private val updateWidgetVisibilityUseCase: UpdateWidgetVisibilityUseCase = mockk(relaxed = true)
    private val observeWidgetConfigUseCase: ObserveWidgetConfigUseCase = mockk(relaxed = true)
    private val updateWidgetSettingUseCase: UpdateWidgetSettingUseCase = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val remoteConfigUtils: RemoteConfigUtils = mockk(relaxed = true)
    private val remoteConfigPrefs: RemoteConfigPrefs = mockk(relaxed = true)

    private lateinit var viewModel: CustomizeDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val user = User(id = 1, shortName = "Test User")
        every { apiPrefs.user } returns user
        every { resources.getString(R.string.widget_hello, any()) } returns "Hello, Test User"
        every { remoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN) } returns false
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())
        coEvery { observeWidgetConfigUseCase(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): CustomizeDashboardViewModel {
        return CustomizeDashboardViewModel(
            observeWidgetMetadataUseCase,
            swapWidgetPositionsUseCase,
            updateWidgetVisibilityUseCase,
            observeWidgetConfigUseCase,
            updateWidgetSettingUseCase,
            resources,
            apiPrefs,
            remoteConfigUtils,
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
            updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget1", false))
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
            updateWidgetVisibilityUseCase(UpdateWidgetVisibilityUseCase.Params("widget1", true))
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
            updateWidgetSettingUseCase(
                UpdateWidgetSettingUseCase.Params("widget1", "key1", "value1")
            )
        }
    }

    @Test
    fun testDashboardRedesignFlagLoadedOnInit() = runTest {
        every { remoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN) } returns true

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isDashboardRedesignEnabled)
    }

    @Test
    fun testToggleDashboardRedesignFlag() = runTest {
        every { remoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN) } returns false

        viewModel = createViewModel()
        viewModel.uiState.value.onToggleDashboardRedesign(true)

        coVerify {
            remoteConfigPrefs.putString(RemoteConfigParam.DASHBOARD_REDESIGN.rc_name, "true")
        }
        assertTrue(viewModel.uiState.value.isDashboardRedesignEnabled)
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
}
