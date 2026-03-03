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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
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
import com.instructure.pandautils.compose.composables.calendar.CalendarBody
import com.instructure.pandautils.compose.composables.calendar.CalendarHeaderUiState
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.announceAccessibilityText
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
            modifier = Modifier
                .swipeable(
                    state = rememberSwipeableState(
                        initialValue = if (calendarUiState.expanded) 1f else 0f,
                        confirmStateChange = {
                            actionHandler(CalendarAction.ExpandChanged(it == 1f))
                            true
                        }),
                    orientation = Orientation.Vertical,
                    anchors = mapOf(0f to 0f, maxHeight.toFloat() to 1f),
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                )
                .testTag("calendarPager"),
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
                            .testTag("calendarBody$monthOffset"),
                        calendarRowHeightInDp = CALENDAR_ROW_HEIGHT
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
private fun CalendarHeader(
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

    val context = LocalContext.current

    var monthRowModifier = Modifier
        .testTag("yearMonthTitle")
    if (screenHeightDp > MIN_SCREEN_HEIGHT_FOR_FULL_CALENDAR) {
        val announcementText = stringResource(if (calendarOpen) R.string.a11y_calendarSwitchedToWeekView else R.string.a11y_calendarSwitchedToMonthView)
        monthRowModifier = monthRowModifier
            .clickable(
                onClick = {
                    actionHandler(CalendarAction.ExpandChanged(!calendarOpen))
                    announceAccessibilityText(context, announcementText)
                },
                onClickLabel = stringResource(id = R.string.a11y_calendarSwitchBetweenMonthAndWeekView)
            )
            .padding(8.dp)
    }

    val calendarExpandedStateTExt =
        stringResource(id = if (calendarOpen) R.string.a11y_calendarMonthView else R.string.a11y_calendarWeekView)
    val calendarHeadingButtonContentDescription = stringResource(
        id = R.string.a11y_calendarMonthButtonContentDescription,
        headerUiState.yearTitle,
        headerUiState.monthTitle,
        calendarExpandedStateTExt
    )
    monthRowModifier = monthRowModifier.clearAndSetSemantics {
        contentDescription = calendarHeadingButtonContentDescription
        role = Role.Button
        liveRegion = LiveRegionMode.Polite
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
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .semantics {
                        role = Role.Button
                    }
            )
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
private fun AccessibilityButtons(
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
