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
package com.instructure.horizon.features.dashboard.course

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.course.card.DashboardCourseCardState
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardCourseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DashboardCourseRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DashboardCourseUiState(onRefresh = ::onRefresh))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(state = DashboardItemState.LOADING) }

        viewModelScope.tryLaunch {
            fetchData(forceNetwork = false)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }
    }

    private fun onRefresh(onFinished: () -> Unit = {}) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            fetchData(forceNetwork = true)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onFinished()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onFinished()
        }
    }

    private suspend fun fetchData(forceNetwork: Boolean) {
        val enrollments = repository.getEnrollments(forceNetwork)
        val programs = repository.getPrograms(forceNetwork)

        val courseCardStates = enrollments.mapToDashboardCourseCardState(
            programs = programs,
            nextModuleForCourse = { courseId ->
                fetchNextModuleState(courseId, forceNetwork)
            },
            acceptInvite = { courseId, enrollmentId ->
                repository.acceptInvite(courseId, enrollmentId)
            }
        ).map { state ->
            if (state.buttonState?.onClickAction is CardClickAction.Action) {
                state.copy(buttonState = state.buttonState.copy(
                    onClickAction = CardClickAction.Action {
                        viewModelScope.tryLaunch {
                            updateCourseButtonState(state, isLoading = true)
                            state.buttonState.action()
                            onRefresh()
                            updateCourseButtonState(state, isLoading = false)
                        } catch {
                            updateCourseButtonState(state, isLoading = false)
                        }
                    },
                ))
            } else state
        } + programs.mapToDashboardCourseCardState()

        _uiState.update { it.copy(courses = courseCardStates) }
    }

    private suspend fun fetchNextModuleState(courseId: Long?, forceNetwork: Boolean): DashboardCourseCardModuleItemState? {
        if (courseId == null) return null
        val modules = repository.getFirstPageModulesWithItems(courseId, forceNetwork = forceNetwork).dataOrThrow
        val nextModuleItem = modules.flatMap { module -> module.items }.firstOrNull()
        val nextModule = modules.find { module -> module.id == nextModuleItem?.moduleId }

        if (nextModuleItem == null) {
            return null
        }

        return DashboardCourseCardModuleItemState(
            moduleItemTitle = nextModuleItem.title.orEmpty(),
            moduleItemType = if (nextModuleItem.quizLti) LearningObjectType.ASSESSMENT else LearningObjectType.fromApiString(nextModuleItem.type.orEmpty()),
            dueDate = nextModuleItem.moduleDetails?.dueDate,
            estimatedDuration = nextModuleItem.estimatedDuration?.formatIsoDuration(context),
            onClickAction = CardClickAction.NavigateToModuleItem(courseId, nextModuleItem.id)
        )
    }

    private fun updateCourseButtonState(state: DashboardCourseCardState, isLoading: Boolean) {
        _uiState.update {
            it.copy(
                courses = it.courses.map { originalState ->
                    if (originalState.title == state.title && originalState.parentPrograms == state.parentPrograms) {
                        originalState.copy(
                            buttonState = originalState.buttonState?.copy(
                                isLoading = isLoading
                            )
                        )
                    } else {
                        originalState
                    }
                }
            )
        }
    }
}