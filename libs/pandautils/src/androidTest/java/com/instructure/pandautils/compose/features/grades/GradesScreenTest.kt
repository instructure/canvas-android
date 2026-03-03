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

import android.graphics.Color
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
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.composetest.hasDrawable
import com.instructure.pandares.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.features.grades.AppBarUiState
import com.instructure.pandautils.features.grades.AssignmentGroupUiState
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesUiState
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
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
                actionHandler = {},
                canvasContextColor = Color.RED
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
                actionHandler = {},
                canvasContextColor = Color.RED
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
                actionHandler = {},
                canvasContextColor = Color.RED
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
                                    displayGrade = DisplayGrade("14/15", ""),
                                    score = 14.0,
                                    maxScore = 15.0,
                                    whatIfScore = null
                                ),
                                AssignmentUiState(
                                    id = 2,
                                    iconRes = R.drawable.ic_quiz,
                                    name = "Assignment 2",
                                    dueDate = "Due date",
                                    submissionStateLabel = SubmissionStateLabel.Submitted,
                                    displayGrade = DisplayGrade("-/10", ""),
                                    score = null,
                                    maxScore = 10.0,
                                    whatIfScore = null
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
                                    displayGrade = DisplayGrade("", ""),
                                    score = null,
                                    maxScore = null,
                                    whatIfScore = null
                                )
                            ),
                            expanded = false
                        )
                    ),
                    gradeText = "87% A"
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
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
                                    score = 7.0,
                                    maxScore = 15.0,
                                    whatIfScore = null,
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
                actionHandler = {},
                canvasContextColor = Color.RED
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
                actionHandler = {},
                canvasContextColor = Color.RED
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
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        val snackbarText = composeTestRule.onNode(hasText("Snackbar message").and(hasAnyAncestor(hasTestTag("snackbarHost"))))
        snackbarText.assertIsDisplayed()
    }

    @Test
    fun assertGradingPeriodLabelDisplayed() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    gradePreferencesUiState = GradePreferencesUiState(
                        gradingPeriods = listOf(
                            GradingPeriod(id = 1L, title = "Spring 2024")
                        )
                    )
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithTag("gradingPeriodLabel")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Grading Period")
            .assertIsDisplayed()
    }

    @Test
    fun assertAllGradingPeriodsDisplayedWhenNoGradingPeriodSelected() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    gradePreferencesUiState = GradePreferencesUiState(
                        selectedGradingPeriod = null,
                        gradingPeriods = listOf(
                            GradingPeriod(id = 1L, title = "Spring 2024")
                        )
                    )
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithTag("gradingPeriodName")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("All Grading Periods")
            .assertIsDisplayed()
    }

    @Test
    fun assertSelectedGradingPeriodNameDisplayed() {
        val gradingPeriod = GradingPeriod(
            id = 1L,
            title = "Spring 2024"
        )

        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    gradePreferencesUiState = GradePreferencesUiState(
                        selectedGradingPeriod = gradingPeriod,
                        gradingPeriods = listOf(gradingPeriod)
                    )
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithTag("gradingPeriodName")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Spring 2024")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("All Grading Periods")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertGradingPeriodHiddenWhenNoGradingPeriods() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    gradePreferencesUiState = GradePreferencesUiState(
                        gradingPeriods = emptyList()
                    )
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithTag("gradingPeriodLabel")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("gradingPeriodName")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertGradingPeriodDisplayedWithMultipleGradingPeriods() {
        val selectedPeriod = GradingPeriod(
            id = 2L,
            title = "Q2 2024"
        )

        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = listOf(
                        AssignmentGroupUiState(
                            id = 1,
                            name = "Assignments",
                            assignments = listOf(
                                AssignmentUiState(
                                    id = 1,
                                    iconRes = R.drawable.ic_assignment,
                                    name = "Test Assignment",
                                    dueDate = "Due date",
                                    submissionStateLabel = SubmissionStateLabel.Graded,
                                    displayGrade = DisplayGrade("10/10", ""),
                                    score = 10.0,
                                    maxScore = 10.0,
                                    whatIfScore = null
                                )
                            ),
                            expanded = true
                        )
                    ),
                    gradeText = "100% A",
                    gradePreferencesUiState = GradePreferencesUiState(
                        selectedGradingPeriod = selectedPeriod,
                        gradingPeriods = listOf(
                            GradingPeriod(id = 1L, title = "Q1 2024"),
                            selectedPeriod,
                            GradingPeriod(id = 3L, title = "Q3 2024")
                        )
                    )
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithTag("gradingPeriodLabel")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("gradingPeriodName")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Q2 2024")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Q1 2024")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText("Q3 2024")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertSearchFieldDisplayedWhenExpanded() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    isSearchExpanded = true
                ),
                actionHandler = {},
                canvasContextColor = Color.RED,
                appBarUiState = AppBarUiState(
                    title = "Grades",
                    subtitle = "Course Name",
                    navigationActionClick = {},
                    bookmarkable = false,
                    addBookmarkClick = {}
                )
            )
        }

        composeTestRule.onNodeWithTag("searchField")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Search")
            .assertIsDisplayed()
    }

    @Test
    fun assertSearchFieldNotDisplayedWhenCollapsed() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    isSearchExpanded = false
                ),
                actionHandler = {},
                canvasContextColor = Color.RED,
                appBarUiState = AppBarUiState(
                    title = "Grades",
                    subtitle = "Course Name",
                    navigationActionClick = {},
                    bookmarkable = false,
                    addBookmarkClick = {}
                )
            )
        }

        composeTestRule.onNodeWithTag("searchField")
            .assertIsNotDisplayed()
    }

    @Test
    fun assertSearchClearButtonDisplayedWhenQueryNotEmpty() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    isSearchExpanded = true,
                    searchQuery = "assignment"
                ),
                actionHandler = {},
                canvasContextColor = Color.RED,
                appBarUiState = AppBarUiState(
                    title = "Grades",
                    subtitle = "Course Name",
                    navigationActionClick = {},
                    bookmarkable = false,
                    addBookmarkClick = {}
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Clear query")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertSearchCloseButtonDisplayed() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    isSearchExpanded = true
                ),
                actionHandler = {},
                canvasContextColor = Color.RED,
                appBarUiState = AppBarUiState(
                    title = "Grades",
                    subtitle = "Course Name",
                    navigationActionClick = {},
                    bookmarkable = false,
                    addBookmarkClick = {}
                )
            )
        }

        composeTestRule.onNodeWithContentDescription("Close search bar")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertEmptySearchResultsDisplayed() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = emptyList(),
                    gradeText = "87% A",
                    searchQuery = "test assignment"
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithText("No Matching Assignments")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("No assignments match your search. Try a different search term.")
            .assertIsDisplayed()
    }

    @Test
    fun assertGradesCardHiddenWhenSearchExpanded() {
        composeTestRule.setContent {
            GradesScreen(
                uiState = GradesUiState(
                    isLoading = false,
                    items = listOf(
                        AssignmentGroupUiState(
                            id = 1,
                            name = "Assignments",
                            assignments = listOf(
                                AssignmentUiState(
                                    id = 1,
                                    iconRes = R.drawable.ic_assignment,
                                    name = "Test Assignment",
                                    dueDate = "Due date",
                                    submissionStateLabel = SubmissionStateLabel.Graded,
                                    displayGrade = DisplayGrade("10/10", ""),
                                    score = 10.0,
                                    maxScore = 10.0,
                                    whatIfScore = null
                                )
                            ),
                            expanded = true
                        )
                    ),
                    gradeText = "87% A",
                    isSearchExpanded = true
                ),
                actionHandler = {},
                canvasContextColor = Color.RED
            )
        }

        composeTestRule.onNodeWithText("Total")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("searchField")
            .assertIsDisplayed()
    }
}
