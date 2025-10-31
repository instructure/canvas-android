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

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("We weren't able to load this content.\nPlease try again.", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Refresh", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assert(refreshCalled) { "Refresh callback should be called when retry button is clicked" }
    }

    @Test
    fun testSuccessStateDisplaysHoursAndTitleWithMultipleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                    CourseOption(id = 2L, name = "Science 201")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("hours", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("total", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysMinutesAndTitleWithMultipleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                minutes = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                    CourseOption(id = 2L, name = "Science 201")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("minutes", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("total", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysHoursAndMinutesAndTitleWithMultipleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 8,
                minutes = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                    CourseOption(id = 2L, name = "Science 201")
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("8", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("hrs", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("mins", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("total", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysHoursAndTitleWithSingleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("hours", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysMinutesAndTitleWithSingleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                minutes = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("minutes", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateDisplaysHoursAndMinutesAndTitleWithSingleCourse() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 8,
                minutes = 12,
                courses = listOf(
                    CourseOption(id = 1L, name = "Math 101"),
                )
            )
        )

        composeTestRule.setContent {
            DashboardTimeSpentSection(state)
        }

        composeTestRule.onNodeWithText("Time learning").assertIsDisplayed()

        composeTestRule.onNodeWithText("8", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("12", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("hrs", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithText("mins", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSuccessStateWithZeroHoursZeroMinutesDisplaysEmpty() {
        val state = DashboardTimeSpentUiState(
            state = DashboardItemState.SUCCESS,
            cardState = DashboardTimeSpentCardState(
                hours = 0,
                minutes = 0,
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
}
