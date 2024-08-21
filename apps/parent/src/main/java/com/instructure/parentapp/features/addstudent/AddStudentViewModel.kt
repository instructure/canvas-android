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
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStudentViewModel @Inject constructor(
    selectedStudentHolder: SelectedStudentHolder,
    private val repository: AddStudentRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AddStudentUiState(
                color = selectedStudentHolder.selectedStudentState.value?.backgroundColor.orDefault(),
                onStartPairing = this::pairStudent
            )
        )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AddStudentViewModelAction>()
    val events = _events.receiveAsFlow()

    private fun pairStudent(pairingCode: String) {
        viewModelScope.launch {
            try {
                repository.pairStudent(pairingCode).dataOrThrow
                _events.send(AddStudentViewModelAction.PairStudentSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                _events.send(AddStudentViewModelAction.PairStudentFailure)
            }
        }

    }
}