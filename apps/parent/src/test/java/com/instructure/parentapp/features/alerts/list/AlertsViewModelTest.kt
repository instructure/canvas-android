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
package com.instructure.parentapp.features.alerts.list

import android.graphics.Color
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.AlertCountUpdater
import com.instructure.parentapp.features.dashboard.TestSelectStudentHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.time.Instant
import java.util.Date

@ExperimentalCoroutinesApi
class AlertsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: AlertsRepository = mockk(relaxed = true)
    private val alertCountUpdater: AlertCountUpdater = mockk(relaxed = true)
    private val selectedStudentFlow = MutableStateFlow<User?>(null)
    private val selectedStudentHolder = TestSelectStudentHolder(selectedStudentFlow)

    private lateinit var viewModel: AlertsViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(User::studentColor)

        coEvery { repository.getAlertThresholdForStudent(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Load alerts on student change`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            ),
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-01T00:00:00Z")
                ),
                title = "Alert 2",
                workflowState = AlertWorkflowState.UNREAD,
                alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                htmlUrl = "https://example.com/alert2",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 2L,
                observerId = 1L,
                userId = 2L
            ),
        )

        val thresholds = listOf(
            AlertThreshold(
                id = 1L,
                observerId = 1L,
                threshold = null,
                alertType = AlertType.ASSIGNMENT_MISSING,
                userId = 1L,
                workflowState = ThresholdWorkflowState.ACTIVE
            ),
            AlertThreshold(
                id = 2L,
                observerId = 1L,
                threshold = "50%",
                alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                userId = 1L,
                workflowState = ThresholdWorkflowState.ACTIVE
            )
        )

        coEvery {
            repository.getAlertsForStudent(student.id, any())
        } returns alerts

        coEvery { repository.getAlertThresholdForStudent(student.id, any()) } returns thresholds


        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = thresholds.find { threshold -> threshold.alertType == it.alertType }?.threshold,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Empty state`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        coEvery {
            repository.getAlertsForStudent(student.id, any())
        } returns emptyList()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = emptyList(),
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Error state if the student is not set`() = runTest {
        createViewModel()

        viewModel.handleAction(AlertsAction.Refresh)

        val expected = AlertsUiState(
            isLoading = false,
            isError = true,
            alerts = emptyList(),
            studentColor = Color.BLACK
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Error state if getting alerts fail`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        coEvery {
            repository.getAlertsForStudent(student.id, any())
        } throws Exception()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = true,
            alerts = emptyList(),
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Refresh data`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns emptyList()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = emptyList(),
            studentColor = 1
        )
        assertEquals(expected, viewModel.uiState.value)

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts

        viewModel.handleAction(AlertsAction.Refresh)

        val expectedRefreshed = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expectedRefreshed, viewModel.uiState.value)
    }

    @Test
    fun `Dismiss alert`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts
        coEvery { repository.updateAlertWorkflow(any(), any()) } returns mockk()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(AlertsAction.DismissAlert(1L))
        assertEquals(emptyList<AlertsItemUiState>(), viewModel.uiState.value.alerts)

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            R.string.alertDismissMessage,
            (events.last() as AlertsViewModelAction.ShowSnackbar).message
        )
        assertEquals(
            R.string.alertDismissAction,
            (events.last() as AlertsViewModelAction.ShowSnackbar).action
        )
    }

    @Test
    fun `Dismiss error resets event`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts
        coEvery { repository.updateAlertWorkflow(any(), any()) } throws Exception()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(AlertsAction.DismissAlert(1L))

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            R.string.alertDismissErrorMessage,
            (events.last() as AlertsViewModelAction.ShowSnackbar).message
        )
        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Undo dismissal`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts
        coEvery { repository.updateAlertWorkflow(any(), any()) } returns mockk()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(AlertsAction.DismissAlert(1L))
        assertEquals(emptyList<AlertsItemUiState>(), viewModel.uiState.value.alerts)

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            R.string.alertDismissMessage,
            (events.last() as AlertsViewModelAction.ShowSnackbar).message
        )
        assertEquals(
            R.string.alertDismissAction,
            (events.last() as AlertsViewModelAction.ShowSnackbar).action
        )

        (events.last() as AlertsViewModelAction.ShowSnackbar).actionCallback?.invoke()

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Undo does not reset event on error`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.ASSIGNMENT_MISSING,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts
        coEvery { repository.updateAlertWorkflow(any(), any()) } returns mockk()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(AlertsAction.DismissAlert(1L))
        assertEquals(emptyList<AlertsItemUiState>(), viewModel.uiState.value.alerts)

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        coEvery { repository.updateAlertWorkflow(any(), any()) } throws Exception()
        (events.last() as AlertsViewModelAction.ShowSnackbar).actionCallback?.invoke()

        assertEquals(emptyList<AlertsItemUiState>(), viewModel.uiState.value.alerts)
    }

    @Test
    fun `Navigate to Course Announcement`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.COURSE_ANNOUNCEMENT,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(
            AlertsAction.Navigate(
                1L,
                1L,
                "https://example.com/alert1",
                AlertType.COURSE_ANNOUNCEMENT
            )
        )

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            AlertsViewModelAction.NavigateToRoute(
                "https://example.com/alert1"
            ), events.last()
        )
    }

    @Test
    fun `Navigate to Global Announcement`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.READ,
                alertType = AlertType.INSTITUTION_ANNOUNCEMENT,
                htmlUrl = "https://example.com/alert1",
                contextId = 10L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = it.workflowState == AlertWorkflowState.UNREAD,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        assertEquals(expected, viewModel.uiState.value)

        viewModel.handleAction(
            AlertsAction.Navigate(
                1L,
                10L,
                "https://example.com/alert1",
                AlertType.INSTITUTION_ANNOUNCEMENT
            )
        )

        val events = mutableListOf<AlertsViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            AlertsViewModelAction.NavigateToGlobalAnnouncement(
                10L
            ), events.last()
        )
    }

    @Test
    fun `Navigation to alert marks it read`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.UNREAD,
                alertType = AlertType.COURSE_ANNOUNCEMENT,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.updateAlertWorkflow(1L, AlertWorkflowState.READ) } returns mockk()
        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = false,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        viewModel.handleAction(
            AlertsAction.Navigate(
                1L,
                1L,
                "https://example.com/alert1",
                AlertType.COURSE_ANNOUNCEMENT
            )
        )

        val events = mutableListOf<AlertsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            AlertsViewModelAction.NavigateToRoute(
                "https://example.com/alert1"
            ), events.last()
        )

        assertEquals(expected, viewModel.uiState.value)
        coVerify {
            repository.updateAlertWorkflow(1L, AlertWorkflowState.READ)
        }
    }

    @Test
    fun `If marking the alert read fails the alert will remain read until refresh`() = runTest {
        val student = User(1L)
        every { student.studentColor } returns 1

        val alerts = listOf(
            Alert(
                id = 1,
                actionDate = Date.from(
                    Instant.parse("2024-01-03T00:00:00Z")
                ),
                title = "Alert 1",
                workflowState = AlertWorkflowState.UNREAD,
                alertType = AlertType.COURSE_ANNOUNCEMENT,
                htmlUrl = "https://example.com/alert1",
                contextId = 1L,
                contextType = "Course",
                lockedForUser = false,
                observerAlertThresholdId = 1L,
                observerId = 1L,
                userId = 2L
            )
        )

        coEvery { repository.updateAlertWorkflow(1L, AlertWorkflowState.READ) } throws Exception()
        coEvery { repository.getAlertsForStudent(student.id, any()) } returns alerts

        createViewModel()
        selectedStudentFlow.emit(student)

        val expected = AlertsUiState(
            isLoading = false,
            isError = false,
            alerts = alerts.map {
                AlertsItemUiState(
                    alertId = it.id,
                    contextId = it.contextId,
                    title = it.title,
                    alertType = it.alertType,
                    date = it.actionDate,
                    observerAlertThreshold = null,
                    lockedForUser = it.lockedForUser,
                    unread = false,
                    htmlUrl = it.htmlUrl
                )
            }.sortedByDescending { it.date },
            studentColor = 1
        )

        viewModel.handleAction(
            AlertsAction.Navigate(
                1L,
                1L,
                "https://example.com/alert1",
                AlertType.COURSE_ANNOUNCEMENT
            )
        )

        val events = mutableListOf<AlertsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(
            AlertsViewModelAction.NavigateToRoute(
                "https://example.com/alert1"
            ), events.last()
        )

        assertEquals(expected, viewModel.uiState.value)

    }

    @Test
    fun `Change color when student color is changed`() = runTest {
        val student = User(1L)
        mockkStatic(User::studentColor)
        every { student.studentColor } returns 1
        createViewModel()
        selectedStudentFlow.emit(student)

        assertEquals(1, viewModel.uiState.value.studentColor)

        every { student.studentColor } returns 2
        selectedStudentHolder.selectedStudentColorChanged()

        assertEquals(2, viewModel.uiState.value.studentColor)
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel =
            AlertsViewModel(repository, selectedStudentHolder, alertCountUpdater)
    }
}