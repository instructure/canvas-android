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
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.R
import com.instructure.student.widget.glance.Empty
import com.instructure.student.widget.glance.Error
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.NotLoggedIn
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import java.util.Locale


class ToDoWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val state = prefs[ToDoWidgetReceiver.toDoWidgetUiStateKey]?.fromJson<ToDoWidgetUiState>() ?: ToDoWidgetUiState(WidgetState.Loading)
            Content(state)
        }
    }

    @Composable
    private fun Content(
        toDoWidgetUiState: ToDoWidgetUiState
    ) {
        Scaffold(
            backgroundColor = WidgetColors.backgroundLightest,
            modifier = GlanceModifier.fillMaxSize()
        ) {
            when (toDoWidgetUiState.state) {
                WidgetState.Loading -> Loading()
                WidgetState.Error -> Error()
                WidgetState.Empty -> Empty()
                WidgetState.NotLoggedIn -> NotLoggedIn()
                WidgetState.Content -> ListContent(toDoWidgetUiState.plannerItems.groupBy { it.date }.toList())
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

    @Composable
    private fun ListContent(
        daysWithItems: List<Pair<LocalDate, List<WidgetPlannerItem>>>
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
    private fun DayItemContent(day: LocalDate, items: List<WidgetPlannerItem>, lastItem: Boolean) {
        Column {
            Row(
                modifier = GlanceModifier.padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier
                        .width(40.dp)
                ) {
                    val isToday = day == LocalDate.now()
                    val dayTextColor = if (isToday) {
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
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = TextStyle(
                                color = dayTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isToday) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        )
                        if (isToday) {
                            Image(
                                provider = ImageProvider(resId = R.drawable.ic_circle_stroke),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorProvider = dayTextColor),
                                modifier = GlanceModifier.size(32.dp)
                            )
                        }
                    }
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
    private fun PlannerItemContent(plannerItem: WidgetPlannerItem, lastItem: Boolean) {
        Column(
            modifier = GlanceModifier.padding(bottom = if (lastItem) 0.dp else 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val canvasContextColorProvider = ColorProvider(
                    color = Color(color = plannerItem.canvasContextColor)
                )

                Image(
                    provider = ImageProvider(resId = plannerItem.iconRes),
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
                    text = plannerItem.canvasContextText,
                    style = TextStyle(
                        color = canvasContextColorProvider,
                        fontSize = 12.sp
                    )
                )
            }
            Text(
                text = plannerItem.title,
                style = TextStyle(
                    color = WidgetColors.textDarkest,
                    fontSize = 14.sp
                )
            )
            Text(
                text = plannerItem.dateText,
                style = TextStyle(
                    color = WidgetColors.textDark,
                    fontSize = 12.sp
                )
            )
        }
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun ToDoWidgetPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            ToDoWidgetUiState(
                state = WidgetState.Content,
                plannerItems = listOf(
                    WidgetPlannerItem(
                        date = LocalDate.now(),
                        iconRes = R.drawable.ic_assignment,
                        canvasContextColor = android.graphics.Color.BLUE,
                        canvasContextText = "BIO 101",
                        title = "Test",
                        dateText = "7:00 AM to 7:30 AM",
                        url = "https://www.instructure.com"
                    ),
                    WidgetPlannerItem(
                        date = LocalDate.now(),
                        iconRes = R.drawable.ic_quiz,
                        canvasContextColor = android.graphics.Color.GREEN,
                        canvasContextText = "GA 101",
                        title = "Test 2",
                        dateText = "7:00 AM to 7:30 AM",
                        url = "https://www.instructure.com"
                    ),
                    WidgetPlannerItem(
                        date = LocalDate.now().plusDays(1),
                        iconRes = R.drawable.ic_calendar,
                        canvasContextColor = android.graphics.Color.RED,
                        canvasContextText = "MAT 101",
                        title = "Test 3",
                        dateText = "7:00 AM to 7:30 AM",
                        url = "https://www.instructure.com"
                    )
                )
            )
        )
    }
}
