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
package com.instructure.horizon.features.moduleitemsequence.content.assessment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    val repository: AssessmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId = savedStateHandle[ModuleItemContent.Assignment.ASSIGNMENT_ID] ?: -1L
    private val courseId = savedStateHandle[Const.COURSE_ID] ?: -1L

    private var assessmentUrl: String? = null

    private val _uiState = MutableStateFlow(
        AssessmentUiState(
            onAssessmentClosed = ::onAssessmentClosed,
            onStartQuizClicked = ::onStartQuizClicked,
            onAssessmentCompletion = ::onAssessmentCompletion,
            onAssessmentLoaded = ::onAssessmentLoaded
        )
    )

    private fun onAssessmentCompletion() {
        _uiState.update { it.copy(assessmentCompletionLoading = true) }
        viewModelScope.launch {
            delay(15000) // This is based on the iOS app, we need to add a loading delay so the quiz result would be processed correctly
            _uiState.update { it.copy(assessmentCompletionLoading = false) }
        }
    }

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update {
            it.copy(loadingState = LoadingState(isLoading = true))
        }
        viewModelScope.tryLaunch {
            val assignment = repository.getAssignment(assignmentId, courseId, false)
            assessmentUrl = assignment.url
            _uiState.update {
                it.copy(loadingState = LoadingState(isLoading = false), assessmentName = assignment.name.orEmpty())
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = LoadingState(isError = true))
            }
        }
    }

    private fun onStartQuizClicked() {
        _uiState.update { it.copy(showAssessmentDialog = true, assessmentLoading = true) }
        viewModelScope.tryLaunch {
            assessmentUrl?.let { url ->
                val authenticatedUrl = repository.authenticateUrl(url)
                _uiState.update { it.copy(urlToLoad = authenticatedUrl) }
            } ?: run {
                _uiState.update { it.copy(assessmentLoading = false) }
            }
        } catch {
            _uiState.update { it.copy(assessmentLoading = false) }
        }
    }

    private fun onAssessmentClosed() {
        _uiState.update { it.copy(urlToLoad = null, showAssessmentDialog = false) }
    }

    private fun onAssessmentLoaded() {
        _uiState.update { it.copy(assessmentLoading = false) }
    }
}