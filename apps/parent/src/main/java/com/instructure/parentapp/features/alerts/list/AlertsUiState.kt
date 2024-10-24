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
 */    package com.instructure.parentapp.features.alerts.list

import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.AlertType
import java.util.Date

data class AlertsUiState(
    val alerts: List<AlertsItemUiState> = emptyList(),
    @ColorInt val studentColor: Int = Color.BLACK,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false
)

data class AlertsItemUiState(
    val alertId: Long,
    val contextId: Long,
    val title: String,
    val alertType: AlertType,
    val date: Date?,
    val observerAlertThreshold: String?,
    val lockedForUser: Boolean,
    val unread: Boolean,
    val htmlUrl: String?
)

sealed class AlertsViewModelAction {
    data class NavigateToRoute(val route: String?): AlertsViewModelAction()
    data class NavigateToGlobalAnnouncement(val alertId: Long): AlertsViewModelAction()
    data class ShowSnackbar(val message: Int, val action: Int?, val actionCallback: (() -> Unit)?): AlertsViewModelAction()
}

sealed class AlertsAction {
    data object Refresh : AlertsAction()
    data class Navigate(val alertId: Long, val contextId: Long, val route: String?, val alertType: AlertType) : AlertsAction()
    data class DismissAlert(val alertId: Long) : AlertsAction()
}