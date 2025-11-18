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

package com.instructure.pandautils.compose.features.grades

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.composetest.hasDrawable
import com.instructure.espresso.assertTextColor
import com.instructure.pandares.R
import com.instructure.pandautils.features.grades.AssignmentItem
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.DisplayGrade
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class GradesAssignmentItemTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun assertNotSubmittedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textDark)
            AssignmentItem(
                uiState = getUiState(),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNodeWithText("Assignment")
            .assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_assignment))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No due date")
            .assertIsDisplayed()
        composeTestRule.onNode(hasDrawable(R.drawable.ic_unpublish), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Not Submitted", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
        composeTestRule.onNodeWithText("-/15", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Content description")
            .assertTextColor(Color(android.graphics.Color.RED))
    }

    @Test
    fun assertSubmittedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textSuccess)
            AssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Submitted
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_complete), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Submitted", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertMissingAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textDanger)
            AssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Missing
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_unpublish), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Missing", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertLateAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textWarning)
            AssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Late
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_clock), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Late", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertGradedAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textSuccess)
            AssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Graded
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_complete_solid), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Graded", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    @Test
    fun assertCustomStatusAssignment() {
        var labelColor = Color(0)

        composeTestRule.setContent {
            labelColor = colorResource(id = R.color.textInfo)
            AssignmentItem(
                uiState = getUiState().copy(
                    submissionStateLabel = SubmissionStateLabel.Custom(R.drawable.ic_flag, R.color.textInfo, "Custom Status")
                ),
                actionHandler = {},
                contextColor = android.graphics.Color.RED
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_flag), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Status", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(labelColor)
    }

    private fun getUiState() = AssignmentUiState(
        id = 1,
        iconRes = R.drawable.ic_assignment,
        name = "Assignment",
        dueDate = "No due date",
        submissionStateLabel = SubmissionStateLabel.NotSubmitted,
        displayGrade = DisplayGrade("-/15", "Content description")
    )
}
