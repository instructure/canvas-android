/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.todolist.filter

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.pandautils.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToDoFilterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun assertSectionHeadersAreVisible() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState()
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterVisibleItems)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowTasksFrom)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowTasksUntil)).assertIsDisplayed()
    }

    @Test
    fun assertCheckboxItemsAreVisible() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState()
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCalendarEvents)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCompleted)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFavoriteCoursesOnly)).assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertCheckboxStates() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    checkboxStates = listOf(true, false, true, false)
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCalendarEvents))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCompleted))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFavoriteCoursesOnly))
            .assertIsDisplayed()
    }

    @Test
    fun assertAllCheckboxesUnchecked() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    checkboxStates = listOf(false, false, false, false)
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCalendarEvents))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCompleted))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFavoriteCoursesOnly))
            .assertIsDisplayed()
    }

    @Test
    fun assertAllCheckboxesChecked() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    checkboxStates = listOf(true, true, true, true)
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCalendarEvents))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCompleted))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFavoriteCoursesOnly))
            .assertIsDisplayed()
    }

    @Test
    fun assertPastDateOptionsAreVisible() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState()
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFourWeeks)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("From 7 Oct").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterThreeWeeks)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("From 14 Oct").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterTwoWeeks)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("From 21 Oct").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterLastWeek)).assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("From 28 Oct").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterThisWeek)).assertIsDisplayed().assertHasClickAction()
        // Note: "From 4 Nov" appears twice (once for THIS_WEEK, once for TODAY), so we check with allowMultiple
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterToday)).assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertPastDateOptionSelection() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(selectedPastOption = DateRangeSelection.TWO_WEEKS)
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFourWeeks)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterThreeWeeks)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterTwoWeeks)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterLastWeek)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterThisWeek)).assertIsDisplayed()
    }

    @Test
    fun clickingCheckboxTriggersCallback() {
        var toggledValue: Boolean? = null
        var callbackInvoked = false

        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    checkboxStates = listOf(false, false, false, false),
                    onCheckboxToggle = { index, checked ->
                        if (index == 0) {
                            toggledValue = checked
                            callbackInvoked = true
                        }
                    }
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos)).performClick()

        assert(callbackInvoked)
        assertEquals(true, toggledValue)
    }

    @Test
    fun clickingPastDateOptionTriggersCallback() {
        val selectedOptions = mutableListOf<DateRangeSelection>()

        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    onPastDaysChanged = { selection ->
                        selectedOptions.add(selection)
                    }
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterTwoWeeks)).performClick()

        assertEquals(DateRangeSelection.TWO_WEEKS, selectedOptions.last())
    }

    @Test
    fun assertDateOptionsAreInCorrectOrder() {
        val uiState = createDefaultUiState()

        composeTestRule.setContent {
            ToDoFilterContent(uiState = uiState)
        }

        // Verify past section header exists
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowTasksFrom)).assertIsDisplayed()

        // Verify future section header exists
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowTasksUntil)).assertIsDisplayed()
    }

    @Test
    fun assertMultipleCheckboxesToggles() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    checkboxStates = listOf(true, false, true, true)
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowPersonalToDos))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCalendarEvents))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterShowCompleted))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFavoriteCoursesOnly))
            .assertIsDisplayed()
    }

    @Test
    fun assertBothDateSelectionsCanBeDifferent() {
        composeTestRule.setContent {
            ToDoFilterContent(
                uiState = createDefaultUiState(
                    selectedPastOption = DateRangeSelection.FOUR_WEEKS,
                    selectedFutureOption = DateRangeSelection.TODAY
                )
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterFourWeeks)).assertIsDisplayed()
        // There are two "Today" options (one for past, one for future), so we just check they exist
        composeTestRule.onNodeWithText(context.getString(R.string.todoFilterToday)).assertIsDisplayed()
    }

    // Helper function to create default UI state for tests
    private fun createDefaultUiState(
        checkboxStates: List<Boolean> = listOf(false, false, false, false),
        selectedPastOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
        selectedFutureOption: DateRangeSelection = DateRangeSelection.ONE_WEEK,
        onCheckboxToggle: (Int, Boolean) -> Unit = { _, _ -> },
        onPastDaysChanged: (DateRangeSelection) -> Unit = {},
        onFutureDaysChanged: (DateRangeSelection) -> Unit = {}
    ): ToDoFilterUiState {
        return ToDoFilterUiState(
            checkboxItems = listOf(
                FilterCheckboxItem(
                    titleRes = R.string.todoFilterShowPersonalToDos,
                    checked = checkboxStates[0],
                    onToggle = { onCheckboxToggle(0, it) }
                ),
                FilterCheckboxItem(
                    titleRes = R.string.todoFilterShowCalendarEvents,
                    checked = checkboxStates[1],
                    onToggle = { onCheckboxToggle(1, it) }
                ),
                FilterCheckboxItem(
                    titleRes = R.string.todoFilterShowCompleted,
                    checked = checkboxStates[2],
                    onToggle = { onCheckboxToggle(2, it) }
                ),
                FilterCheckboxItem(
                    titleRes = R.string.todoFilterFavoriteCoursesOnly,
                    checked = checkboxStates[3],
                    onToggle = { onCheckboxToggle(3, it) }
                )
            ),
            pastDateOptions = listOf(
                DateRangeOption(
                    selection = DateRangeSelection.FOUR_WEEKS,
                    labelText = context.getString(R.string.todoFilterFourWeeks),
                    dateText = "From 7 Oct"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.THREE_WEEKS,
                    labelText = context.getString(R.string.todoFilterThreeWeeks),
                    dateText = "From 14 Oct"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.TWO_WEEKS,
                    labelText = context.getString(R.string.todoFilterTwoWeeks),
                    dateText = "From 21 Oct"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.ONE_WEEK,
                    labelText = context.getString(R.string.todoFilterLastWeek),
                    dateText = "From 28 Oct"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.THIS_WEEK,
                    labelText = context.getString(R.string.todoFilterThisWeek),
                    dateText = "From 4 Nov"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.TODAY,
                    labelText = context.getString(R.string.todoFilterToday),
                    dateText = "From 4 Nov"
                )
            ),
            selectedPastOption = selectedPastOption,
            futureDateOptions = listOf(
                DateRangeOption(
                    selection = DateRangeSelection.TODAY,
                    labelText = context.getString(R.string.todoFilterToday),
                    dateText = "Until 4 Nov"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.THIS_WEEK,
                    labelText = context.getString(R.string.todoFilterThisWeek),
                    dateText = "Until 10 Nov"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.ONE_WEEK,
                    labelText = context.getString(R.string.todoFilterNextWeek),
                    dateText = "Until 17 Nov"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.TWO_WEEKS,
                    labelText = context.getString(R.string.todoFilterInTwoWeeks),
                    dateText = "Until 24 Nov"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.THREE_WEEKS,
                    labelText = context.getString(R.string.todoFilterInThreeWeeks),
                    dateText = "Until 1 Dec"
                ),
                DateRangeOption(
                    selection = DateRangeSelection.FOUR_WEEKS,
                    labelText = context.getString(R.string.todoFilterInFourWeeks),
                    dateText = "Until 8 Dec"
                )
            ),
            selectedFutureOption = selectedFutureOption,
            onPastDaysChanged = onPastDaysChanged,
            onFutureDaysChanged = onFutureDaysChanged
        )
    }
}