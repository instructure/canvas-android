/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.compose.courses.details.summary

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.parentapp.features.courses.details.summary.ScreenState
import com.instructure.parentapp.features.courses.details.summary.SummaryContent
import com.instructure.parentapp.features.courses.details.summary.SummaryUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class SummaryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertLoadingContent() {
        composeTestRule.setContent {
            SummaryContent(
                uiState = SummaryUiState(
                    state = ScreenState.Loading
                ),
                onRefresh = {},
                navigateToAssignmentDetails = { _, _ ->},
                navigateToCalendarEvent = { _, _, _ ->}
            )
        }

        composeTestRule.onNodeWithTag("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            SummaryContent(
                uiState = SummaryUiState(
                    state = ScreenState.Error
                ),
                onRefresh = {},
                navigateToAssignmentDetails = { _, _ ->},
                navigateToCalendarEvent = { _, _, _ ->}
            )
        }

        composeTestRule.onNodeWithText("Failed to load summary")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertEmptyContent() {
        composeTestRule.setContent {
            SummaryContent(
                uiState = SummaryUiState(
                    state = ScreenState.Empty
                ),
                onRefresh = {},
                navigateToAssignmentDetails = { _, _ ->},
                navigateToCalendarEvent = { _, _, _ ->}
            )
        }

        composeTestRule.onNodeWithText("No summary items to display")
            .assertIsDisplayed()
    }

    @Test
    fun assertSuccessContent() {
        val assignmentDueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        composeTestRule.setContent {
            SummaryContent(
                uiState = SummaryUiState(
                    state = ScreenState.Content,
                    courseId = 1,
                    items = listOf(
                        ScheduleItem(
                            title = "Assignment 1",
                            startAt = assignmentDueDate.time.toApiString(),
                            type = "assignment"
                        ),
                        ScheduleItem(
                            title = "Calendar 1",
                            type = "event"
                        )
                    )
                ),
                onRefresh = {},
                navigateToAssignmentDetails = { _, _ ->},
                navigateToCalendarEvent = { _, _, _ ->}
            )
        }

        composeTestRule.onNodeWithText("Assignment 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(assignmentDueDate.time.toApiString().toSimpleDate()?.toFormattedString().orEmpty())
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Calendar 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No Due Date")
            .assertIsDisplayed()
    }
}