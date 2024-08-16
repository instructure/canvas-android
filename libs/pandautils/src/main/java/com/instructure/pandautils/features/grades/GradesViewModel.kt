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

package com.instructure.pandautils.features.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.GradingPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class GradesViewModel @Inject constructor(
    private val repository: GradesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState = _uiState.asStateFlow()

    private var currentGradingPeriod: GradingPeriod? = null

    fun loadGrades(courseId: Long, forceRefresh: Boolean) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isError = false
                )
            }

            val assignmentGroups = repository.loadAssignmentGroups(courseId, forceRefresh)
            val gradingPeriods = repository.loadGradingPeriods(courseId, forceRefresh)
            //  val enrollments = repository.loadEnrollments(courseId, currentGradingPeriod?.id, forceRefresh)

            val items = groupByAssignmentGroup(assignmentGroups)

            _uiState.update {
                it.copy(
                    items = items,
                    isLoading = false
                )
            }
        } catch {
            _uiState.update { it.copy(isError = true) }
        }
    }

    private fun groupByAssignmentGroup(assignmentGroups: List<AssignmentGroup>) = assignmentGroups.map { group ->
        AssignmentGroupUiState(
            id = group.id,
            name = group.name.orEmpty(),
            assignments = mapAssignments(group.assignments),
            expanded = true
        )
    }

    private fun mapAssignments(assignments: List<Assignment>) = assignments.map { assignment ->
        AssignmentUiState(
            iconRes = R.drawable.ic_assignment,
            name = assignment.name.orEmpty(),
            dueDate = assignment.dueAt,
            points = "10",
            pointsPossible = "20"
        )
    }

    fun handleAction(action: GradesAction) {
        when (action) {
            is GradesAction.Refresh -> {

            }

            is GradesAction.HeaderClick -> {
                val items = uiState.value.items.map { group ->
                    if (group.id == action.id) {
                        group.copy(expanded = !group.expanded)
                    } else {
                        group
                    }
                }
                _uiState.update { it.copy(items = items) }
            }
        }
    }
}
