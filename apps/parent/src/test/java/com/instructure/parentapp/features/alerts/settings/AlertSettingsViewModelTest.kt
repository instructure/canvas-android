/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.alerts.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AlertSettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: AlertSettingsViewModel

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: AlertSettingsRepository = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
        every { savedStateHandle.get<User>(any()) } returns User(1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `empty thresholds map correctly`() = runTest {
        coEvery { repository.loadAlertThresholds(any()) } returns emptyList()
        createViewModel()


        assertEquals(emptyMap<AlertType, AlertThreshold>(), viewModel.uiState.value.thresholds)
    }

    @Test
    fun `thresholds map correctly`() = runTest {
        val expected = listOf(
            AlertThreshold(
                1,
                AlertType.ASSIGNMENT_MISSING,
                threshold = null,
                userId = 1,
                observerId = 2,
                workflowState = ThresholdWorkflowState.ACTIVE
            ),
            AlertThreshold(
                2,
                AlertType.ASSIGNMENT_GRADE_HIGH,
                threshold = "80",
                userId = 1,
                observerId = 2,
                workflowState = ThresholdWorkflowState.ACTIVE
            ),
            AlertThreshold(
                3,
                AlertType.ASSIGNMENT_GRADE_LOW,
                threshold = "40",
                userId = 1,
                observerId = 2,
                workflowState = ThresholdWorkflowState.ACTIVE
            )
        )

        coEvery { repository.loadAlertThresholds(any()) } returns expected

        createViewModel()

        assertEquals(expected.associateBy { it.alertType }, viewModel.uiState.value.thresholds)
    }

    @Test
    fun `loadThreshold error state`() = runTest {
        coEvery { repository.loadAlertThresholds(any()) } throws Exception()

        createViewModel()

        assertEquals(true, viewModel.uiState.value.isError)
    }

    @Test
    fun `creating threshold reloads page`() = runTest {
        val alertType = AlertType.ASSIGNMENT_GRADE_HIGH
        val threshold = "80"

        coEvery { repository.createAlertThreshold(any(), any(), any()) } returns Unit

        createViewModel()

        viewModel.uiState.value.actionHandler(
            AlertSettingsAction.CreateThreshold(
                alertType,
                threshold
            )
        )

        coVerify {
            repository.createAlertThreshold(alertType, 1, threshold)
            repository.loadAlertThresholds(1)
        }

        assertEquals(false, viewModel.uiState.value.isError)
    }

    @Test
    fun `createThreshold error`() = runTest {
        val alertType = AlertType.ASSIGNMENT_GRADE_HIGH
        val threshold = "80"

        coEvery { repository.createAlertThreshold(any(), any(), any()) } throws Exception()

        createViewModel()

        viewModel.uiState.value.actionHandler(
            AlertSettingsAction.CreateThreshold(
                alertType,
                threshold
            )
        )

        coVerify {
            crashlytics.recordException(any())
            repository.loadAlertThresholds(any())
        }

        val events = mutableListOf<AlertSettingsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assert(events.last() is AlertSettingsViewModelAction.ShowSnackbar)
    }

    @Test
    fun `deleting threshold reloads page`() = runTest {
        val alertType = AlertType.ASSIGNMENT_GRADE_HIGH

        coEvery { repository.loadAlertThresholds(any()) } returns listOf(
            AlertThreshold(2L, alertType, "80", 1L, 2L, ThresholdWorkflowState.ACTIVE)
        )

        coEvery { repository.deleteAlertThreshold(any()) } returns Unit

        createViewModel()

        viewModel.uiState.value.actionHandler(
            AlertSettingsAction.DeleteThreshold(
                alertType
            )
        )

        coVerify {
            repository.deleteAlertThreshold(2L)
            repository.loadAlertThresholds(1L)
        }

        assertEquals(false, viewModel.uiState.value.isError)
    }

    @Test
    fun `deleteThreshold error`() = runTest {
        val alertType = AlertType.ASSIGNMENT_GRADE_HIGH

        coEvery { repository.loadAlertThresholds(any()) } returns listOf(
            AlertThreshold(2L, alertType, "80", 1L, 2L, ThresholdWorkflowState.ACTIVE)
        )

        coEvery { repository.deleteAlertThreshold(any()) } throws Exception()

        createViewModel()

        viewModel.uiState.value.actionHandler(
            AlertSettingsAction.DeleteThreshold(
                alertType
            )
        )

        coVerify {
            crashlytics.recordException(any())
            repository.loadAlertThresholds(any())
        }

        val events = mutableListOf<AlertSettingsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assert(events.last() is AlertSettingsViewModelAction.ShowSnackbar)
    }

    @Test
    fun `unpair student emits correct event`() = runTest {
        createViewModel()

        viewModel.uiState.value.actionHandler(AlertSettingsAction.UnpairStudent(1))

        val events = mutableListOf<AlertSettingsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val expected = AlertSettingsViewModelAction.UnpairStudent(1L)
        assertEquals(expected, events.last())
    }

    @Test
    fun `unpair student fails`() = runTest {
        createViewModel()

        viewModel.handleAction(AlertSettingsAction.UnpairStudentFailed)

        val events = mutableListOf<AlertSettingsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        val event = events.last() as AlertSettingsViewModelAction.ShowSnackbar
        assertEquals(R.string.generalUnexpectedError, event.message)

        event.actionCallback.invoke()
        assertEquals(AlertSettingsViewModelAction.UnpairStudent(1L), events.last())
    }

    private fun createViewModel() {
        viewModel = AlertSettingsViewModel(savedStateHandle, repository, crashlytics)
    }
}