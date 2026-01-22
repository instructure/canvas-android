/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables.calendar

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Clock
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.util.Locale

private const val DEFAULT_CALENDAR_ROW_HEIGHT = 46

@Composable
fun CalendarBody(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null,
    calendarRowHeightInDp: Int = DEFAULT_CALENDAR_ROW_HEIGHT
) {
    Column(
        modifier
            .background(colorResource(id = R.color.backgroundLightest))
    ) {
        DayHeaders(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        CalendarPage(
            calendarRows,
            selectedDay,
            selectedDayChanged,
            scaleRatio,
            todayFocusRequester = todayFocusRequester,
            calendarRowHeightInDp = calendarRowHeightInDp
        )
    }
}

@Composable
private fun DayHeaders(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics { testTag = "dayHeaders" }, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = DayOfWeek.entries.toTypedArray()
        val localeFirstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
        // Shift the starting point to the correct day
        val shiftAmount = localeFirstDayOfWeek - daysOfWeek.first().value
        val shiftedDaysOfWeek = Array(7) { daysOfWeek[(it + shiftAmount) % 7] }

        for (day in shiftedDaysOfWeek) {
            val headerText = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val colorResource =
                if (day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY) R.color.textDark else R.color.textDarkest
            Text(
                text = headerText,
                fontSize = 12.sp,
                color = colorResource(id = colorResource),
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarPage(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null,
    calendarRowHeightInDp: Int = DEFAULT_CALENDAR_ROW_HEIGHT
) {
    Column(modifier = modifier) {
        calendarRows.forEachIndexed { index, it ->
            // We only scale when it's expanding/collapsing, when it's not we need to show even the rows that don't have the selected day
            // to be able to see the neighbouring pages
            val scale = if (it.days.any { day -> day.date == selectedDay } || calendarRows.size == 1) 1.0f else scaleRatio
            DaysOfWeekRow(
                days = it.days, selectedDay, selectedDayChanged, todayFocusRequester = todayFocusRequester, modifier = Modifier
                    .height(calendarRowHeightInDp.dp * scale)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .scale(scaleX = 1.0f, scaleY = scale)
                    .alpha(scale)
                    .testTag("calendarRow$index")
            )
        }
    }
}

@Composable
private fun DaysOfWeekRow(
    days: List<CalendarDayUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { dayState ->
            val textColor = when {
                dayState.date == selectedDay -> Color(ThemePrefs.buttonTextColor)
                dayState.today -> Color(ThemePrefs.textButtonColor)
                dayState.enabled -> colorResource(id = R.color.textDarkest)
                else -> colorResource(id = R.color.textDark)
            }
            val dayContentDescription =
                dayState.contentDescription + " " + pluralStringResource(
                    id = R.plurals.a11y_calendar_day_event_count,
                    dayState.indicatorCount,
                    dayState.indicatorCount
                )

            var columnModifier = Modifier
                .width(32.dp)
                .wrapContentHeight()

            if (dayState.today && dayState.enabled && todayFocusRequester != null) {
                columnModifier = columnModifier
                    .focusRequester(todayFocusRequester)
                    .focusable()
            }

            Column(
                columnModifier
                    .testTag(dayState.dayNumber.toString())
                    .selectable(dayState.date == selectedDay) {
                        selectedDayChanged(dayState.date)
                    }
                    .semantics(mergeDescendants = true) {
                        contentDescription = dayContentDescription
                        role = Role.Button
                    }
            ) {
                var dayModifier = Modifier
                    .width(32.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(32.dp))

                if (dayState.date == selectedDay) {
                    dayModifier = dayModifier
                        .background(
                            color = Color(ThemePrefs.buttonColor),
                            shape = RoundedCornerShape(500.dp),
                        )
                }

                Text(
                    text = dayState.dayNumber.toString(),
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = dayModifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .clearAndSetSemantics { },
                    textAlign = TextAlign.Center
                )
                Row(
                    Modifier
                        .height(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(dayState.indicatorCount) {
                        EventIndicator(modifier = Modifier.clearAndSetSemantics {
                            testTag = "eventIndicator$it"
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun EventIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = 3.dp)
            .graphicsLayer()
            .clip(CircleShape)
            .size(4.dp)
            .background(Color(ThemePrefs.buttonColor))
    )
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun CalendarBodyFullMonthPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())
    val bodyUiState = calendarStateMapper.createBodyUiState(
        expanded = true,
        selectedDay = LocalDate.now(),
        jumpToToday = false,
        scrollToPageOffset = 0,
        eventIndicators = mapOf(
            LocalDate.now().plusDays(1) to 2,
            LocalDate.now().plusDays(3) to 1,
            LocalDate.now().plusDays(5) to 3
        )
    )

    CalendarBody(
        calendarRows = bodyUiState.currentPage.calendarRows,
        selectedDay = LocalDate.now(),
        selectedDayChanged = {},
        scaleRatio = 1.0f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun CalendarBodyWeekPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())
    val bodyUiState = calendarStateMapper.createBodyUiState(
        expanded = false,
        selectedDay = LocalDate.now(),
        jumpToToday = false,
        scrollToPageOffset = 0,
        eventIndicators = mapOf(
            LocalDate.now().plusDays(1) to 2,
            LocalDate.now().plusDays(2) to 1
        )
    )

    CalendarBody(
        calendarRows = bodyUiState.currentPage.calendarRows,
        selectedDay = LocalDate.now(),
        selectedDayChanged = {},
        scaleRatio = 1.0f,
        modifier = Modifier.fillMaxWidth()
    )
}