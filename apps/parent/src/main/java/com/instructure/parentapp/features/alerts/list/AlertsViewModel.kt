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

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.AlertCountUpdater
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: AlertsRepository,
    private val selectedStudentHolder: SelectedStudentHolder,
    private val alertCountUpdater: AlertCountUpdater
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AlertsViewModelAction>()
    val events = _events.receiveAsFlow()

    private var selectedStudent: User? = null
    private var thresholds: Map<Long, AlertThreshold> = emptyMap()

    val lazyListState = LazyListState()

    init {
        viewModelScope.launch {
            selectedStudentHolder.selectedStudentState.collectLatest {
                studentChanged(it)
            }
        }

        viewModelScope.launch {
            selectedStudentHolder.selectedStudentColorChanged.collect {
                updateColor()
            }
        }
    }

    private fun updateColor() {
        selectedStudent?.let { student ->
            _uiState.update {
                it.copy(studentColor = student.studentColor)
            }
        }
    }

    private suspend fun studentChanged(student: User?) {
        if (selectedStudent != student) {
            selectedStudent = student
            _uiState.update {
                it.copy(
                    studentColor = student.studentColor,
                    isLoading = true
                )
            }
            loadThresholds()
            loadAlerts()
        }
    }

    private suspend fun loadThresholds(forceNetwork: Boolean = false) {
        selectedStudent?.let { student ->
            val thresholds = repository.getAlertThresholdForStudent(student.id, forceNetwork)
            this.thresholds = thresholds.associateBy { it.id }
        }
    }

    private suspend fun loadAlerts(forceNetwork: Boolean = false) {
        selectedStudent?.let { student ->
            try {
                val alerts = repository.getAlertsForStudent(student.id, forceNetwork)
                val alertItems = alerts.map { createAlertItem(it) }
                _uiState.update {
                    it.copy(
                        alerts = alertItems,
                        isLoading = false,
                        isError = false,
                        isRefreshing = false,
                    )
                }
            } catch (e: Exception) {
                setError()
            }
        } ?: setError()

        alertCountUpdater.updateShouldRefreshAlertCount(true)
    }

    private fun setError() {
        _uiState.update {
            it.copy(isLoading = false, isError = true, isRefreshing = false, alerts = emptyList())
        }
    }

    fun handleAction(action: AlertsAction) {
        when (action) {
            is AlertsAction.Navigate -> {
                viewModelScope.launch {
                        when (action.alertType) {
                            AlertType.INSTITUTION_ANNOUNCEMENT -> {
                                _events.send(AlertsViewModelAction.NavigateToGlobalAnnouncement(action.contextId))
                            }
                            else -> {
                                _events.send(AlertsViewModelAction.NavigateToRoute(action.route))
                            }
                        }
                    markAlertRead(action.alertId)
                    alertCountUpdater.updateShouldRefreshAlertCount(true)
                }
            }

            is AlertsAction.Refresh -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isRefreshing = true) }
                    loadThresholds(true)
                    loadAlerts(true)
                }
            }

            is AlertsAction.DismissAlert -> {
                viewModelScope.launch {
                    dismissAlert(action.alertId)
                }
            }
        }
    }

    private suspend fun markAlertRead(alertId: Long) {
        try {
            _uiState.update { uiState ->
                uiState.copy(
                    alerts = uiState.alerts.map { alertItem ->
                        if (alertItem.alertId == alertId) alertItem.copy(unread = false) else alertItem
                    }
                )
            }
            repository.updateAlertWorkflow(alertId, AlertWorkflowState.READ)
            alertCountUpdater.updateShouldRefreshAlertCount(true)
        } catch (e: Exception) {
            //No need to do anything. The alert will stay read.
        }
    }

    private suspend fun dismissAlert(alertId: Long) {
        fun resetAlert(alert: AlertsItemUiState) {
            val alerts = _uiState.value.alerts.toMutableList()
            alerts.add(alert)
            alerts.sortByDescending { it.date }
            viewModelScope.launch {
                _uiState.update { it.copy(alerts = alerts) }
                alertCountUpdater.updateShouldRefreshAlertCount(true)
            }
        }

        val alerts = _uiState.value.alerts.toMutableList()
        val alert = alerts.find { it.alertId == alertId } ?: return
        alerts.removeIf { it.alertId == alertId }
        _uiState.update { it.copy(alerts = alerts) }

        try {
            repository.updateAlertWorkflow(alertId, AlertWorkflowState.DISMISSED)
            alertCountUpdater.updateShouldRefreshAlertCount(true)
            _events.send(AlertsViewModelAction.ShowSnackbar(R.string.alertDismissMessage, R.string.alertDismissAction) {
                viewModelScope.launch {
                    try {
                        repository.updateAlertWorkflow(
                            alert.alertId,
                            if (alert.unread) AlertWorkflowState.UNREAD else AlertWorkflowState.READ
                        )
                        resetAlert(alert)
                    } catch (e: Exception) {
                        _events.send(AlertsViewModelAction.ShowSnackbar(R.string.alertDismissActionErrorMessage, null, null))
                    }
                }
            })
        } catch (e: Exception) {
            _events.send(AlertsViewModelAction.ShowSnackbar(R.string.alertDismissErrorMessage, null, null))
            resetAlert(alert)
        }
    }

    private fun createAlertItem(alert: Alert): AlertsItemUiState {
        return AlertsItemUiState(
            alertId = alert.id,
            contextId = alert.contextId,
            title = alert.title,
            alertType = alert.alertType,
            date = alert.actionDate,
            observerAlertThreshold = thresholds[alert.observerAlertThresholdId]?.threshold,
            lockedForUser = alert.lockedForUser,
            unread = alert.workflowState == AlertWorkflowState.UNREAD,
            htmlUrl = alert.htmlUrl
        )
    }
}