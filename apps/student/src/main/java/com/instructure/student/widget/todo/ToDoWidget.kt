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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.fromJson
import com.instructure.pandautils.utils.toLocalDate
import com.instructure.student.R
import com.instructure.student.widget.glance.Empty
import com.instructure.student.widget.glance.Error
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.NotLoggedIn
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import org.threeten.bp.LocalDate
import java.util.Locale


class ToDoWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val state = WidgetState.valueOf(prefs[ToDoWidgetReceiver.stateKey] ?: WidgetState.Loading.name)
            val plannerItems = prefs[ToDoWidgetReceiver.plannerItemsKey]?.map {
                it.fromJson<PlannerItem>()
            }.orEmpty().groupBy {
                it.plannableDate.toLocalDate()
            }.toList()

            Scaffold(
                backgroundColor = WidgetColors.backgroundLightest,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                when (state) {
                    WidgetState.Loading -> Loading()
                    WidgetState.Error -> Error()
                    WidgetState.Empty -> Empty()
                    WidgetState.NotLoggedIn -> NotLoggedIn()
                    WidgetState.Content -> Content(plannerItems)
                }
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Image(
                        provider = ImageProvider(resId = R.drawable.ic_canvas_logo_student),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorProvider = WidgetColors.textLightest),
                        modifier = GlanceModifier
                            .size(32.dp)
                            .cornerRadius(20.dp)
                            .background(WidgetColors.textDanger)
                            .padding(8.dp)
                            .clickable {

                            }
                    )
                }
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Image(
                        provider = ImageProvider(resId = R.drawable.ic_add_lined),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            colorProvider = ColorProvider(
                                color = Color(color = ThemePrefs.buttonTextColor)
                            )
                        ),
                        modifier = GlanceModifier
                            .size(32.dp)
                            .cornerRadius(20.dp)
                            .background(
                                colorProvider = ColorProvider(
                                    color = Color(color = ThemePrefs.buttonColor)
                                )
                            )
                            .padding(8.dp)
                            .clickable {

                            }
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(
        daysWithItems: List<Pair<LocalDate, List<PlannerItem>>>
    ) {
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            itemsIndexed(items = daysWithItems) { index, item ->
                DayItemContent(item.first, item.second, index == daysWithItems.lastIndex)
            }
        }
    }

    @Composable
    private fun DayItemContent(day: LocalDate, items: List<PlannerItem>, lastItem: Boolean) {
        Column {
            Row(
                modifier = GlanceModifier.padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier
                        .width(40.dp)
                ) {
                    val dayTextColor = if (day == LocalDate.now()) {
                        ColorProvider(color = Color(color = ThemePrefs.brandColor))
                    } else {
                        WidgetColors.textDark
                    }

                    Text(
                        text = day.dayOfWeek.getDisplayName(
                            org.threeten.bp.format.TextStyle.SHORT,
                            Locale.getDefault()
                        ),
                        style = TextStyle(
                            color = dayTextColor,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = TextStyle(
                            color = dayTextColor,
                            fontSize = 12.sp
                        )
                    )
                }
                Column {
                    items.forEachIndexed { index, item ->
                        PlannerItemContent(item, index == items.lastIndex)
                    }
                }
            }
            if (!lastItem) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth()
                        .height(.5.dp)
                        .background(WidgetColors.borderMedium)
                ) {}
            }
        }
    }

    @Composable
    private fun PlannerItemContent(plannerItem: PlannerItem, lastItem: Boolean) {
        Column(
            modifier = GlanceModifier.padding(bottom = if (lastItem) 0.dp else 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canvasContextColorProvider = ColorProvider(
                    color = Color(color = plannerItem.canvasContext.color)
                )

                Image(
                    provider = getIconForPlannerItem(plannerItem),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorProvider = canvasContextColorProvider),
                    modifier = GlanceModifier.size(16.dp)
                )
                Spacer(
                    modifier = GlanceModifier.width(4.dp)
                )
                Text(
                    text = "|",
                    style = TextStyle(
                        color = WidgetColors.textDark,
                        fontSize = 16.sp
                    )
                )
                Spacer(
                    modifier = GlanceModifier.width(4.dp)
                )
                Text(
                    text = plannerItem.canvasContext.contextId,
                    style = TextStyle(
                        color = canvasContextColorProvider,
                        fontSize = 12.sp
                    )
                )
            }
            Text(
                text = plannerItem.plannable.title,
                style = TextStyle(
                    color = WidgetColors.textDarkest,
                    fontSize = 14.sp
                )
            )
            val dateText = getDateTextForPlannerItem(plannerItem)
            dateText?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        color = WidgetColors.textDark,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun getIconForPlannerItem(plannerItem: PlannerItem): ImageProvider {
        val iconRes = when (plannerItem.plannableType) {
            PlannableType.ASSIGNMENT, PlannableType.SUB_ASSIGNMENT -> R.drawable.ic_assignment
            PlannableType.QUIZ -> R.drawable.ic_quiz
            PlannableType.CALENDAR_EVENT -> R.drawable.ic_calendar
            PlannableType.DISCUSSION_TOPIC -> R.drawable.ic_discussion
            PlannableType.PLANNER_NOTE -> R.drawable.ic_todo
            else -> R.drawable.ic_calendar
        }

        return ImageProvider(iconRes)
    }

    @Composable
    fun getDateTextForPlannerItem(plannerItem: PlannerItem): String? {
        val context = LocalContext.current

        return when (plannerItem.plannableType) {
            PlannableType.PLANNER_NOTE -> {
                plannerItem.plannable.todoDate.toDate()?.let {
                    val dateText = DateHelper.dayMonthDateFormat.format(it)
                    val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                    context.getString(R.string.calendarAtDateTime, dateText, timeText)
                }
            }

            PlannableType.CALENDAR_EVENT -> {
                val startDate = plannerItem.plannable.startAt
                val endDate = plannerItem.plannable.endAt
                if (startDate != null && endDate != null) {
                    val dateText = DateHelper.dayMonthDateFormat.format(startDate)
                    val startText = DateHelper.getFormattedTime(context, startDate).orEmpty()
                    val endText = DateHelper.getFormattedTime(context, endDate).orEmpty()

                    when {
                        plannerItem.plannable.allDay == true -> dateText
                        startDate == endDate -> context.getString(R.string.calendarAtDateTime, dateText, startText)
                        else -> context.getString(R.string.calendarFromTo, dateText, startText, endText)
                    }
                } else null
            }

            else -> {
                plannerItem.plannable.dueAt?.let {
                    val dateText = DateHelper.dayMonthDateFormat.format(it)
                    val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                    context.getString(R.string.calendarDueDate, dateText, timeText)
                }
            }
        }
    }
}
