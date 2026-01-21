/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.customize.course

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.domain.usecase.course.SetCourseColorParams
import com.instructure.pandautils.domain.usecase.course.SetCourseColorUseCase
import com.instructure.pandautils.domain.usecase.course.SetCourseNicknameParams
import com.instructure.pandautils.domain.usecase.course.SetCourseNicknameUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeCourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val setCourseNicknameUseCase: SetCourseNicknameUseCase,
    private val setCourseColorUseCase: SetCourseColorUseCase,
    private val resources: Resources
) : ViewModel() {

    private val course: Course = savedStateHandle.get<Course>(Const.COURSE) ?: throw IllegalArgumentException("Course can not be null")

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<CustomizeCourseUiState> = _uiState.asStateFlow()

    private fun createInitialState(): CustomizeCourseUiState {
        val availableColors = getAvailableColors()
        val currentColor = course.color

        return CustomizeCourseUiState(
            courseId = course.id,
            courseName = course.name,
            courseCode = course.courseCode.orEmpty(),
            imageUrl = course.imageUrl,
            nickname = course.originalName?.let { course.name } ?: "",
            selectedColor = currentColor,
            availableColors = availableColors
        )
    }

    private fun getAvailableColors(): List<Int> {
        return ColorKeeper.courseColors.map { resources.getColor(it, null) }
    }

    fun onNicknameChanged(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun onColorSelected(color: Int) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun onDone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val nicknameResult = setCourseNicknameUseCase(
                    SetCourseNicknameParams(course.id, _uiState.value.nickname)
                )

                val colorResult = setCourseColorUseCase(
                    SetCourseColorParams(course.contextId, _uiState.value.selectedColor)
                )

                ColorKeeper.addToCache(course.contextId, _uiState.value.selectedColor)

                _uiState.update { it.copy(isLoading = false) }
                _events.emit(CustomizeCourseEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(CustomizeCourseEvent.ShowError)
            }
        }
    }

    private val _events = MutableStateFlow<CustomizeCourseEvent?>(null)
    val events: StateFlow<CustomizeCourseEvent?> = _events.asStateFlow()
}

sealed class CustomizeCourseEvent {
    data object NavigateBack : CustomizeCourseEvent()
    data object ShowError : CustomizeCourseEvent()
}
