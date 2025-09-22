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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.home.COURSE_PREFIX
import com.instructure.horizon.features.home.PROGRAM_PREFIX
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: LearnRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LearnUiState(
            screenState = LoadingState(onRefresh = ::onRefresh, onSnackbarDismiss = ::onSnackbarDismiss),
            onSelectedLearningItemChanged = ::onSelectedLearningItemChanged,
            onProgramCourseSelected = ::onProgramCourseSelected
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
            _state.update { it.copy(screenState = it.screenState.copy(isLoading = false, errorMessage = context.getString(R.string.learn_failedToLoad), isError = true)) }
        }
    }

    private suspend fun getLearningItems(forceRefresh: Boolean = false) {
        val enrolledPrograms = repository.getPrograms(forceNetwork = forceRefresh)
        val courseIdsInPrograms = enrolledPrograms.flatMap { it.sortedRequirements.map { requirement -> requirement.courseId } }.toSet()

        val enrolledCourses = repository.getCoursesWithProgress(forceNetwork = forceRefresh)
        val standaloneCourses = enrolledCourses.filter { !courseIdsInPrograms.contains(it.courseId) }

        val learningItems = enrolledPrograms.map { program ->
            val programCourses = repository.getCoursesById(program.sortedRequirements.map { it.courseId }, forceNetwork = forceRefresh)
            val programCourseItems = programCourses.map { courseWithModuleItemDurations ->
                enrolledCourses.find { it.courseId == courseWithModuleItemDurations.courseId }?.let { courseWithProgress ->
                    LearningItem.CourseItem(courseWithProgress)
                } ?: LearningItem.LockedCourseItem(courseWithModuleItemDurations.courseName)
            }
            if (programCourseItems.any { it is LearningItem.CourseItem }) {
                val programDetailsItem =
                    LearningItem.ProgramDetails(program, programCourses, context.getString(R.string.programSwitcher_programOverview))
                LearningItem.ProgramGroupItem(
                    program.id,
                    program.name,
                    listOf(
                        LearningItem.BackToAllItems(context.getString(R.string.programSwitcher_goBack)),
                        LearningItem.ProgramHeaderItem(program.name),
                        programDetailsItem
                    ) + programCourseItems.map {
                        if (it is LearningItem.CourseItem) it.copy(
                            parentItem = programDetailsItem
                        ) else it
                    }
                )
            } else {
                LearningItem.ProgramDetails(program, programCourses, program.name)
            }
        } + standaloneCourses.map { LearningItem.CourseItem(it) }

        val selectableLearningItems = learningItems
            .flatMap { if (it is LearningItem.ProgramGroupItem) it.items else listOf(it) }
            .filter { it is LearningItem.CourseItem || it is LearningItem.ProgramDetails }

        val selectedLearningItem = when {
            learningItemId.startsWith(COURSE_PREFIX) -> {
                val courseId = learningItemId.removePrefix(COURSE_PREFIX).toLongOrNull()
                selectableLearningItems
                    .find { it is LearningItem.CourseItem && it.courseWithProgress.courseId == courseId }
                    ?: selectableLearningItems.firstOrNull()
            }

            learningItemId.startsWith(PROGRAM_PREFIX) -> {
                val programId = learningItemId.removePrefix(PROGRAM_PREFIX)
                selectableLearningItems
                    .find { it is LearningItem.ProgramDetails && it.program.id == programId }
                    ?: selectableLearningItems.firstOrNull()
            }

            else -> selectableLearningItems.firstOrNull()
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
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false, isError = false, errorMessage = null)) }
        } catch {
            _state.update { it.copy(screenState = it.screenState.copy(isRefreshing = false, snackbarMessage = context.getString(R.string.learn_failedToRefresh))) }
        }
    }

    private fun onProgramCourseSelected(programId: String, courseId: Long) {
        val courseParentProgram = _state.value.learningItems
            .find { (it as? LearningItem.ProgramGroupItem)?.programId == programId } as? LearningItem.ProgramGroupItem
        val learningItem = courseParentProgram?.items
            ?.find { it is LearningItem.CourseItem && it.courseWithProgress.courseId == courseId }

        if (learningItem != null) {
            _state.value = state.value.copy(selectedLearningItem = learningItem)
        } else {
            _state.update {
                it.copy(screenState = it.screenState.copy(snackbarMessage = context.getString(R.string.learnScreen_courseCannotBeOpened)))
            }
        }
    }

    private fun onSnackbarDismiss() {
        _state.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }
}