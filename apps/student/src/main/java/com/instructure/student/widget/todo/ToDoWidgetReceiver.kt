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
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.toJson
import com.instructure.student.widget.glance.WidgetState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject


@AndroidEntryPoint
class ToDoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = ToDoWidget()

    @Inject
    lateinit var repository: ToDoWidgetRepository

    @Inject
    lateinit var apiPrefs: ApiPrefs

    private val coroutineScope = MainScope()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateData(context)
    }

    private fun updateData(context: Context) {
        coroutineScope.launch {
            val glanceId = GlanceAppWidgetManager(context)
                .getGlanceIds(ToDoWidget::class.java)
                .firstOrNull() ?: return@launch

            suspend fun setState(state: WidgetState, items: Set<String> = emptySet()) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[plannerItemsKey] = items
                        this[stateKey] = state.name
                    }
                }
            }

            val user = apiPrefs.user
            if (user == null) {
                setState(WidgetState.NotLoggedIn)
                glanceAppWidget.update(context, glanceId)
                return@launch
            }

            try {
                val now = LocalDateTime.now()
                val plannerItems = repository.getPlannerItems(
                    now.toApiString().orEmpty(),
                    now.plusDays(28).toApiString().orEmpty(),
                    true
                )

                val jsonItems = plannerItems.map { it.toJson() }.toSet()
                val newState = if (plannerItems.isEmpty()) WidgetState.Empty else WidgetState.Content

                setState(newState, jsonItems)
            } catch (e: Exception) {
                setState(WidgetState.Error)
            }

            glanceAppWidget.update(context, glanceId)
        }
    }

    companion object {
        val plannerItemsKey = stringSetPreferencesKey("plannerItems")
        val stateKey = stringPreferencesKey("state")
    }
}
