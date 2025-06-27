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
package com.instructure.student.widget.grades.singleGrade

import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.courseselector.CourseSelectorActivity
import com.instructure.student.widget.grades.toWidgetCourseItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SingleGradeWidgetUpdater(
    private val repository: GradesWidgetRepository,
    private val apiPrefs: ApiPrefs,
    private val glanceAppWidgetManager: GlanceAppWidgetManager
) {

    private val _events = Channel<SingleGradeWidgetAction>()
    val events = _events.receiveAsFlow()

    suspend fun updateData(
        widgetIds: List<Int>,
        showLoading: Boolean = false,
        forceNetwork: Boolean = true
    ) {
        var glanceId: GlanceId? = null
        try {
            if (showLoading) {
                updateAll(WidgetState.Loading)
            }

            val user = apiPrefs.user
            if (user == null) {
                updateAll(WidgetState.NotLoggedIn)
                return
            }

            val coursesDataResult = repository.getCoursesWithGradingScheme(forceNetwork)

            if (coursesDataResult is DataResult.Fail && coursesDataResult.failure is Failure.Authorization) {
                updateAll(WidgetState.NotLoggedIn)
                return
            }
            // Other errors are handled in catch
            val courses = coursesDataResult.dataOrThrow

            if (courses.isEmpty()) {
                updateAll(WidgetState.Error)
                return
            }

            for (widgetId in widgetIds) {
                glanceId = glanceAppWidgetManager.getGlanceIdBy(widgetId)
                val courseId = StudentPrefs.getLong(
                    CourseSelectorActivity.WIDGET_COURSE_ID_PREFIX + widgetId,
                    -1
                )
                val course = courses.find { it.id == courseId }
                if (course != null) {
                    _events.send(
                        SingleGradeWidgetAction.UpdateState(
                            glanceId, SingleGradeWidgetUiState(
                                WidgetState.Content,
                                course.toWidgetCourseItem(apiPrefs)
                            )
                        )
                    )
                } else {
                    _events.send(
                        SingleGradeWidgetAction.UpdateState(
                            glanceId,
                            SingleGradeWidgetUiState(WidgetState.Empty)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            glanceId?.let {
                _events.send(
                    SingleGradeWidgetAction.UpdateState(
                        it,
                        SingleGradeWidgetUiState(WidgetState.Error)
                    )
                )
            } ?: run {
                _events.send(
                    SingleGradeWidgetAction.UpdateAllState(
                        SingleGradeWidgetUiState(WidgetState.Error)
                    )
                )
            }
        } finally {
            _events.send(SingleGradeWidgetAction.UpdateUi)
        }
    }

    private suspend fun updateAll(
        widgetState: WidgetState
    ) {
        _events.send(
            SingleGradeWidgetAction.UpdateAllState(SingleGradeWidgetUiState(widgetState))
        )
    }
}