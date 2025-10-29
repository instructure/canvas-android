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
package com.instructure.horizon.features.dashboard.widget.timespent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.timespent.card.CourseOption
import com.instructure.horizon.features.dashboard.widget.timespent.card.DashboardTimeSpentCardState
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardTimeSpentViewModel @Inject constructor(
    private val repository: DashboardTimeSpentRepository
) : ViewModel() {

    private var courses: List<CourseOption> = emptyList()
    private var timeSpentData: TimeSpentWidgetData? = null

    private val _uiState = MutableStateFlow(
        DashboardTimeSpentUiState(
            onRefresh = ::refresh
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadTimeSpentData()
    }

    private fun loadTimeSpentData(forceNetwork: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            courses = repository.getCourses(forceNetwork).map {
                CourseOption(
                    id = it.courseId,
                    name = it.courseName,
                )
            }

            timeSpentData = repository.getTimeSpentData(null, forceNetwork)
            updateTimeSpentWidgetState()

        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }
    }

    private fun updateTimeSpentWidgetState() {
        val totalHours = timeSpentData?.data
            ?.filter { courses.any { course -> course.id == it.courseId } }
            ?.filter { uiState.value.cardState.selectedCourseId == null || it.courseId == uiState.value.cardState.selectedCourseId }
            ?.sumOf { it.minutesPerDay?.toDouble() ?: 0.0 }
            .orDefault() / 60

        _uiState.update {
            it.copy(
                state = DashboardItemState.SUCCESS,
                cardState = DashboardTimeSpentCardState(
                    hours = totalHours,
                    courses = courses,
                    selectedCourseId = it.cardState.selectedCourseId,
                    onCourseSelected = ::onCourseSelected
                )
            )
        }
    }

    private fun onCourseSelected(courseName: String?) {
        val courseId = uiState.value.cardState.courses.firstOrNull { it.name == courseName }?.id
        _uiState.update {
            it.copy(
                cardState = it.cardState.copy(selectedCourseId = courseId)
            )
        }
        updateTimeSpentWidgetState()
    }

    private fun refresh(onComplete: () -> Unit = {}) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            loadTimeSpentData(forceNetwork = true)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onComplete()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onComplete()
        }
    }
}
