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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarEventsPageUiState
import com.instructure.pandautils.features.calendar.CalendarEventsUiState
import com.instructure.pandautils.features.calendar.EventUiState
import com.instructure.pandautils.features.calendar.composables.CalendarEvents
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
class CalendarEventsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    loading = true,
                    events = emptyList()
                )
            ), actionHandler = {})
        }

        val loading = composeTestRule.onNode(hasTestTag("loading0"))
        loading.assertIsDisplayed()
    }

    @Test
    fun assertError() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    error = true,
                    events = emptyList()
                )
            ), actionHandler = {})
        }

        val parentMatcher = (hasAnyAncestor(hasTestTag("calendarEventsPage0")))
        val errorText = composeTestRule.onNode(parentMatcher.and(hasText("There was an error loading your calendar")))
        errorText.assertIsDisplayed()
        val retryButton = composeTestRule.onNode(parentMatcher.and(hasText("Retry")))
        retryButton.assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun retryActionIsTriggeredWhenRetryButtonIsClicked() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    error = true,
                    events = emptyList()
                )
            ), actionHandler = { actions.add(it) })
        }

        val parentMatcher = (hasAnyAncestor(hasTestTag("calendarEventsPage0")))
        val retryButton = composeTestRule.onNode(parentMatcher.and(hasText("Retry")))
        retryButton.performClick()
        composeTestRule.waitForIdle()

        assertEquals(CalendarAction.Retry, actions.last())
    }

    @Test
    fun assertEmptyEvents() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    events = emptyList()
                )
            ), actionHandler = {})
        }

        val parentMatcher = (hasAnyAncestor(hasTestTag("calendarEventsPage0")))
        val emptyTitle = composeTestRule.onNode(parentMatcher.and(hasText("No Events Today!")))
        emptyTitle.assertIsDisplayed()
        val emptyDescription = composeTestRule.onNode(parentMatcher.and(hasText("It looks like a great day to rest, relax, and recharge.")))
        emptyDescription.assertIsDisplayed()
    }

    @Test
    fun assertEvents() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    events = listOf(
                        EventUiState(
                            1L,
                            "Course To Do",
                            CanvasContext.defaultCanvasContext(),
                            "Todo 1",
                            R.drawable.ic_assignment
                        ),
                        EventUiState(
                            2L,
                            "Course",
                            CanvasContext.defaultCanvasContext(),
                            "Assignment 1",
                            R.drawable.ic_assignment,
                            "Due Jan 9 at 8:00 AM",
                            "Missing"
                        ),
                        EventUiState(
                            3L,
                            "Course 2",
                            CanvasContext.defaultCanvasContext(),
                            "Discussion Checkpoints",
                            R.drawable.ic_discussion,
                            "Due Jan 9 at 9:00 AM",
                            tag = "Reply to topic"
                        )
                    )
                )
            ), actionHandler = {})
        }

        val event1Title = composeTestRule.onNode(hasText("Todo 1"))
        event1Title.assertIsDisplayed()
        val event1ContextName = composeTestRule.onNode(hasText("Course To Do"))
        event1ContextName.assertIsDisplayed()

        val event2Title = composeTestRule.onNode(hasText("Assignment 1"))
        event2Title.assertIsDisplayed()
        val event2ContextName = composeTestRule.onNode(hasText("Course"))
        event2ContextName.assertIsDisplayed()
        val event2Date = composeTestRule.onNode(hasText("Due Jan 9 at 8:00 AM"))
        event2Date.assertIsDisplayed()
        val event2Status = composeTestRule.onNode(hasText("Missing"))
        event2Status.assertIsDisplayed()

        val event3Title = composeTestRule.onNode(hasText("Discussion Checkpoints"))
        event3Title.assertIsDisplayed()
        val event3ContextName = composeTestRule.onNode(hasText("Course 2"))
        event3ContextName.assertIsDisplayed()
        val event3Date = composeTestRule.onNode(hasText("Due Jan 9 at 9:00 AM"))
        event3Date.assertIsDisplayed()
        val event3Tag= composeTestRule.onNode(hasText("Reply to topic"))
        event3Tag.assertIsDisplayed()
    }

    @Test
    fun eventClickTriggersEventSelectedAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            CalendarEvents(calendarEventsUiState = CalendarEventsUiState(
                currentPage = CalendarEventsPageUiState(
                    date = LocalDate.of(2023, 4, 20),
                    events = listOf(
                        EventUiState(
                            1L,
                            "Course To Do",
                            CanvasContext.defaultCanvasContext(),
                            "Todo 1",
                            R.drawable.ic_assignment
                        ),
                        EventUiState(
                            2L,
                            "Course",
                            CanvasContext.defaultCanvasContext(),
                            "Assignment 1",
                            R.drawable.ic_assignment,
                            "Due Jan 9 at 8:00 AM",
                            "Missing"
                        )
                    )
                )
            ), actionHandler = { actions.add(it) })
        }

        val event1Title = composeTestRule.onNode(hasText("Todo 1"))
        event1Title.assertIsDisplayed()
        event1Title.performClick()

        assertEquals(CalendarAction.EventSelected(1L), actions.last())
    }
}