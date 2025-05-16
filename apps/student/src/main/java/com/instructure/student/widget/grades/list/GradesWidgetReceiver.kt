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
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.toJson
import com.instructure.student.widget.glance.WidgetState
import com.instructure.student.widget.grades.GradesWidgetRepository
import com.instructure.student.widget.grades.toWidgetCourseItem
import dagger.hilt.android.AndroidEntryPoint
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
        updateData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        updateData(context)
    }

    private fun updateData(context: Context) {
        coroutineScope.launch {

            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(GradesWidget::class.java).firstOrNull()
                    ?: return@launch

            suspend fun setState(state: GradesWidgetUiState) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->
                    pref.toMutablePreferences().apply {
                        this[gradesWidgetUiStateKey] = state.toJson()
                    }
                }
            }

            val user = apiPrefs.user
            if (user == null) {
                setState(GradesWidgetUiState(WidgetState.NotLoggedIn))
                glanceAppWidget.update(context, glanceId)
                return@launch
            }

            try {
                val courses = repository.getCoursesWithGradingScheme(true)
                setState(
                    GradesWidgetUiState(
                        WidgetState.Content,
                        courses.map { it.toWidgetCourseItem() })
                )
            } catch (e: Exception) {
                setState(GradesWidgetUiState(WidgetState.Error))
            }

            glanceAppWidget.update(context, glanceId)
        }
    }

    companion object {
        val gradesWidgetUiStateKey = stringPreferencesKey("gradesWidgetUiState")
    }
}
