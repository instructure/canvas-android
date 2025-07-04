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

package com.instructure.student.widget.todo

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.utils.toJson
import com.instructure.student.widget.WidgetLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ToDoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = ToDoWidget()

    @Inject
    lateinit var toDoWidgetUpdater: ToDoWidgetUpdater

    @Inject
    lateinit var widgetLogger: WidgetLogger

    private val coroutineScope = MainScope()

    override fun onEnabled(context: Context?) {
        context?.let {
            coroutineScope.launch(Dispatchers.IO) {
                widgetLogger.logEvent(AnalyticsEventConstants.WIDGET_TODO_WIDGET_ADDED, context)
            }
        }
        super.onEnabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        coroutineScope.launch(Dispatchers.IO) {
            widgetLogger.logEvent(AnalyticsEventConstants.WIDGET_TODO_WIDGET_DELETED, context)
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context)
    }

    private fun updateData(context: Context) {
        coroutineScope.launch {
            toDoWidgetUpdater.updateData(context).collectLatest {
                val glanceId = GlanceAppWidgetManager(context)
                    .getGlanceIds(ToDoWidget::class.java)
                    .firstOrNull() ?: return@collectLatest

                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[toDoWidgetUiStateKey] = it.toJson()
                    }
                }

                glanceAppWidget.update(context, glanceId)
            }
        }
    }

    companion object {
        val toDoWidgetUiStateKey = stringPreferencesKey("toDoWidgetUiState")
    }
}
