/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.assignmentdetails

import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.instui.compose.InstUITheme
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsScreenState
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsUiState
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.utils.ThemedColor
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import androidx.compose.ui.graphics.Color as ComposeColor

@RunWith(AndroidJUnit4::class)
class NGCAssignmentDetailsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val courseColor = ComposeColor(0xFF2573DF)

    private fun setContent(
        uiState: AssignmentDetailsUiState,
        rubric: NgcRubricUiState? = null,
        onRubricClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            InstUITheme(courseColor = courseColor) {
                NGCAssignmentDetailsContent(
                    courseName = "Biology 101",
                    uiState = uiState,
                    onNavigateBack = {},
                    onSubmitClick = {},
                    webViewCallbacks = ComposeWebViewCallbacks(),
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(),
                    rubric = rubric,
                    onRubricClick = onRubricClick,
                )
            }
        }
    }

    // Base Content-state ui state with no WebView (noDescriptionVisible) so the screen renders
    // without inflating a WebView during tests.
    private fun contentState(
        assignmentName: String = "Essay 1",
        points: String = "10 pts",
        submitVisible: Boolean = true,
        submitEnabled: Boolean = true,
        hasRemainingAttempts: Boolean = true,
        submitButtonText: String = "Submit Assignment",
        isQuiz: Boolean = false,
        attempts: List<AssignmentDetailsAttemptViewData> = emptyList(),
        gradeCell: GradeCellViewData? = null,
    ) = AssignmentDetailsUiState(
        screenState = AssignmentDetailsScreenState.Content,
        assignmentName = assignmentName,
        submissionTypes = "Text Entry",
        attemptsText = "Unlimited attempts",
        points = points,
        descriptionLabelText = "Instructions",
        noDescriptionVisible = true,
        submitVisible = submitVisible,
        submitEnabled = submitEnabled,
        hasRemainingAttempts = hasRemainingAttempts,
        submitButtonText = submitButtonText,
        isQuiz = isQuiz,
        attempts = attempts,
        selectedAttemptPosition = 0,
        gradeCell = gradeCell,
    )

    @Test
    fun loadingStateShowsLoadingIndicator() {
        setContent(AssignmentDetailsUiState(screenState = AssignmentDetailsScreenState.Loading))

        composeTestRule.onNodeWithTag("ngcAssignmentDetails_loading").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsMessageAndRetry() {
        setContent(AssignmentDetailsUiState(screenState = AssignmentDetailsScreenState.Error("Something went wrong")))

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun contentShowsAssignmentNameAndSubmitButton() {
        setContent(contentState(assignmentName = "Essay 1", submitButtonText = "Submit Assignment"))

        // "Essay 1" also appears in the TopBar title, so assert the name via its (unique) tag.
        composeTestRule.onNodeWithTag("ngcAssignmentDetails_assignmentName").assertTextEquals("Essay 1")
        composeTestRule.onNodeWithTag("ngcAssignmentDetails_submitButton").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit Assignment").assertIsDisplayed()
    }

    @Test
    fun contentHidesSubmitButtonWhenNoRemainingAttempts() {
        setContent(contentState(hasRemainingAttempts = false))

        composeTestRule.onNodeWithTag("ngcAssignmentDetails_submitButton").assertDoesNotExist()
    }

    @Test
    fun contentShowsNewAttemptLabelWhenHasAttemptsAndNotQuiz() {
        setContent(
            contentState(
                isQuiz = false,
                submitButtonText = "Resubmit Assignment",
                attempts = listOf(AssignmentDetailsAttemptViewData(title = "Attempt 1", date = "Jan 1")),
            )
        )

        composeTestRule.onNodeWithText("New attempt").assertIsDisplayed()
    }

    @Test
    fun contentKeepsQuizLabelWhenQuizWithAttempts() {
        setContent(
            contentState(
                isQuiz = true,
                submitButtonText = "View Quiz",
                attempts = listOf(AssignmentDetailsAttemptViewData(title = "Attempt 1", date = "Jan 1")),
            )
        )

        composeTestRule.onNodeWithText("View Quiz").assertIsDisplayed()
        composeTestRule.onNodeWithText("New attempt").assertDoesNotExist()
    }

    @Test
    fun contentShowsGradeCardWhenGraded() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val gradeCell = GradeCellViewData.fromSubmission(
            resources,
            ThemedColor(Color.BLUE),
            Color.BLUE,
            Assignment(pointsPossible = 10.0, gradingType = Assignment.POINTS_TYPE),
            Submission(submittedAt = Date(), grade = "8", score = 8.0, workflowState = "graded"),
        )

        setContent(contentState(gradeCell = gradeCell))

        composeTestRule.onNodeWithTag("ngcAssignmentDetails_gradeCard").assertIsDisplayed()
    }

    @Test
    fun contentShowsRubricRowAndInvokesClickWhenRubricPresent() {
        var clicked = false
        val rubric = NgcRubricUiState(
            criteria = listOf(
                NgcRubricCriterion(
                    title = "Criteria 1",
                    description = "Use of space",
                    ratings = listOf(
                        NgcRubricRating(pointsLabel = "6", title = "Excellent", detail = "Great", isAssessed = true),
                    ),
                    assessedScore = "6",
                    maxScore = "6",
                    comment = null,
                ),
            ),
        )
        setContent(contentState(), rubric = rubric, onRubricClick = { clicked = true })

        composeTestRule.onNodeWithTag("ngcAssignmentDetails_rubricRow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ngcAssignmentDetails_rubricRow").performClick()
        assertTrue(clicked)
    }

    @Test
    fun contentHidesRubricRowWhenNoRubric() {
        setContent(contentState(), rubric = null)

        composeTestRule.onNodeWithTag("ngcAssignmentDetails_rubricRow").assertDoesNotExist()
    }
}
