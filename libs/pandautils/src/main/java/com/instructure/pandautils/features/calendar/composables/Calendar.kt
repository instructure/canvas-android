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
@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)

package com.instructure.pandautils.features.calendar.composables

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarDayUiState
import com.instructure.pandautils.features.calendar.CalendarHeaderUiState
import com.instructure.pandautils.features.calendar.CalendarRowUiState
import com.instructure.pandautils.features.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.WeekFields
import java.util.Locale

private const val MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR = 500
private const val HEADER_HEIGHT = 20
private const val CALENDAR_ROW_HEIGHT = 46
private const val PAGE_COUNT = 1000

@Composable
fun Calendar(
    calendarUiState: CalendarUiState,
    actionHandler: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null
) {
    Column(modifier = modifier) {
        var centerIndex by remember { mutableIntStateOf(PAGE_COUNT / 2) }
        val pagerState = rememberPagerState(
            initialPage = PAGE_COUNT / 2,
            initialPageOffsetFraction = 0f
        ) {
            PAGE_COUNT
        }

        val calendarBodyUiState = calendarUiState.bodyUiState
        val maxHeight = CALENDAR_ROW_HEIGHT + CALENDAR_ROW_HEIGHT * calendarBodyUiState.currentPage.calendarRows.size - 1

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
            calendarUiState.headerUiState, calendarUiState.expanded, actionHandler,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        AccessibilityButtons(
            previousContentDescription = calendarUiState.bodyUiState.previousPage.buttonContentDescription,
            nextContentDescription = calendarUiState.bodyUiState.nextPage.buttonContentDescription,
            pagerState = pagerState,
            expanded = calendarUiState.expanded
        )
        HorizontalPager(
            modifier = Modifier.swipeable(
                state = rememberSwipeableState(initialValue = if (calendarUiState.expanded) 1f else 0f, confirmStateChange = {
                    actionHandler(CalendarAction.ExpandChanged(it == 1f))
                    true
                }),
                orientation = Orientation.Vertical,
                anchors = mapOf(0f to 0f, maxHeight.toFloat() to 1f),
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
            ).testTag("calendarPager"),
            state = pagerState,
            beyondViewportPageCount = 2,
            reverseLayout = false,
            pageSize = PageSize.Fill,
            pageContent = { page ->
                val settledPage = pagerState.settledPage

                val monthOffset = page - centerIndex
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
                        todayFocusRequester = todayFocusRequester,
                        modifier = Modifier
                            .height(height.dp)
                            .testTag("calendarBody$monthOffset")
                    )
                } else {
                    Loading(
                        modifier = Modifier
                            .height(height.dp)
                            .fillMaxWidth()
                    )
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

    var monthRowModifier = Modifier
        .semantics(mergeDescendants = true) {
            role = Role.Button
        }
        .testTag("yearMonthTitle")
    if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        monthRowModifier = monthRowModifier
            .clickable(
                onClick = { actionHandler(CalendarAction.ExpandChanged(!calendarOpen)) },
                onClickLabel = stringResource(id = if (calendarOpen) R.string.a11y_calendarSwitchToWeekView else R.string.a11y_calendarSwitchToMonthView)
            )
            .padding(8.dp)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.defaultMinSize(minHeight = 30.dp)
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
                modifier = Modifier
                    .clickable {
                        actionHandler(CalendarAction.FilterTapped)
                    }
                    .padding(horizontal = 8.dp, vertical = 12.dp))
        }
        if (headerUiState.loadingMonths) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp), color = Color(ThemePrefs.buttonColor), backgroundColor = colorResource(id = R.color.backgroundLightest)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CalendarBody(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null
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
        CalendarPage(calendarRows, selectedDay, selectedDayChanged, scaleRatio, todayFocusRequester = todayFocusRequester)
    }
}

@Composable
fun DayHeaders(modifier: Modifier = Modifier) {
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
fun CalendarPage(
    calendarRows: List<CalendarRowUiState>,
    selectedDay: LocalDate,
    selectedDayChanged: (LocalDate) -> Unit,
    scaleRatio: Float,
    modifier: Modifier = Modifier,
    todayFocusRequester: FocusRequester? = null
) {
    Column(modifier = modifier) {
        calendarRows.forEachIndexed { index, it ->
            // We only scale when it's expanding/collapsing, when it's not we need to show even the rows that don't have the selected day
            // to be able to see the neighbouring pages
            val scale = if (it.days.any { day -> day.date == selectedDay } || calendarRows.size == 1) 1.0f else scaleRatio
            DaysOfWeekRow(
                days = it.days, selectedDay, selectedDayChanged, todayFocusRequester = todayFocusRequester, modifier = Modifier
                    .height(CALENDAR_ROW_HEIGHT.dp * scale)
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
fun DaysOfWeekRow(
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
            var textColor = when {
                dayState.date == selectedDay -> Color(ThemePrefs.buttonTextColor)
                dayState.today -> Color(ThemePrefs.textButtonColor)
                dayState.enabled -> colorResource(id = R.color.textDarkest)
                else -> colorResource(id = R.color.textDark)
            }
            var dayModifier = Modifier
                .width(32.dp)
                .height(32.dp)

            if (dayState.date == selectedDay) {
                dayModifier = dayModifier
                    .background(
                        color = Color(ThemePrefs.buttonColor),
                        shape = RoundedCornerShape(500.dp),
                    )
            }

            if (dayState.today && dayState.enabled && todayFocusRequester != null) {
                dayModifier = dayModifier.focusRequester(todayFocusRequester).focusable()
            }

            dayModifier = dayModifier
                .clip(RoundedCornerShape(32.dp))
                .clickable { selectedDayChanged(dayState.date) }
                .wrapContentHeight(align = Alignment.CenterVertically)

            Column(
                Modifier
                    .width(32.dp)
                    .wrapContentHeight()
            ) {
                val dayContentDescription =
                    dayState.contentDescription + " " + pluralStringResource(
                        id = R.plurals.a11y_calendar_day_event_count,
                        dayState.indicatorCount,
                        dayState.indicatorCount
                    )
                Text(
                    text = dayState.dayNumber.toString(),
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = dayModifier.semantics {
                        contentDescription = dayContentDescription
                    },
                    textAlign = TextAlign.Center,
                )
                Row(
                    Modifier
                        .height(10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(dayState.indicatorCount) {
                        EventIndicator(modifier = Modifier.clearAndSetSemantics { testTag = "eventIndicator$it" })
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

@Composable
fun AccessibilityButtons(
    previousContentDescription: String,
    nextContentDescription: String,
    pagerState: PagerState,
    expanded: Boolean
) {
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val a11yListener: (AccessibilityManager) -> Unit = { manager ->
        isAccessibilityEnabled = manager.isEnabled
    }

    LaunchedEffect(Unit) {
        isAccessibilityEnabled = isAccessibilityEnabled(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            accessibilityManager.addAccessibilityServicesStateChangeListener(a11yListener)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                accessibilityManager.removeAccessibilityServicesStateChangeListener(a11yListener)
            }
        }
    }
    if (isAccessibilityEnabled) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = stringResource(
                        id = if (expanded) R.string.a11y_calendarPreviousMonth else R.string.a11y_calendarPreviousWeek,
                        previousContentDescription
                    )
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.scrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward_arrow),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = stringResource(
                        id = if (expanded) R.string.a11y_calendarNextMonth else R.string.a11y_calendarNextWeek,
                        nextContentDescription
                    )
                )
            }
        }
    }
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
        headerUiState = calendarStateMapper.createHeaderUiState(LocalDate.now(), null, true),
        bodyUiState = calendarStateMapper.createBodyUiState(true, LocalDate.now(), false, 0, emptyMap())
    )
    Calendar(calendarUiState = calendarUiState, actionHandler = {})
}
