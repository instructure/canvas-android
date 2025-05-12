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
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getIconForPlannerItem
import com.instructure.pandautils.utils.toJson
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.student.R
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

            suspend fun setState(state: ToDoWidgetUiState) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[toDoWidgetUiStateKey] = state.toJson()
                    }
                }
            }

            val user = apiPrefs.user
            if (user == null) {
                setState(ToDoWidgetUiState(WidgetState.NotLoggedIn))
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

                val state = if (plannerItems.isEmpty()) {
                    WidgetState.Empty
                } else {
                    WidgetState.Content
                }
                val toDoWidgetUiState = ToDoWidgetUiState(state, plannerItems.map { it.toWidgetPlannerItem(context) })
                setState(toDoWidgetUiState)
            } catch (e: Exception) {
                setState(ToDoWidgetUiState(WidgetState.Error))
            }

            glanceAppWidget.update(context, glanceId)
        }
    }

    private fun PlannerItem.toWidgetPlannerItem(context: Context) = WidgetPlannerItem(
        date = this.plannableDate.toLocalDate(),
        iconRes = this.getIconForPlannerItem(),
        canvasContextColor = this.canvasContext.color,
        canvasContextText = this.canvasContext.name.orEmpty(),
        title = this.plannable.title,
        dateText = this.getDateTextForPlannerItem(context).orEmpty(),
        url = this.htmlUrl.orEmpty(),
    )

    private fun PlannerItem.getDateTextForPlannerItem(context: Context): String? {
        return when (this.plannableType) {
            PlannableType.PLANNER_NOTE -> {
                this.plannable.todoDate.toDate()?.let {
                    val dateText = DateHelper.dayMonthDateFormat.format(it)
                    val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                    context.getString(R.string.calendarAtDateTime, dateText, timeText)
                }
            }

            PlannableType.CALENDAR_EVENT -> {
                val startDate = this.plannable.startAt
                val endDate = this.plannable.endAt
                if (startDate != null && endDate != null) {
                    val dateText = DateHelper.dayMonthDateFormat.format(startDate)
                    val startText = DateHelper.getFormattedTime(context, startDate).orEmpty()
                    val endText = DateHelper.getFormattedTime(context, endDate).orEmpty()

                    when {
                        this.plannable.allDay == true -> dateText
                        startDate == endDate -> context.getString(R.string.calendarAtDateTime, dateText, startText)
                        else -> context.getString(R.string.calendarFromTo, dateText, startText, endText)
                    }
                } else null
            }

            else -> {
                this.plannable.dueAt?.let {
                    val dateText = DateHelper.dayMonthDateFormat.format(it)
                    val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                    context.getString(R.string.calendarDueDate, dateText, timeText)
                }
            }
        }
    }

    companion object {
        val toDoWidgetUiStateKey = stringPreferencesKey("toDoWidgetUiState")
    }
}
