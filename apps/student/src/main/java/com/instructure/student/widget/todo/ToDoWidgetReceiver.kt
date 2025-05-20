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
import com.instructure.canvasapi2.models.Course
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
import org.threeten.bp.LocalDate
import javax.inject.Inject


private const val PLANNER_DATE_RANGE_DAYS = 28L

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

                glanceAppWidget.update(context, glanceId)
            }

            setState(ToDoWidgetUiState(WidgetState.Loading))

            val user = apiPrefs.user
            if (user == null) {
                setState(ToDoWidgetUiState(WidgetState.NotLoggedIn))
                return@launch
            }

            try {
                val courses = repository.getFavouriteCourses(true)
                val groups = repository.getFavouriteGroups(true)

                val contextCodes = buildList {
                    addAll(courses.map { it.contextId })
                    addAll(groups.map { it.contextId })
                    apiPrefs.user?.contextId?.let { add(it) }
                }

                val startDateTime = LocalDate.now().atStartOfDay()
                val plannerItems = repository.getPlannerItems(
                    startDateTime.toApiString().orEmpty(),
                    startDateTime.plusDays(PLANNER_DATE_RANGE_DAYS).toApiString().orEmpty(),
                    contextCodes,
                    true
                )

                val toDoWidgetUiState = ToDoWidgetUiState(
                    if (plannerItems.isEmpty()) {
                        WidgetState.Empty
                    } else {
                        WidgetState.Content
                    },
                    plannerItems.map {
                        it.toWidgetPlannerItem(context, courses)
                    }
                )
                setState(toDoWidgetUiState)
            } catch (e: Exception) {
                setState(ToDoWidgetUiState(WidgetState.Error))
            }
        }
    }

    private fun PlannerItem.toWidgetPlannerItem(
        context: Context,
        courses: List<Course>
    ) = WidgetPlannerItem(
        date = plannableDate.toLocalDate(),
        iconRes = getIconForPlannerItem(),
        canvasContextColor = canvasContext.color,
        canvasContextText = getContextNameForPlannerItem(context, courses),
        title = plannable.title,
        dateText = getDateTextForPlannerItem(context).orEmpty(),
        url = getUrl(),
    )

    private fun PlannerItem.getContextNameForPlannerItem(context: Context, courses: List<Course>): String {
        val courseCode = courses.find { it.id == canvasContext.id }?.courseCode
        return when (plannableType) {
            PlannableType.PLANNER_NOTE -> {
                if (contextName.isNullOrEmpty()) {
                    context.getString(R.string.userCalendarToDo)
                } else {
                    context.getString(R.string.courseToDo, courseCode)
                }
            }

            else -> {
                if (canvasContext is Course) {
                    courseCode.orEmpty()
                } else {
                    contextName.orEmpty()
                }
            }
        }
    }

    private fun PlannerItem.getDateTextForPlannerItem(context: Context): String? {
        return when (plannableType) {
            PlannableType.PLANNER_NOTE -> {
                plannable.todoDate.toDate()?.let {
                    DateHelper.getFormattedTime(context, it)
                }
            }

            PlannableType.CALENDAR_EVENT -> {
                val startDate = plannable.startAt
                val endDate = plannable.endAt
                if (startDate != null && endDate != null) {
                    val startText = DateHelper.getFormattedTime(context, startDate).orEmpty()
                    val endText = DateHelper.getFormattedTime(context, endDate).orEmpty()

                    when {
                        plannable.allDay == true -> context.getString(R.string.widgetAllDay)
                        startDate == endDate -> startText
                        else -> context.getString(R.string.widgetFromTo, startText, endText)
                    }
                } else null
            }

            else -> {
                plannable.dueAt?.let {
                    val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                    context.getString(R.string.widgetDueDate, timeText)
                }
            }
        }
    }

    private fun PlannerItem.getUrl(): String {
        val url = when (plannableType) {
            PlannableType.CALENDAR_EVENT -> {
                "/${canvasContext.type.apiString}/${canvasContext.id}/calendar_events/${plannable.id}"
            }

            PlannableType.PLANNER_NOTE -> {
                "/todos/${plannable.id}"
            }

            else -> {
                htmlUrl.orEmpty()
            }
        }

        return if (url.startsWith("/")) {
            apiPrefs.fullDomain + url
        } else {
            url
        }
    }

    companion object {
        val toDoWidgetUiStateKey = stringPreferencesKey("toDoWidgetUiState")
    }
}
