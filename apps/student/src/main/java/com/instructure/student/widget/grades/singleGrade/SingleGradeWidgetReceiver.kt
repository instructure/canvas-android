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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.pandautils.utils.toJson
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.grades.courseselector.CourseSelectorActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SingleGradeWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = SingleGradeWidget()

    @Inject
    lateinit var updater: SingleGradeWidgetUpdater

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context, appWidgetIds.toList())
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
        updateData(context, listOf(appWidgetId), showLoading = true, forceNetwork = false)
    }

    private suspend fun setState(
        context: Context,
        state: SingleGradeWidgetUiState,
        glanceId: GlanceId
    ) {
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

    private suspend fun handleAction(context: Context, action: SingleGradeWidgetAction, widgetIds: List<Int>) {
        when (action) {
            is SingleGradeWidgetAction.UpdateAllState -> {
                for (widgetId in widgetIds) {
                    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)
                    setState(
                        context,
                        action.state,
                        glanceId
                    )
                }
            }

            is SingleGradeWidgetAction.UpdateState -> {
                setState(context, action.state, action.glanceId)
            }

            SingleGradeWidgetAction.UpdateUi -> glanceAppWidget.updateAll(context)
        }
    }

    private fun updateData(
        context: Context,
        widgetIds: List<Int>,
        showLoading: Boolean = false,
        forceNetwork: Boolean = true
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            async {
                updater.events.collect { action ->
                    handleAction(context, action, widgetIds)
                }
            }
            updater.updateData(widgetIds, showLoading, forceNetwork)
        }
    }

    companion object {
        val singleGradeWidgetUiStateKey = stringPreferencesKey("singleGradeWidgetUiState")
    }
}
