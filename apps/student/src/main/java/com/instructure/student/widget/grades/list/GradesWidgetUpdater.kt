/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.widget.grades.list

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.toWidgetCourseItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GradesWidgetUpdater(
    private val repository: GradesWidgetRepository,
    private val apiPrefs: ApiPrefs,
    private val glanceAppWidgetManager: GlanceAppWidgetManager
) {

    private val _uiState = MutableStateFlow<Pair<GlanceId?, GradesWidgetUiState>>(
        Pair(
            null,
            GradesWidgetUiState(WidgetState.Loading)
        )
    )
    val uiState = _uiState.asStateFlow()

    suspend fun updateData(widgetIds: List<Int>) {
        for (widgetId in widgetIds) {
            val glanceId = try {
                glanceAppWidgetManager.getGlanceIdBy(widgetId)
            } catch (e: IllegalArgumentException) {
                // Invalid AppWidget ID (widget deleted)
                continue
            }

            val user = apiPrefs.user
            if (user == null) {
                _uiState.emit(Pair(glanceId, GradesWidgetUiState(WidgetState.NotLoggedIn)))
                continue
            }

            try {
                val coursesDataResult = repository.getCoursesWithGradingScheme(true)

                if (coursesDataResult is DataResult.Fail && coursesDataResult.failure is Failure.Authorization) {
                    _uiState.emit(Pair(glanceId, GradesWidgetUiState(WidgetState.NotLoggedIn)))
                    continue
                }
                // Other errors are handled in catch
                val courses = coursesDataResult.dataOrThrow

                if (courses.isEmpty()) {
                    _uiState.emit(Pair(glanceId, GradesWidgetUiState(WidgetState.Empty)))
                    continue
                }
                _uiState.emit(
                    Pair(
                        glanceId,
                        GradesWidgetUiState(
                            WidgetState.Content,
                            courses.map { it.toWidgetCourseItem(apiPrefs) }
                        )
                    )
                )
            } catch (e: Exception) {
                _uiState.emit(Pair(glanceId, GradesWidgetUiState(WidgetState.Error)))
            }
        }
    }
}
