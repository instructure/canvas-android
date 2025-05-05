/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId = savedStateHandle[ModuleItemContent.Assignment.ASSIGNMENT_ID] ?: -1L
    private val courseId = savedStateHandle[Const.COURSE_ID] ?: -1L

    private val _uiState =
        MutableStateFlow(AssignmentDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
        viewModelScope.tryLaunch {
            val result = assignmentDetailsRepository.getAssignment(assignmentId, courseId, forceNetwork = false)
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    instructions = result.description.orEmpty()
                )
            }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }
}