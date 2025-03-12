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
package com.instructure.pandautils.features.assignments.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AssignmentListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AssignmentListRepository,
    private val assignmentListBehavior: AssignmentListBehavior
): ViewModel() {
    private val _uiState = MutableStateFlow(AssignmentListUiState())
    val uiState = _uiState.asStateFlow()

    private val courseId: Long? = savedStateHandle.get<Long>(Const.COURSE_ID)

    init {
        getAssignments(false)
    }

    private fun getAssignments(forceRefresh: Boolean = false) {
        if (courseId != null) {
            viewModelScope.tryLaunch {
                val assignmentGroups = repository.getAssignments(courseId, forceRefresh).dataOrThrow
                val course = repository.getCourse(courseId).dataOrThrow
                _uiState.update {
                    it.copy(
                        state = ScreenState.Content,
                        course = course,
                        listState = GroupedListViewState(
                            assignmentGroups.map { group ->
                                AssignmentGroupState(
                                    group.id,
                                    group.name.orEmpty(),
                                    group.assignments.map { assignment ->
                                        assignmentListBehavior.getAssignmentGroupItemState(assignment)
                                    }
                                )
                            }
                        )
                    )
                }
            } catch {
                _uiState.update { it.copy(state = ScreenState.Error) }
            }
        } else {
            _uiState.update { it.copy(state = ScreenState.Error) }
        }
    }
}