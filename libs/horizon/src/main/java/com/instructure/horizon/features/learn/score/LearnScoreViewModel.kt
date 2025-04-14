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
package com.instructure.horizon.features.learn.score

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnScoreViewModel @Inject constructor(
    private val learnScoreRepository: LearnScoreRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(
        LearnScoreUiState(
            screenState = LoadingState(
                onRefresh = ::refresh,
            ),
        )
    )
    val uiState = _uiState.asStateFlow()

    fun loadState(courseId: Long) {
        _uiState.update {
            it.copy(
                screenState = it.screenState.copy(isLoading = true),
                courseId = courseId,
            )
        }
        viewModelScope.tryLaunch {
            getData(courseId)
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch { exception ->
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isLoading = false, errorMessage = exception.message),
                )
            }
        }
    }

    private suspend fun getData(courseId: Long, forceRefresh: Boolean = false) {
        val assignmentGroups = learnScoreRepository.getAssignmentGroups(courseId, forceRefresh)
        val enrollments = learnScoreRepository.getEnrollments(courseId, forceRefresh)
        val grades = enrollments.first { it.enrollmentState == EnrollmentAPI.STATE_ACTIVE }.grades

        sortAssignments(assignmentGroups)
        _uiState.update {
            it.copy(
                assignmentGroups = assignmentGroups,
                grades = grades,
            )
        }
    }

    fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getData(uiState.value.courseId, forceRefresh = true)
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        }
    }

    fun updateSelectedSortOption(sortOption: LearnScoreSortOption) {
        _uiState.update { it.copy(selectedSortOption = sortOption) }
        sortAssignments(uiState.value.assignmentGroups)
    }

    private fun sortAssignments(assignmentGroups: List<AssignmentGroup>) {
        val sortedAssignments = when (_uiState.value.selectedSortOption) {
            LearnScoreSortOption.AssignmentName -> assignmentGroups.flatMap { it.assignments }.sortedBy { it.name }
            LearnScoreSortOption.DueDate -> assignmentGroups.flatMap { it.assignments }.sortedBy { it.dueAt }
        }
        _uiState.update { it.copy(sortedAssignments = sortedAssignments) }
    }
}