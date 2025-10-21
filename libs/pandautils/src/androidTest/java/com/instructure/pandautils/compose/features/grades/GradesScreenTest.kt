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

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.composetest.hasDrawable
import com.instructure.pandares.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.features.grades.AssignmentGroupUiState
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesUiState
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.DisplayGrade
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class GradesScreenTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun assertLoadingContent() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    isError = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("We're having trouble loading your student's grades. Please try reloading the page or check back later.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertEmptyContent() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList()
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("No Assignments")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("It looks like assignments haven't been created in this space yet.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(R.drawable.ic_panda_space.toString())
            .assertIsDisplayed()
    }

    @Test
    fun assertGradesContent() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = listOf(
                        AssignmentGroupUiState(
                            id = 1,
                            name = "Group 1",
                            assignments = listOf(
                                AssignmentUiState(
                                    id = 1,
                                    iconRes = R.drawable.ic_assignment,
                                    name = "Assignment 1",
                                    dueDate = "No due date",
                                    submissionStateLabel = SubmissionStateLabel.Graded,
                                    displayGrade = DisplayGrade("14/15", "")
                                ),
                                AssignmentUiState(
                                    id = 2,
                                    iconRes = R.drawable.ic_quiz,
                                    name = "Assignment 2",
                                    dueDate = "Due date",
                                    submissionStateLabel = SubmissionStateLabel.Submitted,
                                    displayGrade = DisplayGrade("-/10", "")
                                )
                            ),
                            expanded = true
                        ),
                        AssignmentGroupUiState(
                            id = 1,
                            name = "Group 2",
                            assignments = listOf(
                                AssignmentUiState(
                                    id = 3,
                                    iconRes = R.drawable.ic_assignment,
                                    name = "Assignment 3",
                                    dueDate = "Due date",
                                    submissionStateLabel = SubmissionStateLabel.Late,
                                    displayGrade = DisplayGrade("", "")
                                )
                            ),
                            expanded = false
                        )
                    ),
                    gradeText = "87% A"
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("Total")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("87% A")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Filter")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Based on graded assignments")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Group 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No due date")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Graded")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("14/15")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment 2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Due date")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Submitted")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("-/10")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Group 2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment 3")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertDiscussionCheckpointsContent() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = listOf(
                        AssignmentGroupUiState(
                            id = 1,
                            name = "Group 1",
                            assignments = listOf(
                                AssignmentUiState(
                                    id = 1,
                                    iconRes = R.drawable.ic_discussion,
                                    name = "Assignment 1",
                                    dueDate = "No due date",
                                    submissionStateLabel = SubmissionStateLabel.Graded,
                                    displayGrade = DisplayGrade("7/15", ""),
                                    checkpoints = listOf(
                                        DiscussionCheckpointUiState(
                                            name = "Checkpoint 1",
                                            dueDate = "Due date 1",
                                            submissionStateLabel = SubmissionStateLabel.Graded,
                                            displayGrade = DisplayGrade("7/10", ""),
                                            pointsPossible = 10
                                        ),
                                        DiscussionCheckpointUiState(
                                            name = "Checkpoint 2",
                                            dueDate = "Due date 2",
                                            submissionStateLabel = SubmissionStateLabel.Missing,
                                            displayGrade = DisplayGrade("-/5", ""),
                                            pointsPossible = 5
                                        )
                                    ),
                                    checkpointsExpanded = true
                                )
                            ),
                            expanded = true
                        )
                    ),
                    gradeText = "87% A"
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("Group 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Assignment 1")
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("assignmentDueDate") and hasText("Due date 1"), true)
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("assignmentDueDate") and hasText("Due date 2"), true)
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("submissionStateLabel") and hasText("Graded"), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("7/15")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Checkpoint 1")
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointDueDate_Checkpoint 1") and hasText("Due date 1"), true)
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointSubmissionStateLabel") and hasText("Graded"), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("7/10")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Checkpoint 2")
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointDueDate_Checkpoint 2") and hasText("Due date 2"), true)
            .assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointSubmissionStateLabel") and hasText("Missing"), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("-/5")
            .assertIsDisplayed()
    }

    @Test
    fun assertLockedGrade() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    gradeText = "87% A",
                    isGradeLocked = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNode(hasDrawable(R.drawable.ic_lock_lined), true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("87% A")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertSnackbarText() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    gradeText = "87% A",
                    isGradeLocked = true,
                    snackbarMessage = "Snackbar message"
                ),
                actionHandler = {}
            )
        }

        val snackbarText = composeTestRule.onNode(hasText("Snackbar message").and(hasAnyAncestor(hasTestTag("snackbarHost"))))
        snackbarText.assertIsDisplayed()
    }
}
