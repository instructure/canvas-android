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
package com.instructure.horizon.features.account.manageofflinecontent

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ManageOfflineContentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        ManageOfflineContentUiState(
            onRemoveContentClick = ::onRemoveContentClick,
            onSyncClick = ::onSyncClick,
        )
    )
    val uiState = _uiState.asStateFlow()

    private fun onRemoveContentClick() {
        // Navigation to confirmation screen is handled in the composable
    }

    private fun onSyncClick() {
        _uiState.update { it.copy(mode = ManageOfflineContentMode.SYNCING) }
    }

    fun toggleCourseExpanded(courseId: Long) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) course.copy(isExpanded = !course.isExpanded)
                    else course
                }
            )
        }
    }

    fun updateCourseOfflineState(courseId: Long, offlineState: CourseOfflineState) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) course.copy(offlineState = offlineState)
                    else course
                }
            )
        }
    }

    fun updateFileSelection(courseId: Long, fileId: Long, selected: Boolean) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) {
                        val updatedFiles = course.files.map { file ->
                            if (file.fileId == fileId) file.copy(isSelected = selected) else file
                        }
                        val allSelected = updatedFiles.all { it.isSelected }
                        val noneSelected = updatedFiles.none { it.isSelected }
                        val newOfflineState = when {
                            allSelected -> CourseOfflineState.ALL
                            noneSelected -> CourseOfflineState.NONE
                            else -> CourseOfflineState.PARTIAL
                        }
                        course.copy(files = updatedFiles, offlineState = newOfflineState)
                    } else course
                }
            )
        }
    }
}
