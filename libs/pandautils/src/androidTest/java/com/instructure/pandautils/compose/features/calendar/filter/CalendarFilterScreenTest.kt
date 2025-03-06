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
package com.instructure.pandautils.compose.features.calendar.filter

import android.graphics.Color
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.calendar.filter.CalendarFilterAction
import com.instructure.pandautils.features.calendar.filter.CalendarFilterItemUiState
import com.instructure.pandautils.features.calendar.filter.CalendarFilterScreenUiState
import com.instructure.pandautils.features.calendar.filter.composables.CalendarFiltersScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
class CalendarFilterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertToolbarTitleAndIcon() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(loading = true), actionHandler = {}) {}
        }

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasText("Calendars"))
        ).assertIsDisplayed()
        val backButton = composeTestRule.onNode(
            hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close"))
        )
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(loading = true), actionHandler = {}) {}
        }

        val loading = composeTestRule.onNode(hasTestTag("loading"))
        loading.assertIsDisplayed()
    }

    @Test
    fun assertError() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(error = true), actionHandler = {}) {}
        }

        val errorText = composeTestRule.onNodeWithText("Failed to load filters")
        errorText.assertIsDisplayed()
        val retryButton = composeTestRule.onNodeWithText("Retry")
        retryButton.assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertItemsAndHeadersAreVisible() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", true, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", true, Color.BLUE)
                )
            ), actionHandler = {}) {}
        }

        composeTestRule.onNodeWithContentDescription("User 1").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("User 1").assertIsOff()

        val courseHeader = composeTestRule.onNodeWithText("Courses")
        courseHeader.assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Course 1").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Course 1").assertIsOn()
        composeTestRule.onNodeWithContentDescription("Course 2").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Course 2").assertIsOff()

        val groupHeader = composeTestRule.onNodeWithText("Groups")
        groupHeader.assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Group 1").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Group 1").assertIsOff()
        composeTestRule.onNodeWithContentDescription("Group 2").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithContentDescription("Group 2").assertIsOn()
    }

    @Test
    fun assertHeaderIsNotVisibleIfThereIsNoItemsInGroup() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", true, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = emptyList()
            ), actionHandler = {}) {}
        }

        val courseHeader = composeTestRule.onNodeWithText("Courses")
        courseHeader.assertIsDisplayed()

        val groupHeader = composeTestRule.onNodeWithText("Groups")
        groupHeader.assertDoesNotExist()
    }

    @Test
    fun assertDeselectAllIsVisibleIfAnythingIsSelected() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", true, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", true, Color.BLUE)
                )
            ), actionHandler = {}) {}
        }

        composeTestRule.onNode(hasText("Deselect all").and(hasAnyAncestor(hasTestTag("toolbar"))))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertSelectAllIsVisibleIfNothingIsSelected() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", false, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", false, Color.BLUE)
                ), selectAllAvailable = true
            ), actionHandler = {}) {}
        }

        composeTestRule.onNode(hasText("Select all").and(hasAnyAncestor(hasTestTag("toolbar"))))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertSelectAllIsNotVisibleIfNothingIsSelectedButItsNotAvailable() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", false, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", false, Color.BLUE)
                ), selectAllAvailable = false
            ), actionHandler = {}) {}
        }

        composeTestRule.onNode(hasText("Select all").and(hasAnyAncestor(hasTestTag("toolbar"))))
            .assertDoesNotExist()
    }

    @Test
    fun assertExplanationMessage() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ), explanationMessage = "Explanation message"
            ), actionHandler = {}) {}
        }

        composeTestRule.onNodeWithText("Explanation message").assertIsDisplayed()
    }

    @Test
    fun assertSnackbarMessage() {
        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ), snackbarMessage = "Snackbar message"
            ), actionHandler = {}) {}
        }

        composeTestRule.onNodeWithText("Snackbar message").assertIsDisplayed()
    }

    @Test
    fun clickingOnFilterItemSendsToggleFilterEvent() {
        val actions = mutableListOf<CalendarFilterAction>()

        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", false, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", false, Color.BLUE)
                ), selectAllAvailable = false
            ), actionHandler = { actions.add(it) }) {}
        }

        composeTestRule.onNodeWithContentDescription("Course 1").performClick()

        assertEquals(CalendarFilterAction.ToggleFilter("course_1"), actions.last())
    }

    @Test
    fun clickingOnSelectAllSendsSelectAllAction() {
        val actions = mutableListOf<CalendarFilterAction>()

        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", false, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", false, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", false, Color.BLUE)
                ), selectAllAvailable = true
            ), actionHandler = { actions.add(it) }) {}
        }

        composeTestRule.onNode(hasText("Select all").and(hasAnyAncestor(hasTestTag("toolbar"))))
            .performClick()

        assertEquals(CalendarFilterAction.SelectAll, actions.last())
    }

    @Test
    fun clickingOnDeselectAllSendsSelectAllAction() {
        val actions = mutableListOf<CalendarFilterAction>()

        composeTestRule.setContent {
            CalendarFiltersScreen(uiState = CalendarFilterScreenUiState(
                users = listOf(
                    CalendarFilterItemUiState("user_1", "User 1", false, Color.BLUE),
                ),
                courses = listOf(
                    CalendarFilterItemUiState("course_1", "Course 1", true, Color.BLUE),
                    CalendarFilterItemUiState("course_2", "Course 2", false, Color.BLUE)
                ),
                groups = listOf(
                    CalendarFilterItemUiState("group_1", "Group 1", true, Color.BLUE),
                    CalendarFilterItemUiState("group_2", "Group 2", false, Color.BLUE)
                ), selectAllAvailable = true
            ), actionHandler = { actions.add(it) }) {}
        }

        composeTestRule.onNode(hasText("Deselect all").and(hasAnyAncestor(hasTestTag("toolbar"))))
            .performClick()

        assertEquals(CalendarFilterAction.DeselectAll, actions.last())
    }
}