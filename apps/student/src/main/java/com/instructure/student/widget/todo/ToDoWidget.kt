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
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.fromJson
import com.instructure.student.R
import com.instructure.student.activity.InterwebsToApplication
import com.instructure.student.widget.glance.Error
import com.instructure.student.widget.glance.Loading
import com.instructure.student.widget.glance.WidgetColors
import com.instructure.student.widget.glance.WidgetState
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
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
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.backgroundLightest)
                .cornerRadius(16.dp)
        ) {
            val state = toDoWidgetUiState.state
            when (state) {
                WidgetState.Loading -> Loading()

                WidgetState.Error -> Error(
                    imageRes = R.drawable.ic_panda_notsupported,
                    titleRes = R.string.widgetErrorTitle,
                    subtitleRes = R.string.widgetErrorSubtitle
                )

                WidgetState.Empty -> Error(
                    imageRes = R.drawable.ic_no_events,
                    titleRes = R.string.widgetToDoEmptyTitle,
                    subtitleRes = R.string.widgetToDoEmptySubtitle
                )

                WidgetState.NotLoggedIn -> Error(
                    imageRes = R.drawable.ic_smart_search_empty,
                    titleRes = R.string.widgetNotLoggedInTitle,
                    subtitleRes = R.string.widgetToDoNotLoggedInSubtitle
                )

                WidgetState.Content -> ListContent(toDoWidgetUiState.plannerItems.groupBy { it.date }.toList())
            }
            WidgetFloatingActionButton(
                alignment = Alignment.TopEnd,
                imageRes = R.drawable.ic_canvas_logo_student,
                contentDescriptionRes = R.string.a11y_widgetToDoOpenToDoList,
                backgroundColor = WidgetColors.textDanger,
                tintColor = WidgetColors.textLightest,
                onClickAction = actionStartActivity(
                    InterwebsToApplication.createIntent(
                        LocalContext.current,
                        Uri.parse("${ApiPrefs.fullDomain}/todolist")
                    )
                )
            )
            when (state) {
                WidgetState.Error -> {
                    WidgetFloatingActionButton(
                        alignment = Alignment.BottomEnd,
                        imageRes = R.drawable.ic_refresh_lined,
                        contentDescriptionRes = R.string.a11y_refresh,
                        backgroundColor = ColorProvider(
                            color = Color(color = ThemePrefs.buttonColor)
                        ),
                        tintColor = ColorProvider(
                            color = Color(color = ThemePrefs.buttonTextColor)
                        ),
                        onClickAction = actionRunCallback<ToDoWidgetRefreshCallback>()
                    )
                }

                WidgetState.Content, WidgetState.Empty -> {
                    WidgetFloatingActionButton(
                        alignment = Alignment.BottomEnd,
                        imageRes = R.drawable.ic_add_lined,
                        contentDescriptionRes = R.string.a11y_widgetToDoCreateNewToDo,
                        backgroundColor = ColorProvider(
                            color = Color(color = ThemePrefs.buttonColor)
                        ),
                        tintColor = ColorProvider(
                            color = Color(color = ThemePrefs.buttonTextColor)
                        ),
                        onClickAction = actionStartActivity(
                            InterwebsToApplication.createIntent(
                                LocalContext.current,
                                Uri.parse("${ApiPrefs.fullDomain}/todos/new")
                            )
                        )
                    )
                }

                else -> {}
            }
        }
    }

    @Composable
    private fun WidgetFloatingActionButton(
        alignment: Alignment,
        @DrawableRes imageRes: Int,
        @StringRes contentDescriptionRes: Int,
        backgroundColor: ColorProvider,
        tintColor: ColorProvider,
        onClickAction: Action
    ) {
        Box(
            contentAlignment = alignment,
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Image(
                provider = ImageProvider(resId = imageRes),
                contentDescription = LocalContext.current.getString(contentDescriptionRes),
                colorFilter = ColorFilter.tint(tintColor),
                modifier = GlanceModifier
                    .size(32.dp)
                    .cornerRadius(20.dp)
                    .background(backgroundColor)
                    .padding(8.dp)
                    .clickable(onClickAction)
            )
        }
    }

    @Composable
    private fun ListContent(
        daysWithItems: List<Pair<LocalDate, List<WidgetPlannerItem>>>
    ) {
        LazyColumn(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            daysWithItems.forEachIndexed { dayIndex, dayItem ->
                itemsIndexed(items = dayItem.second) { index, item ->
                    ListItemContent(item, index == 0)
                }
                if (dayIndex != daysWithItems.lastIndex) {
                    item {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(.5.dp)
                                .background(WidgetColors.borderMedium)
                        ) {}
                    }
                }
            }
        }
    }

    @Composable
    private fun ListItemContent(item: WidgetPlannerItem, showDay: Boolean) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(
                    actionStartActivity(
                        InterwebsToApplication.createIntent(
                            LocalContext.current,
                            Uri.parse("${ApiPrefs.fullDomain}/calendar/${item.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                        )
                    )
                )
        ) {
            if (showDay) {
                DayContent(item.date)
            } else {
                Spacer(modifier = GlanceModifier.width(40.dp))
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            PlannerItemContent(item)
        }
    }

    @Composable
    private fun DayContent(day: LocalDate) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier
                .width(40.dp)
                .padding(vertical = 2.dp)
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
    }

    @Composable
    private fun PlannerItemContent(plannerItem: WidgetPlannerItem) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionStartActivity(
                        InterwebsToApplication.createIntent(
                            LocalContext.current,
                            Uri.parse(plannerItem.url)
                        )
                    )
                )
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
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = plannerItem.title,
                style = TextStyle(
                    color = WidgetColors.textDarkest,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
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
    private fun ToDoWidgetContentPreview() {
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

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun ToDoWidgetEmptyPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            ToDoWidgetUiState(
                state = WidgetState.Empty
            )
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun ToDoWidgetErrorPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            ToDoWidgetUiState(
                state = WidgetState.Error
            )
        )
    }

    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 200)
    @Composable
    private fun ToDoWidgetNotLoggedInPreview() {
        ContextKeeper.appContext = LocalContext.current
        AndroidThreeTen.init(LocalContext.current)
        Content(
            ToDoWidgetUiState(
                state = WidgetState.NotLoggedIn
            )
        )
    }
}
