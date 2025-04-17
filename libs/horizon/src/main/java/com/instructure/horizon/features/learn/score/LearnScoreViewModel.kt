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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.stringValueWithoutTrailingZeros
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnScoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val learnScoreRepository: LearnScoreRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow(
        LearnScoreUiState(
            screenState = LoadingState(
                onRefresh = ::refresh,
                onErrorSnackbarDismiss = ::dismissSnackbar,
            ),
        )
    )
    val uiState = _uiState.asStateFlow()

    private var assignments: List<Assignment> = emptyList()

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
                    screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                        R.string.failedToLoadScores
                    )),
                )
            }
        }
    }

    private suspend fun getData(courseId: Long, forceRefresh: Boolean = false) {
        val assignmentGroups = learnScoreRepository.getAssignmentGroups(courseId, forceRefresh)
        val assignmentGroupItems = assignmentGroups.map { AssignmentGroupScoreItem(it) }
        val enrollments = learnScoreRepository.getEnrollments(courseId, forceRefresh)
        val grades = enrollments.first { it.enrollmentState == EnrollmentAPI.STATE_ACTIVE }.grades
        assignments = assignmentGroups.flatMap { it.assignments }
        sortAssignments(assignmentGroupItems)
        _uiState.update {
            it.copy(
                assignmentGroups = assignmentGroupItems,
                currentScore = grades?.currentScore.orDefault().stringValueWithoutTrailingZeros,
            )
        }
    }

    fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getData(uiState.value.courseId, forceRefresh = true)
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(errorSnackbar = context.getString(R.string.errorOccurred), isRefreshing = false)) }
        }
    }

    fun updateSelectedSortOption(sortOption: LearnScoreSortOption) {
        _uiState.update { it.copy(selectedSortOption = sortOption) }
        sortAssignments(uiState.value.assignmentGroups)
    }

    private fun sortAssignments(assignmentGroups: List<AssignmentGroupScoreItem>) {
        val sortedAssignments = when (_uiState.value.selectedSortOption) {
            LearnScoreSortOption.AssignmentName -> assignmentGroups.flatMap { it.assignmentItems }.sortedBy { it.name }
            LearnScoreSortOption.DueDate -> assignmentGroups.flatMap { it.assignmentItems }.sortedWith(
                compareBy(nullsLast()) { it.dueDate }
            )
        }
        _uiState.update { it.copy(sortedAssignments = sortedAssignments) }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(errorSnackbar = null))
        }
    }
}