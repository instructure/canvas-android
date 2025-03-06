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
package com.instructure.pandautils.compose.features.calendar

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.features.calendar.composables.Calendar
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

@RunWith(AndroidJUnit4::class)
class CalendarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var stateMapper: CalendarStateMapper

    @Test
    fun calendarHeaderShowsTheCorrectDateAndFilterButton() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = {},
            )
        }

        val yearMonthTitle = composeTestRule.onNodeWithTag("yearMonthTitle", useUnmergedTree = true)
        yearMonthTitle
            .assertIsDisplayed()
            .assertContentDescriptionEquals("2023, April, Calendar is in month view")
            .assertHasClickAction()
        val calendarsButton = composeTestRule.onNodeWithText("Calendars")
        calendarsButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun calendarShowsCorrectDayHeaders() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = {},
            )
        }

        val dayHeadersMatcher = hasTestTag("dayHeaders").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        val dayHeaders = composeTestRule.onNode(dayHeadersMatcher)
        dayHeaders.assertIsDisplayed()
    }

    @Test
    fun calendarShowsMultipleWeeksWhenExpanded() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = {},
            )
        }

        val calendarRowMatcher = hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        val calendarRow = composeTestRule.onNode(calendarRowMatcher)
        calendarRow.assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("26")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("27")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("28")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("29")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("30")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("31")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("1")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()

        val calendarRowMatcher2 = hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        val calendarRow2 = composeTestRule.onNode(calendarRowMatcher2)
        calendarRow2.assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("2")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("3")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("4")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("5")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("6")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("7")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("8")).and(hasAnyAncestor(calendarRowMatcher2))).assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun calendarShowsOneWeekWhenCollapsed() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), false, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(false, LocalDate.now(clock))
                ),
                actionHandler = {},
            )
        }

        val calendarRowMatcher = hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        val calendarRow = composeTestRule.onNode(calendarRowMatcher)
        calendarRow.assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("16")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("17")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("18")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("19")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("20")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("21")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNode((hasTestTag("22")).and(hasAnyAncestor(calendarRowMatcher))).assertIsDisplayed().assertHasClickAction()

        composeTestRule.onNode(hasTestTag("calendarRow1").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow2").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow3").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
        composeTestRule.onNode(hasTestTag("calendarRow4").and(hasAnyAncestor(hasTestTag("calendarBody0")))).assertIsNotDisplayed()
    }

    @Test
    fun eventIndicatorsAreDisplayedForDaysWithEvents() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)

            val eventIndicators = mapOf(LocalDate.now(clock).plusDays(1) to 2)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), false, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(false, LocalDate.now(clock), eventIndicators = eventIndicators)
                ),
                actionHandler = {},
            )
        }

        val calendarRowMatcher = hasTestTag("calendarRow0").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        val calendarRow = composeTestRule.onNode(calendarRowMatcher)
        calendarRow.assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("eventIndicator0")), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("eventIndicator1")), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode((hasTestTag("eventIndicator2")), useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun clickingCalendarMonthPerformsExpandChangedAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = { actions.add(it) },
            )
        }

        val yearMonthTitle = composeTestRule.onNodeWithTag("yearMonthTitle")
        yearMonthTitle.assertIsDisplayed()
        yearMonthTitle.performClick()
        assertEquals(CalendarAction.ExpandChanged(false), actions.last())
    }

    @Test
    fun clickingCalendarFiltersPerformsFilterTappedEvent() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = { actions.add(it) },
            )
        }

        val calendarsButton = composeTestRule.onNodeWithText("Calendars")
        calendarsButton.performClick()
        assertEquals(CalendarAction.FilterTapped, actions.last())
    }

    @Test
    fun selectingADayTriggersSelectDayAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            val clock = Clock.fixed(Instant.parse("2023-04-20T14:00:00.00Z"), ZoneId.systemDefault())
            stateMapper = CalendarStateMapper(clock)
            Calendar(
                calendarUiState = CalendarUiState(
                    LocalDate.now(clock), true, stateMapper.createHeaderUiState(
                        LocalDate.now(clock), null
                    ), stateMapper.createBodyUiState(true, LocalDate.now(clock))
                ),
                actionHandler = { actions.add(it) },
            )
        }

        val calendarRowMatcher = hasTestTag("calendarRow3").and(hasAnyAncestor(hasTestTag("calendarBody0")))
        composeTestRule.onNode((hasTestTag("16")).and(hasAnyAncestor(calendarRowMatcher))).performClick()

        assertEquals(CalendarAction.DaySelected(LocalDate.of(2023, 4, 16)), actions.last())
    }
}