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
package com.instructure.parentapp.features.addstudent

import androidx.annotation.ColorInt

data class AddStudentUiState(
    @ColorInt val color: Int,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val actionHandler: (AddStudentAction) -> Unit,
)

sealed class AddStudentViewModelAction {
    data object PairStudentSuccess : AddStudentViewModelAction()
    data object UnpairStudentSuccess : AddStudentViewModelAction()
    data object UnpairStudentFailed : AddStudentViewModelAction()
}

sealed class AddStudentAction {
    data class UnpairStudent(val studentId: Long) : AddStudentAction()
    data class PairStudent(val pairingCode: String) : AddStudentAction()
    data object ResetError : AddStudentAction()
}