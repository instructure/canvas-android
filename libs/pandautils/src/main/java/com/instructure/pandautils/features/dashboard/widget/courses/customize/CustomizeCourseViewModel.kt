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

package com.instructure.pandautils.features.dashboard.widget.courses.customize

import android.content.Intent
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.course.SetCourseColorUseCase
import com.instructure.pandautils.domain.usecase.course.SetCourseNicknameUseCase
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesConfig
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeCourseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val setCourseNicknameUseCase: SetCourseNicknameUseCase,
    private val setCourseColorUseCase: SetCourseColorUseCase,
    private val resources: Resources,
    private val colorKeeper: ColorKeeper,
    private val localBroadcastManager: LocalBroadcastManager,
    private val observeWidgetConfigUseCase: ObserveWidgetConfigUseCase,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val course: Course = savedStateHandle.get<Course>(Const.COURSE) ?: throw IllegalArgumentException("Course can not be null")
    private val originalNickname = course.originalName?.let { course.name }.orEmpty()

    private val _uiState = MutableStateFlow(
        createInitialState(
            onNicknameChanged = this::onNicknameChanged,
            onColorSelected = this::onColorSelected,
            onDone = this::onDone,
            onNavigationHandled = this::onNavigationHandled,
            onErrorHandled = this::onErrorHandled
        )
    )
    val uiState: StateFlow<CustomizeCourseUiState> = _uiState.asStateFlow()

    init {
        loadShowColorOverlay()
    }

    private fun createInitialState(
        onNicknameChanged: (String) -> Unit,
        onColorSelected: (Int) -> Unit,
        onDone: () -> Unit,
        onNavigationHandled: () -> Unit,
        onErrorHandled: () -> Unit
    ): CustomizeCourseUiState {
        val availableColors = getAvailableColors()
        val currentColor = course.color

        return CustomizeCourseUiState(
            courseId = course.id,
            courseName = course.name,
            courseCode = course.courseCode.orEmpty(),
            imageUrl = course.imageUrl,
            nickname = course.originalName?.let { course.name }.orEmpty(),
            selectedColor = currentColor,
            initialColor = currentColor,
            availableColors = availableColors,
            showColorOverlay = false,
            onNicknameChanged = onNicknameChanged,
            onColorSelected = onColorSelected,
            onDone = onDone,
            onNavigationHandled = onNavigationHandled,
            onErrorHandled = onErrorHandled
        )
    }

    private fun loadShowColorOverlay() {
        viewModelScope.launch {
            val settings = observeWidgetConfigUseCase(WidgetMetadata.WIDGET_ID_COURSES).first()
            val showColorOverlay = settings.firstOrNull { it.key == CoursesConfig.KEY_SHOW_COLOR_OVERLAY }?.value as? Boolean ?: false
            _uiState.update { it.copy(showColorOverlay = showColorOverlay) }
        }
    }

    private fun getAvailableColors(): List<Int> {
        return colorKeeper.courseColors.map { resources.getColor(it, null) }
    }

    private fun onNicknameChanged(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    private fun onColorSelected(color: Int) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    private fun onDone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                if (_uiState.value.nickname != originalNickname) {
                    setCourseNicknameUseCase(
                        SetCourseNicknameUseCase.Params(course.id, _uiState.value.nickname)
                    )
                }

                setCourseColorUseCase(
                    SetCourseColorUseCase.Params(course.contextId, _uiState.value.selectedColor)
                )

                colorKeeper.addToCache(course.contextId, _uiState.value.selectedColor)

                val intent = Intent(Const.COURSE_THING_CHANGED).apply {
                    putExtra(Const.COURSE_ID, course.id)
                }
                localBroadcastManager.sendBroadcast(intent)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        shouldNavigateBack = true
                    )
                }
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = resources.getString(R.string.errorOccurred)
                    )
                }
            }
        }
    }

    private fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    private fun onErrorHandled() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
