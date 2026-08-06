/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.ngc.features.coursehome.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.ngc.features.coursehome.CourseHomeViewModel
import com.instructure.pandautils.domain.usecase.pages.LoadFrontPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseHomeTabViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadFrontPageUseCase: LoadFrontPageUseCase,
    private val crashlytics: FirebaseCrashlytics,
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>(CourseHomeViewModel.ARG_COURSE_ID) ?: 0L

    private val _uiState = MutableStateFlow(CourseHomeTabUiState())
    val uiState: StateFlow<CourseHomeTabUiState> = _uiState.asStateFlow()

    init {
        loadFrontPage()
    }

    private fun loadFrontPage() {
        viewModelScope.launch {
            try {
                val page = loadFrontPageUseCase(
                    LoadFrontPageUseCase.Params(CanvasContext.emptyCourseContext(courseId), false)
                ).dataOrNull
                val content = page?.body?.takeIf { it.isNotBlank() }
                _uiState.update { it.copy(isLoading = false, content = content) }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }
}