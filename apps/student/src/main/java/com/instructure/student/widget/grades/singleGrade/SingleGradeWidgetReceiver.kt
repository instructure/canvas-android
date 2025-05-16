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
 */package com.instructure.student.widget.grades.singleGrade

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.toJson
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.courseselector.CourseSelectorActivity
import com.instructure.student.widget.grades.toWidgetCourseItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SingleGradeWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = SingleGradeWidget()

    @Inject
    lateinit var repository: GradesWidgetRepository

    @Inject
    lateinit var apiPrefs: ApiPrefs

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context, appWidgetIds.toList())
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val widgetIds = intent.extras?.getIntArray(EXTRA_APPWIDGET_IDS)
        widgetIds?.let {
            updateData(context, it.toList())
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            StudentPrefs.remove(CourseSelectorActivity.WIDGET_COURSE_ID_PREFIX + widgetId)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateData(context, listOf(appWidgetId))
    }

    private fun updateData(context: Context, widgetIds: List<Int>) {
        if (widgetIds.isEmpty()) {
            return
        }
        coroutineScope.launch {
            val courses = repository.getCoursesWithGradingScheme(true)

            for (widgetId in widgetIds) {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)

                suspend fun setState(state: SingleGradeWidgetUiState) {
                    updateAppWidgetState(
                        context,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { pref ->
                        pref.toMutablePreferences().apply {
                            this[singleGradeWidgetUiStateKey] = state.toJson()
                        }
                    }
                }

                val user = apiPrefs.user
                if (user == null) {
                    setState(SingleGradeWidgetUiState(WidgetState.NotLoggedIn))
                    glanceAppWidget.update(context, glanceId)
                    return@launch
                }

                try {
                    val courseId = StudentPrefs.getLong(
                        CourseSelectorActivity.WIDGET_COURSE_ID_PREFIX + widgetId,
                        -1
                    )
                    val course = courses.find { it.id == courseId }
                    if (course != null) {
                        setState(
                            SingleGradeWidgetUiState(
                                WidgetState.Content,
                                course.toWidgetCourseItem()
                            )
                        )
                    } else {
                        setState(SingleGradeWidgetUiState(WidgetState.Error))
                    }
                } catch (e: Exception) {
                    setState(SingleGradeWidgetUiState(WidgetState.Error))
                }

                glanceAppWidget.update(context, glanceId)
            }
        }
    }

    companion object {
        val singleGradeWidgetUiStateKey = stringPreferencesKey("singleGradeWidgetUiState")
    }
}