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
 */    package com.instructure.parentapp.features.alerts.settings

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.User


data class AlertSettingsUiState(
    val student: User,
    @ColorInt val userColor: Int,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val avatarUrl: String,
    val studentName: String,
    val studentPronouns: String?,
    val thresholds: Map<AlertType, AlertThreshold> = mutableMapOf(),
    val actionHandler: (AlertSettingsAction) -> Unit
)

sealed class AlertSettingsAction {
    data class CreateThreshold(val alertType: AlertType, val threshold: String?) : AlertSettingsAction()
    data class DeleteThreshold(val alertType: AlertType) : AlertSettingsAction()
    data class UnpairStudent(val studentId: Long) : AlertSettingsAction()
    data object UnpairStudentFailed : AlertSettingsAction()
    data object ReloadAlertSettings : AlertSettingsAction()
}

sealed class AlertSettingsViewModelAction {
    data class UnpairStudent(val studentId: Long) : AlertSettingsViewModelAction()

    data class ShowSnackbar(@StringRes val message: Int, val actionCallback: () -> Unit) : AlertSettingsViewModelAction()
}