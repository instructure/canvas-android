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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStudentViewModel @Inject constructor(
    selectedStudentHolder: SelectedStudentHolder,
    private val repository: AddStudentRepository,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AddStudentUiState(
                color = selectedStudentHolder.selectedStudentState.value.studentColor,
                actionHandler = this::handleAction
            )
        )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddStudentViewModelAction>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            selectedStudentHolder.selectedStudentChangedFlow.collectLatest { user ->
                _uiState.value = _uiState.value.copy(
                    color = user.studentColor
                )
            }
        }
    }

    fun handleAction(action: AddStudentAction) {
        when (action) {
            is AddStudentAction.UnpairStudent -> unpairStudent(action.studentId)
            is AddStudentAction.PairStudent -> pairStudent(action.pairingCode)
            is AddStudentAction.ResetError -> resetError()
        }
    }

    private fun pairStudent(pairingCode: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
                repository.pairStudent(pairingCode).dataOrThrow
                _events.emit(AddStudentViewModelAction.PairStudentSuccess)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = _uiState.value.copy(isLoading = false, isError = true)
            }
        }
    }

    private fun unpairStudent(studentId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
                repository.unpairStudent(studentId).dataOrThrow
                _events.emit(AddStudentViewModelAction.UnpairStudentSuccess)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun resetError() {
        _uiState.value = _uiState.value.copy(isError = false)
    }
}