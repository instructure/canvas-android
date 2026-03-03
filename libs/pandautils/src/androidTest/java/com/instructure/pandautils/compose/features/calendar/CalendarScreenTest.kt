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
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarAction
import com.instructure.pandautils.features.calendar.CalendarScreenUiState
import com.instructure.pandautils.features.calendar.CalendarUiState
import com.instructure.pandautils.features.calendar.composables.CalendarScreen
import com.jakewharton.threetenabp.AndroidThreeTen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var stateMapper: CalendarStateMapper

    @Test
    fun assertToolbarWithoutTodayButton() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        LocalDate.now(), true, stateMapper.createHeaderUiState(
                            LocalDate.now(), null
                        ), stateMapper.createBodyUiState(true, LocalDate.now())
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Calendar")))
            .assertIsDisplayed()
        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Open navigation drawer")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val todayButton = composeTestRule.onNode(hasAnyAncestor(hasTestTag("toolbar")).and(hasContentDescription("Jump to Today", true)))
        todayButton.assertIsNotDisplayed()
    }

    @Test
    fun assertToolbarWithTodayButton() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Calendar")))
            .assertIsDisplayed()
        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Open navigation drawer")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
        val todayButton = composeTestRule.onNode(hasAnyAncestor(hasTestTag("toolbar")).and(hasContentDescription("Jump to Today", true)))
        todayButton.assertIsDisplayed()
    }

    @Test
    fun todayButtonClickShouldSendTodayTappedAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = { actions.add(it) },
                navigationActionClick = {},
            )
        }

        val todayButton = composeTestRule.onNode(hasAnyAncestor(hasTestTag("toolbar")).and(hasContentDescription("Jump to Today", true)))
        todayButton.assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        assertEquals(CalendarAction.TodayTapped, actions.last())
    }

    @Test
    fun assertFabInClosedState() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.assertIsDisplayed()
        val addToDoItem = composeTestRule.onNode((hasText("Add To Do")))
        addToDoItem.assertIsNotDisplayed()
        val addEventItem = composeTestRule.onNode((hasText("Add Event")))
        addEventItem.assertIsNotDisplayed()
    }

    @Test
    fun assertFabInOpenState() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.assertIsDisplayed()
        fab.performClick()
        composeTestRule.waitForIdle()
        val addToDoItem = composeTestRule.onNode((hasText("Add To Do")))
        addToDoItem.assertIsDisplayed()
        val addEventItem = composeTestRule.onNode((hasText("Add Event")))
        addEventItem.assertIsDisplayed()
    }

    @Test
    fun clickingAddEventTriggersAddEventAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = { actions.add(it) },
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.performClick()
        composeTestRule.waitForIdle()
        val addEventItem = composeTestRule.onNode((hasText("Add Event")))
        addEventItem.performClick()
        composeTestRule.waitForIdle()

        assertEquals(CalendarAction.AddEventTapped, actions.last())
    }

    @Test
    fun clickingAddToDoTriggersAddTodoAction() {
        val actions = mutableListOf<CalendarAction>()

        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    )
                ),
                false,
                showToolbar = true,
                actionHandler = { actions.add(it) },
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.performClick()
        composeTestRule.waitForIdle()
        val addToDo = composeTestRule.onNode((hasText("Add To Do")))
        addToDo.performClick()
        composeTestRule.waitForIdle()

        assertEquals(CalendarAction.AddToDoTapped, actions.last())
    }

    @Test
    fun assertSnackbarText() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    ), snackbarMessage = "Snackbar message"
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val snackbarText = composeTestRule.onNode(hasText("Snackbar message").and(hasAnyAncestor(hasTestTag("snackbarHost"))))
        snackbarText.assertIsDisplayed()
    }

    @Test
    fun addEventButtonIsHiddenWhenShowAddEventButtonIsFalse() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    ),
                    showAddEventButton = false
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.performClick()
        composeTestRule.waitForIdle()
        
        val addToDoItem = composeTestRule.onNode((hasText("Add To Do")))
        addToDoItem.assertIsDisplayed()
        
        val addEventItem = composeTestRule.onNode((hasText("Add Event")))
        addEventItem.assertIsNotDisplayed()
    }

    @Test
    fun addEventButtonIsShownWhenShowAddEventButtonIsTrue() {
        composeTestRule.setContent {
            AndroidThreeTen.init(LocalContext.current)
            stateMapper = CalendarStateMapper(Clock.systemDefaultZone())
            val selectedDate = LocalDate.now().plusDays(1)
            CalendarScreen(
                title = "Calendar",
                calendarScreenUiState = CalendarScreenUiState(
                    calendarUiState = CalendarUiState(
                        selectedDate, true, stateMapper.createHeaderUiState(
                            selectedDate, null
                        ), stateMapper.createBodyUiState(true, selectedDate)
                    ),
                    showAddEventButton = true
                ),
                false,
                showToolbar = true,
                actionHandler = {},
                navigationActionClick = {},
            )
        }

        val fab = composeTestRule.onNode(hasContentDescription("Add new calendar item"))
        fab.performClick()
        composeTestRule.waitForIdle()
        
        val addToDoItem = composeTestRule.onNode((hasText("Add To Do")))
        addToDoItem.assertIsDisplayed()
        
        val addEventItem = composeTestRule.onNode((hasText("Add Event")))
        addEventItem.assertIsDisplayed()
    }
}