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
package com.instructure.pandautils.features.calendar.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarDayUiState
import com.instructure.pandautils.features.calendar.CalendarHeaderUiState
import com.instructure.pandautils.features.calendar.CalendarRowUiState
import com.instructure.pandautils.features.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Clock
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale

private const val MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR = 500
private const val HEADER_HEIGHT = 20
private const val CALENDAR_ROW_HEIGHT = 46

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Calendar(calendarUiState: CalendarUiState, actionHandler: (CalendarAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        var centerIndex by remember { mutableIntStateOf(Int.MAX_VALUE / 2) }
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            initialPageOffsetFraction = 0f
        ) {
            Int.MAX_VALUE
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                val monthOffset = page - centerIndex
                centerIndex = page
                actionHandler(CalendarAction.PageChanged(monthOffset))
            }
        }

        LaunchedEffect(calendarUiState.scrollToPageOffset) {
            if (calendarUiState.scrollToPageOffset != 0) {
                pagerState.animateScrollToPage(pagerState.currentPage + calendarUiState.scrollToPageOffset)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CalendarHeader(
            calendarUiState.headerUiState, calendarUiState.expanded, actionHandler, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            reverseLayout = false,
            pageSize = PageSize.Fill,
            pageContent = { page ->
                val settledPage = pagerState.settledPage

                val monthOffset = page - centerIndex
                val calendarBodyUiState = calendarUiState.bodyUiState
                val calendarPageUiState = when (monthOffset) {
                    -1 -> calendarBodyUiState.previousPage
                    1 -> calendarBodyUiState.nextPage
                    else -> calendarBodyUiState.currentPage
                }

                val rowsHeight =
                    if (calendarUiState.expanded) CALENDAR_ROW_HEIGHT * calendarBodyUiState.currentPage.calendarRows.size else CALENDAR_ROW_HEIGHT
                val height by animateIntAsState(targetValue = rowsHeight + HEADER_HEIGHT, label = "heightAnimation", finishedListener = {
                    actionHandler(CalendarAction.HeightAnimationFinished)
                })

                val rowsScaleRatio by animateFloatAsState(
                    targetValue = if (calendarUiState.expanded) 1.0f else 0.0f,
                    label = "animationScale"
                )

                if (page >= settledPage - 1 && page <= settledPage + 1) {
                    CalendarBody(
                        calendarPageUiState.calendarRows,
                        calendarUiState.pendingSelectedDay ?: calendarUiState.selectedDay,
                        scaleRatio = rowsScaleRatio,
                        selectedDayChanged = { actionHandler(CalendarAction.DaySelected(it)) },
                        modifier = Modifier.height(height.dp)
                    )
                } else {
                    Box(
                        Modifier
                            .height(height.dp)
                            .fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(ThemePrefs.buttonColor))
                    }
                }
            }
        )
        Divider(Modifier.fillMaxWidth(), color = colorResource(id = R.color.backgroundMedium), thickness = 0.5.dp)
    }
}

@Composable
fun CalendarHeader(
    headerUiState: CalendarHeaderUiState,
    calendarOpen: Boolean,
    actionHandler: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRotation: Float by animateFloatAsState(targetValue = if (calendarOpen) 0f else 180f, label = "expandIconRotation")

    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    if (screenHeightDp <= MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        actionHandler(CalendarAction.ExpandDisabled)
    } else {
        actionHandler(CalendarAction.ExpandEnabled)
    }

    var monthRowModifier = Modifier.semantics(mergeDescendants = true) {}
    if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        monthRowModifier = monthRowModifier.clickable(
            onClick = { actionHandler(CalendarAction.ExpandChanged) },
            onClickLabel = stringResource(id = if (calendarOpen) R.string.a11y_calendarSwitchToWeekView else R.string.a11y_calendarSwitchToMonthView)
        )
            .padding(8.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = monthRowModifier) {
            Text(
                text = headerUiState.yearTitle,
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Row {
                Text(
                    text = headerUiState.monthTitle,
                    fontSize = 22.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.height(30.dp)
                )
                if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
                    Icon(
                        painterResource(id = R.drawable.ic_chevron_down),
                        tint = colorResource(id = R.color.textDarkest),
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(iconRotation + 180)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
        Text(
            text = stringResource(id = R.string.calendarFilterCalendars),
            fontSize = 16.sp,
            color = Color(ThemePrefs.textButtonColor),
            modifier = Modifier.clickable {
                actionHandler(CalendarAction.FilterTapped)
            }.padding(horizontal = 8.dp, vertical = 12.dp))
    }
}

@Composable
fun CalendarBody(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier
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
        CalendarPage(calendarRows, selectedDay, selectedDayChanged, scaleRatio)
    }
}

@Composable
fun DayHeaders(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = DayOfWeek.values()
        // Shift the starting point to Sunday
        val shiftedDaysOfWeek = Array(7) { daysOfWeek[(it + 6) % 7] }

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
fun CalendarPage(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        calendarRows.forEach {
            // We only scale when it's expanding/collapsing, when it's not we need to show even the rows that don't have the selected day
            // to be able to see the neighbouring pages
            val scale = if (it.days.any { day -> day.date == selectedDay } || calendarRows.size == 1) 1.0f else scaleRatio
            DaysOfWeekRow(
                days = it.days, selectedDay, selectedDayChanged, modifier = Modifier
                    .height(CALENDAR_ROW_HEIGHT.dp * scale)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .scale(scaleX = 1.0f, scaleY = scale)
                    .alpha(scale)
            )
        }
    }
}

@Composable
fun DaysOfWeekRow(
    days: List<CalendarDayUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
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
            var dayModifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(32.dp))
                .clickable { selectedDayChanged(dayState.date) }
            if (dayState.date == selectedDay) {
                dayModifier = dayModifier
                    .background(
                        color = Color(ThemePrefs.buttonColor),
                        shape = RoundedCornerShape(500.dp),
                    )
            }
            dayModifier = dayModifier.wrapContentHeight(align = Alignment.CenterVertically)
            Column(
                Modifier
                    .width(32.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = dayState.dayNumber.toString(),
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = dayModifier,
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
                        EventIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun EventIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = 3.dp)
            .graphicsLayer()
            .clip(CircleShape)
            .size(4.dp)
            .background(Color(ThemePrefs.buttonColor))
    )
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    val calendarStateMapper = CalendarStateMapper(Clock.systemDefaultZone())
    val calendarUiState = CalendarUiState(
        LocalDate.now(),
        true,
        headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.now(), null),
        bodyUiState = calendarStateMapper.createBodyUiState(true, LocalDate.now(), false, 0, emptyMap())
    )
    Calendar(calendarUiState = calendarUiState, actionHandler = {})
}
