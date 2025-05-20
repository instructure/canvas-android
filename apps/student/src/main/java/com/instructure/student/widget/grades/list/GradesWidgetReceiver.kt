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

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.toJson
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.toWidgetCourseItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GradesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = GradesWidget()

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

    private suspend fun setState(context: Context, state: GradesWidgetUiState, glanceId: GlanceId) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->
            pref.toMutablePreferences().apply {
                this[gradesWidgetUiStateKey] = state.toJson()
            }
        }
    }

    private fun updateData(context: Context, widgetIds: List<Int>) {
        coroutineScope.launch(Dispatchers.IO) {
            val user = apiPrefs.user
            if (user == null) {
                setAllNotLoggedIn(context, widgetIds)
                return@launch
            }

            for (widgetId in widgetIds) {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)

                try {
                    val courses = repository.getCoursesWithGradingScheme(true)
                    if (courses.isEmpty()) {
                        setState(context, GradesWidgetUiState(WidgetState.Empty), glanceId)
                        continue
                    }
                    setState(
                        context,
                        GradesWidgetUiState(
                            WidgetState.Content,
                            courses.map { it.toWidgetCourseItem() }),
                        glanceId
                    )
                } catch (e: Exception) {
                    setState(context, GradesWidgetUiState(WidgetState.Error), glanceId)
                }

                glanceAppWidget.update(context, glanceId)
            }
        }
    }

    private suspend fun setAllNotLoggedIn(context: Context, widgetIds: List<Int>) {
        for (widgetId in widgetIds) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
            setState(
                context,
                GradesWidgetUiState(WidgetState.NotLoggedIn),
                glanceId
            )
        }
        glanceAppWidget.updateAll(context)
    }

    companion object {
        val gradesWidgetUiStateKey = stringPreferencesKey("gradesWidgetUiState")
    }
}
