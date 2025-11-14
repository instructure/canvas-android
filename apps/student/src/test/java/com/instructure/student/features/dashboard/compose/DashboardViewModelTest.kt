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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.EnsureDefaultWidgetsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
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

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { networkStateProvider.isOnline() } returns true
        coEvery { observeWidgetMetadataUseCase(Unit) } returns flowOf(emptyList())

        viewModel = createViewModel()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(networkStateProvider, ensureDefaultWidgetsUseCase, observeWidgetMetadataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testInitialState() = runTest {
        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals(null, state.error)
        assertFalse(state.refreshing)
    }

    @Test
    fun testLoadDashboardSuccess() = runTest {
        val state = viewModel.uiState.value

        assertEquals(false, state.loading)
        assertEquals(null, state.error)
    }

    @Test
    fun testRefresh() = runTest {
        viewModel.uiState.value.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.refreshing)
        assertEquals(null, state.error)
    }

    @Test
    fun testRetry() = runTest {
        viewModel.uiState.value.onRetry()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals(null, state.error)
    }

    @Test
    fun testCallbacksExist() {
        val state = viewModel.uiState.value

        assertTrue(state.onRefresh != null)
        assertTrue(state.onRetry != null)
    }

    @Test
    fun testEnsureDefaultWidgetsCalledOnInit() = runTest {
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
}