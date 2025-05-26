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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.utils.toJson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GradesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = GradesWidget()

    @Inject
    lateinit var updater: GradesWidgetUpdater

    private val coroutineScope = MainScope()

    override fun onEnabled(context: Context?) {
        Analytics.logEvent(AnalyticsEventConstants.WIDGET_GRADES_WIDGET_ADDED)
        super.onEnabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Analytics.logEvent(AnalyticsEventConstants.WIDGET_GRADES_WIDGET_DELETED)
        super.onDeleted(context, appWidgetIds)
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        coroutineScope.launch(Dispatchers.IO) {
            async {
                updater.uiState.collect {
                    it.first?.let { glanceId ->
                        setState(context, it.second, glanceId)
                    }
                }
            }
            updater.updateData(appWidgetIds.toList())
        }
    }

    private suspend fun setState(context: Context, state: GradesWidgetUiState, glanceId: GlanceId) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->
            pref.toMutablePreferences().apply {
                this[gradesWidgetUiStateKey] = state.toJson()
            }
        }
        glanceAppWidget.update(context, glanceId)
    }

    companion object {
        val gradesWidgetUiStateKey = stringPreferencesKey("gradesWidgetUiState")
    }
}
