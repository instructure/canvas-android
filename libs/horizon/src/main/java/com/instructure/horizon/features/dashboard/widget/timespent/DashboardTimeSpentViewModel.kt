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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardTimeSpentViewModel @Inject constructor(
    private val repository: DashboardTimeSpentRepository
) : ViewModel() {

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
        viewModelScope.launch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            try {
                val data = repository.getTimeSpentData(forceNetwork = forceNetwork)

                val totalHours = parseHoursFromData(data.data)
                val courses = parseCoursesFromData(data.data)

                _uiState.update {
                    it.copy(
                        state = DashboardItemState.SUCCESS,
                        cardState = DashboardTimeSpentCardState(
                            hours = totalHours,
                            courses = courses,
                            selectedCourseId = null,
                            onCourseSelected = ::onCourseSelected
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            }
        }
    }

    private fun parseHoursFromData(data: List<Any>): Double {
        if (data.isEmpty()) return 0.0

        return try {
            val dataMap = data.firstOrNull() as? Map<*, *>
            val hours = dataMap?.get("hours") ?: dataMap?.get("totalHours")
            when (hours) {
                is Number -> hours.toDouble()
                is String -> hours.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun parseCoursesFromData(data: List<Any>): List<CourseOption> {
        return try {
            data.mapNotNull { item ->
                val courseMap = item as? Map<*, *>
                val courseId = when (val id = courseMap?.get("courseId")) {
                    is Number -> id.toLong()
                    is String -> id.toLongOrNull()
                    else -> null
                }
                val courseName = courseMap?.get("courseName") as? String

                if (courseId != null && courseName != null) {
                    CourseOption(courseId, courseName)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun onCourseSelected(courseName: String?) {
        val courseId = uiState.value.cardState.courses.firstOrNull { it.name == courseName }?.id
        _uiState.update {
            it.copy(
                cardState = it.cardState.copy(selectedCourseId = courseId)
            )
        }
    }

    private fun refresh(onComplete: () -> Unit) {
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
