/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val learnRepository: LearnRepository,
): ViewModel() {
    private val _state = MutableStateFlow(LearnUiState())
    val state = _state.asStateFlow()

    init {
        getInProgressCourse()
    }

    private fun getInProgressCourse(forceRefresh: Boolean = false) = viewModelScope.launch {
        _state.value = state.value.copy(screenState = state.value.screenState.copy(isLoading = true))
        learnRepository.getCoursesWithProgress(forceNetwork = forceRefresh).onSuccess { courses ->
            val course = courses.firstOrNull()
            _state.value = state.value.copy(
                screenState = state.value.screenState.copy(isLoading = false),
                course = course
            )
        }.onFailure {
            _state.value = state.value.copy(screenState = state.value.screenState.copy(isLoading = false, errorMessage = it?.message))
        }
    }
}