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

package com.instructure.parentapp.features.managestudents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ThemedColor


data class ManageStudentsUiState(
    val isLoading: Boolean = false,
    val isLoadError: Boolean = false,
    val studentListItems: List<StudentItemUiState> = emptyList(),
    val colorPickerDialogUiState: ColorPickerDialogUiState = ColorPickerDialogUiState()
)

data class UserColor(
    val colorRes: Int = 0,
    val color: ThemedColor = ThemedColor(Color.Black.toArgb()),
    val contentDescriptionRes: Int = 0
)

data class ColorPickerDialogUiState(
    val showColorPickerDialog: Boolean = false,
    val studentId: Long = 0,
    val initialUserColor: UserColor? = null,
    val userColors: List<UserColor> = emptyList(),
    val isSavingColor: Boolean = false,
    val isSavingColorError: Boolean = false
)

data class StudentItemUiState(
    val studentId: Long = 0,
    val avatarUrl: String? = null,
    val studentName: String = "",
    val studentPronouns: String? = null,
    val studentColor: ThemedColor = ThemedColor(Color.Black.toArgb())
)

sealed class ManageStudentsAction {
    data class StudentTapped(val studentId: Long) : ManageStudentsAction()
    data object Refresh : ManageStudentsAction()
    data object AddStudent : ManageStudentsAction()
    data class ShowColorPickerDialog(val studentId: Long, val studentColor: ThemedColor) : ManageStudentsAction()
    data object HideColorPickerDialog : ManageStudentsAction()
    data class StudentColorChanged(val studentId: Long, val userColor: UserColor) : ManageStudentsAction()
}

sealed class ManageStudentsViewModelAction {
    data class NavigateToAlertSettings(val student: User) : ManageStudentsViewModelAction()
    data object AddStudent : ManageStudentsViewModelAction()
    data class AccessibilityAnnouncement(val announcement: String) : ManageStudentsViewModelAction()
}
