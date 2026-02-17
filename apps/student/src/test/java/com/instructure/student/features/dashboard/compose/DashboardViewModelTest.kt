/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.compose

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.EnsureDefaultWidgetsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.clearMocks
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class DashboardViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val ensureDefaultWidgetsUseCase: EnsureDefaultWidgetsUseCase = mockk(relaxed = true)
    private val observeWidgetMetadataUseCase: ObserveWidgetMetadataUseCase = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val bundleStorage = mutableMapOf<String, String?>()
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

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

        // Clear bundle storage and analytics before each test
        bundleStorage.clear()
        clearMocks(analytics, answers = false)

        every { networkStateProvider.isOnline() } returns true
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig())
        every { crashlytics.recordException(any()) } just Runs

        mockkObject(ColorKeeper)
        every { ColorKeeper.createThemedColor(any()) } returns ThemedColor(0, 0)
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(networkStateProvider, ensureDefaultWidgetsUseCase, observeWidgetMetadataUseCase, analytics, observeGlobalConfigUseCase,
            crashlytics)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testInitialState() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals(null, state.error)
        assertFalse(state.refreshing)
    }

    @Test
    fun testLoadDashboardSuccess() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(false, state.loading)
        assertEquals(null, state.error)
    }

    @Test
    fun testRefresh() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.value.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.refreshing)
        assertEquals(null, state.error)
    }

    @Test
    fun testRetry() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.value.onRetry()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals(null, state.error)
    }

    @Test
    fun testCallbacksExist() {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.onRefresh != null)
        assertTrue(state.onRetry != null)
    }

    @Test
    fun testEnsureDefaultWidgetsCalledOnInit() = runTest {
        viewModel = createViewModel()
        coVerify { ensureDefaultWidgetsUseCase(Unit) }
    }

    @Test
    fun testWidgetsLoadedFromUseCase() = runTest {
        val widgets = listOf(
            WidgetMetadata("widget1", 0, true),
            WidgetMetadata("widget2", 1, true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.widgets.size)
        assertEquals("widget1", state.widgets[0].id)
        assertEquals("widget2", state.widgets[1].id)
    }

    @Test
    fun testOnlyVisibleWidgetsShown() = runTest {
        val widgets = listOf(
            WidgetMetadata("widget1", 0, true),
            WidgetMetadata("widget2", 1, false),
            WidgetMetadata("widget3", 2, true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(2, state.widgets.size)
        assertEquals("widget1", state.widgets[0].id)
        assertEquals("widget3", state.widgets[1].id)
    }

    @Test
    fun testEmptyWidgetsList() = runTest {
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(0, state.widgets.size)
    }

    @Test
    fun testLoadDashboardError() = runTest {
        coEvery { observeWidgetMetadataUseCase(Unit) } throws Exception("Test error")

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals("Test error", state.error)
    }

    @Test
    fun testShowSnackbarEmitsMessage() = runTest {
        viewModel = createViewModel()
        val message = "Test message"
        val messages = mutableListOf<SnackbarMessage>()

        val job = launch(testDispatcher) {
            viewModel.snackbarMessage.collect { snackbarMessage ->
                messages.add(snackbarMessage)
            }
        }

        viewModel.showSnackbar(message)
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals(message, messages[0].message)
        assertEquals(null, messages[0].actionLabel)
        assertEquals(null, messages[0].action)

        job.cancel()
    }

    @Test
    fun testShowSnackbarWithActionEmitsMessageAndAction() = runTest {
        viewModel = createViewModel()
        val message = "Test message"
        val actionLabel = "Retry"
        var actionInvoked = false
        val action: () -> Unit = { actionInvoked = true }

        val messages = mutableListOf<SnackbarMessage>()

        val job = launch(testDispatcher) {
            viewModel.snackbarMessage.collect { snackbarMessage ->
                messages.add(snackbarMessage)
            }
        }

        viewModel.showSnackbar(message, actionLabel, action)
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals(message, messages[0].message)
        assertEquals(actionLabel, messages[0].actionLabel)
        messages[0].action?.invoke()
        assertTrue(actionInvoked)

        job.cancel()
    }

    @Test
    fun testObserveNetworkStateEmitsRefreshSignalOnNetworkChange() = runTest {
        val networkStateLiveData = MutableLiveData<Boolean>()
        every { networkStateProvider.isOnlineLiveData } returns networkStateLiveData

        // Create new ViewModel to start observing
        viewModel = createViewModel()
        advanceUntilIdle()

        val refreshSignals = mutableListOf<Unit>()
        val job = launch(testDispatcher) {
            viewModel.refreshSignal.collect {
                refreshSignals.add(Unit)
            }
        }

        // Simulate network state change from online to offline
        networkStateLiveData.postValue(false)
        advanceUntilIdle()

        // Simulate network state change from offline to online
        networkStateLiveData.postValue(true)
        advanceUntilIdle()

        // Should have received 2 refresh signals (one for each network state change)
        assertEquals(2, refreshSignals.size)

        job.cancel()
    }

    @Test
    fun testObserveNetworkStateEmitsRefreshSignalWhenGoingOnline() = runTest {
        val networkStateLiveData = MutableLiveData(false)
        every { networkStateProvider.isOnlineLiveData } returns networkStateLiveData

        // Create new ViewModel to start observing
        viewModel = createViewModel()
        advanceUntilIdle()

        val refreshSignals = mutableListOf<Unit>()
        val job = launch(testDispatcher) {
            viewModel.refreshSignal.collect {
                refreshSignals.add(Unit)
            }
        }

        // Initially offline, no signals yet
        assertEquals(0, refreshSignals.size)

        // Simulate going online
        networkStateLiveData.postValue(true)
        advanceUntilIdle()

        // Should have received 1 refresh signal
        assertEquals(1, refreshSignals.size)

        job.cancel()
    }

    @Test
    fun testObserveNetworkStateEmitsRefreshSignalWhenGoingOffline() = runTest {
        val networkStateLiveData = MutableLiveData(true)
        every { networkStateProvider.isOnlineLiveData } returns networkStateLiveData

        // Create new ViewModel to start observing
        viewModel = createViewModel()
        advanceUntilIdle()

        val refreshSignals = mutableListOf<Unit>()
        val job = launch(testDispatcher) {
            viewModel.refreshSignal.collect {
                refreshSignals.add(Unit)
            }
        }

        // Initially online, no signals yet
        assertEquals(0, refreshSignals.size)

        // Simulate going offline
        networkStateLiveData.postValue(false)
        advanceUntilIdle()

        // Should have received 1 refresh signal
        assertEquals(1, refreshSignals.size)

        job.cancel()
    }

    @Test
    fun testWidgetVisibilityTrackedOnLoad() = runTest {
        val widgets = listOf(
            WidgetMetadata("welcome", 0, true),
            WidgetMetadata("forecast", 1, false),
            WidgetMetadata("todo", 2, true),
            WidgetMetadata("courses", 3, true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)

        val eventNameSlot = slot<String>()
        val bundleSlot = slot<Bundle>()
        every { analytics.logEvent(capture(eventNameSlot), capture(bundleSlot)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        verify(exactly = 1) { analytics.logEvent(any(), any<Bundle>()) }
        assertEquals(AnalyticsEventConstants.DASHBOARD_WIDGET_VISIBILITY, eventNameSlot.captured)

        val capturedBundle = bundleSlot.captured
        assertEquals("0", capturedBundle.getString("welcome"))
        assertEquals("-1", capturedBundle.getString("forecast"))
        assertEquals("2", capturedBundle.getString("todo"))
        assertEquals("3", capturedBundle.getString("courses"))
    }

    @Test
    fun testWidgetVisibilityTrackedOnlyOnce() = runTest {
        val widgets = listOf(
            WidgetMetadata("welcome", 0, true),
            WidgetMetadata("todo", 1, false)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)
        every { analytics.logEvent(any(), any<Bundle>()) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        verify(exactly = 1) { analytics.logEvent(AnalyticsEventConstants.DASHBOARD_WIDGET_VISIBILITY, any()) }
    }

    @Test
    fun testWidgetVisibilityIncludesHiddenWidgets() = runTest {
        val widgets = listOf(
            WidgetMetadata("widget1", 0, true),
            WidgetMetadata("widget2", 1, false),
            WidgetMetadata("widget3", 2, false)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)

        val bundleSlot = slot<Bundle>()
        every { analytics.logEvent(any(), capture(bundleSlot)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        val capturedBundle = bundleSlot.captured
        assertEquals("0", capturedBundle.getString("widget1"))
        assertEquals("-1", capturedBundle.getString("widget2"))
        assertEquals("-1", capturedBundle.getString("widget3"))
    }

    @Test
    fun testWidgetPositionTrackedCorrectly() = runTest {
        val widgets = listOf(
            WidgetMetadata("welcome", 0, true),
            WidgetMetadata("forecast", 3, true),
            WidgetMetadata("todo", 5, false),
            WidgetMetadata("courses", 1, true)
        )
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(widgets)

        val bundleSlot = slot<Bundle>()
        every { analytics.logEvent(any(), capture(bundleSlot)) } returns Unit

        viewModel = createViewModel()
        advanceUntilIdle()

        val capturedBundle = bundleSlot.captured
        // Visible widgets should report their actual position
        assertEquals("0", capturedBundle.getString("welcome"))
        assertEquals("3", capturedBundle.getString("forecast"))
        assertEquals("1", capturedBundle.getString("courses"))
        // Hidden widgets should report -1
        assertEquals("-1", capturedBundle.getString("todo"))
    }

    @Test
    fun testObserveConfigUpdatesColor() = runTest {
        val testColor = 0xFF00FF00.toInt()
        val themedColor = ThemedColor(testColor, testColor)
        every { ColorKeeper.createThemedColor(testColor) } returns themedColor
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig(backgroundColor = testColor))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(themedColor, state.color)
    }
}
