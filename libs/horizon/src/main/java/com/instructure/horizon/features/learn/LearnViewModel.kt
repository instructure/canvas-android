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
package com.instructure.horizon.features.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.home.COURSE_PREFIX
import com.instructure.horizon.features.home.PROGRAM_PREFIX
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: LearnRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LearnUiState(
            screenState = LoadingState(onRefresh = ::onRefresh),
            onSelectedLearningItemChanged = ::onSelectedLearningItemChanged,
        )
    )
    val state = _state.asStateFlow()

    private val learningItemId: String = savedStateHandle["learningItemId"] ?: ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = true)) }
            getLearningItems()
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = false)) }
        } catch {
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = false, errorMessage = "Failed to load Courses")) }
        }
    }

    private suspend fun getLearningItems(forceRefresh: Boolean = false) {
        val enrolledPrograms = repository.getPrograms(forceNetwork = forceRefresh)
        val courseIdsInPrograms = enrolledPrograms.flatMap { it.sortedRequirements.map { requirement -> requirement.courseId } }.toSet()

        val standaloneCourses =
            repository.getCoursesWithProgress(forceNetwork = forceRefresh).filter { !courseIdsInPrograms.contains(it.courseId) }

        val learningItems = enrolledPrograms.map { program ->
            val programCourses = repository.getCoursesById(program.sortedRequirements.map { it.courseId }, forceNetwork = forceRefresh)
            LearningItem.ProgramItem(program, programCourses)
        } + standaloneCourses.map { LearningItem.CourseItem(it) }

        val selectedLearningItem = when {
            learningItemId.startsWith(COURSE_PREFIX) -> {
                val courseId = learningItemId.removePrefix(COURSE_PREFIX).toLongOrNull()
                learningItems.find { it is LearningItem.CourseItem && it.courseWithProgress.courseId == courseId }
                    ?: learningItems.firstOrNull()
            }

            learningItemId.startsWith(PROGRAM_PREFIX) -> {
                val programId = learningItemId.removePrefix(PROGRAM_PREFIX)
                learningItems.find { it is LearningItem.ProgramItem && it.program.id == programId } ?: learningItems.firstOrNull()
            }

            else -> learningItems.firstOrNull()
        }

        _state.update {
            state.value.copy(learningItems = learningItems, selectedLearningItem = selectedLearningItem)
        }
    }

    private fun onSelectedLearningItemChanged(learningItem: LearningItem) {
        _state.value = state.value.copy(selectedLearningItem = learningItem)
    }

    private fun onRefresh() {
        viewModelScope.tryLaunch {
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getLearningItems(forceRefresh = true)
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        }
    }
}