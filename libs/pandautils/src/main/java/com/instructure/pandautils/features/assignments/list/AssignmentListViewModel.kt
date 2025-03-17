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
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.compose.composables.GroupedListViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AssignmentListRepository,
    private val assignmentListBehavior: AssignmentListBehavior
): ViewModel() {
    private val _uiState = MutableStateFlow(AssignmentListUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<AssignmentListFragmentEvent>()
    val events = _events.receiveAsFlow()

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
                        subtitle = course.name,
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
                        ),
                        filterState = assignmentListBehavior.getAssignmentListFilterState(course.color, course.gradingPeriods)
                    )
                }
            } catch {
                _uiState.update { it.copy(state = ScreenState.Error) }
            }
        } else {
            _uiState.update { it.copy(state = ScreenState.Error) }
        }
    }

    fun handleListEvent(event: GroupedListViewEvent<AssignmentGroupState, AssignmentGroupItemState>) {
        when (event) {
            is GroupedListViewEvent.GroupClicked -> {
                _uiState.update {
                    it.copy(
                        listState = it.listState.copy(
                            groups = it.listState.groups.map { group ->
                                if (group.id == event.group.id) {
                                    AssignmentGroupState(
                                        group.id as? Long ?: 0,
                                        group.title,
                                        group.items,
                                        !group.isExpanded
                                    )
                                } else {
                                    group
                                }
                            }
                        )
                    )
                }
            }

            is GroupedListViewEvent.ItemClicked -> {
                viewModelScope.launch {
                    _events.send(AssignmentListFragmentEvent.NavigateToAssignment(uiState.value.course, event.groupItem.assignment.id))
                }
            }
        }
    }

    fun handleAction(action: AssignmentListScreenEvent) {
        when (action) {
            AssignmentListScreenEvent.NavigateBack -> {
                viewModelScope.launch {
                    _events.send(AssignmentListFragmentEvent.NavigateBack)
                }
            }
            is AssignmentListScreenEvent.UpdateFilterState -> {
                _uiState.update { it.copy(filterState = action.filterState) }
            }
            AssignmentListScreenEvent.OpenFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.Filter) }
            }
            AssignmentListScreenEvent.CloseFilterScreen -> {
                _uiState.update { it.copy(screenOption = AssignmentListScreenOption.List) }
            }
        }
    }
}