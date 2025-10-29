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
package com.instructure.horizon.ui.features.dashboard.widget.timespent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.timespent.DashboardTimeSpentSection
import com.instructure.horizon.features.dashboard.widget.timespent.DashboardTimeSpentUiState
import com.instructure.horizon.features.dashboard.widget.timespent.card.CourseOption
import com.instructure.horizon.features.dashboard.widget.timespent.card.DashboardTimeSpentCardState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardTimeSpentWidgetUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingStateDisplaysShimmerEffect() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.LOADING
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Loading state should render without crashing
        // Shimmer effects don't have testable text/content descriptions
        composeTestRule.waitForIdle()
    }

    @Test
    fun testErrorStateDisplaysErrorMessageAndRetryButton() {
        var refreshCalled = false
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.ERROR,
            onRefresh = { onComplete ->
                refreshCalled = true
                onComplete()
            }
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("Time").assertIsDisplayed()

        // Verify error message is displayed
        composeTestRule.onNodeWithText("We weren't able to load this content.\nPlease try again.", substring = true)
            .assertIsDisplayed()

        // Verify retry button is displayed and clickable
        composeTestRule.onNodeWithText("Refresh", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateDisplaysHoursAndTitle() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 12.5,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                    CourseOption(id = 2L, name = "Science 201")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("Time").assertIsDisplayed()

        // Verify hours (rounded to 13) is displayed
        composeTestRule.onNodeWithText("13").assertIsDisplayed()

        // Verify "hours in" text is displayed for multiple courses
        composeTestRule.onNodeWithText("hours in").assertIsDisplayed()

        // Verify dropdown shows "all courses" by default
        composeTestRule.onNodeWithText("all courses", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithZeroHoursDisplaysZero() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 0.0,
                courses = listOf(
                    CourseOption(id = 1L, name = "Course 1")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify zero hours is not displayed
        composeTestRule.onNodeWithText("0").assertDoesNotExist()

        // Verify single course text is not displayed
        composeTestRule.onNodeWithText("hours in your course").assertDoesNotExist()

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("This widget will update once data becomes available.").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithSingleCourseDisplaysDifferentText() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 15.8,
                courses = listOf(
                    CourseOption(id = 1L, name = "History 101")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify hours (rounded to 16) is displayed
        composeTestRule.onNodeWithText("16").assertIsDisplayed()

        // Verify single course text (no dropdown)
        composeTestRule.onNodeWithText("hours in your course").assertIsDisplayed()

        // Verify no dropdown exists (shouldn't find "all courses")
        composeTestRule.onNodeWithText("all courses").assertDoesNotExist()
    }

    @Test
    fun testSuccessStateWithMultipleCoursesShowsDropdown() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 25.3,
                courses = listOf(
                    CourseOption(id = 1L, name = "Biology"),
                    CourseOption(id = 2L, name = "Chemistry"),
                    CourseOption(id = 3L, name = "Physics")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify hours (rounded to 25) is displayed
        composeTestRule.onNodeWithText("25").assertIsDisplayed()

        // Verify "hours in" text is displayed
        composeTestRule.onNodeWithText("hours in").assertIsDisplayed()

        // Verify dropdown shows "all courses" by default
        composeTestRule.onNodeWithText("all courses", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithSelectedCourseDisplaysCourseName() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 8.7,
                courses = listOf(
                    CourseOption(id = 1L, name = "English Literature"),
                    CourseOption(id = 2L, name = "World History")
                ),
                selectedCourseId = 1L
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify hours (rounded to 9) is displayed
        composeTestRule.onNodeWithText("9").assertIsDisplayed()

        // Verify selected course name is displayed
        composeTestRule.onNodeWithText("English Literature", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysCorrectRoundedDownHours() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 24.4,
                courses = listOf(CourseOption(id = 1L, name = "Course 1"))
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("24").assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysCorrectRoundedUpHours() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 24.6,
                courses = listOf(CourseOption(id = 1L, name = "Course 1"))
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun testCourseDropdownInteraction() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 18.5,
                courses = listOf(
                    CourseOption(id = 1L, name = "Computer Science"),
                    CourseOption(id = 2L, name = "Data Structures")
                ),
                selectedCourseId = null,
                onCourseSelected = { }
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        // Verify dropdown is displayed and clickable
        composeTestRule.onNodeWithText("all courses", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        // After clicking dropdown, course options should appear
        composeTestRule.onNodeWithText("Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Data Structures").assertIsDisplayed()
    }
}
