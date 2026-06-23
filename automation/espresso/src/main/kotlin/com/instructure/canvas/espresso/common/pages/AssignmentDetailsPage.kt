/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvas.espresso.common.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import com.instructure.canvas.espresso.utils.stringContainsTextCaseInsensitive
import com.instructure.canvas.espresso.utils.waitForMatcherWithSleeps
import com.instructure.canvas.espresso.utils.withResourceIdContaining
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.pandautils.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

/**
 * Page object for the (now Jetpack Compose) shared Assignment Details screen used by the Student
 * and Parent apps. Most interactions go through [composeTestRule]. A few collaborators are still
 * Android Views and are matched with Espresso: the submit-type picker dialog, the custom reminder
 * dialog, the description WebView, and the PSPDFKit viewer.
 */
open class AssignmentDetailsPage(val moduleItemInteractions: ModuleItemInteractions, val composeTestRule: ComposeTestRule) : BasePage() {

    override fun assertPageObjects(duration: Long) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("assignmentDetailsPage").assertIsDisplayed()
    }

    // The previous screen (e.g. the Assignment List) is also Compose and stays in the semantics
    // tree behind this one, so the shared "toolbar"/"overflowMenu" tags exist twice. Scope matches
    // to this screen's root to keep them unambiguous.
    private fun inToolbar(text: String) = hasText(text) and
        hasAnyAncestor(hasTestTag("toolbar")) and
        hasAnyAncestor(hasTestTag("assignmentDetailsPage"))

    fun assertDisplayToolbarTitle() {
        composeTestRule.onNode(inToolbar(getStringFromResource(R.string.assignmentDetails))).assertIsDisplayed()
    }

    fun assertDisplayToolbarTitle(toolbarTitle: String) {
        composeTestRule.onNode(inToolbar(toolbarTitle)).assertIsDisplayed()
    }

    fun assertDisplayToolbarSubtitle(courseNameText: String) {
        composeTestRule.onNode(inToolbar(courseNameText)).assertIsDisplayed()
    }

    fun assertDisplaysDate(dateText: String, position: Int = 0) {
        // Scroll the due-date row into view first — on landscape / small screens it is below the fold.
        composeTestRule.onNodeWithTag("dueDateText-$position").performScrollTo().assertTextEquals(dateText).assertIsDisplayed()
    }

    fun assertAssignmentDetails(assignment: Assignment) {
        composeTestRule.onNodeWithTag("assignmentName").assertTextEquals(assignment.name!!)
        composeTestRule.onNodeWithTag("points").assertTextContains(assignment.pointsPossible.toInt().toString(), substring = true, ignoreCase = true)
    }

    fun assertAssignmentDetails(assignment: AssignmentApiModel) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("assignmentName").assertTextEquals(assignment.name)
        composeTestRule.onNodeWithTag("points").assertTextContains(assignment.pointsPossible?.toInt().toString(), substring = true, ignoreCase = true)
    }

    fun assertAssignmentTitle(assignmentName: String) {
        composeTestRule.onNodeWithTag("assignmentName").assertTextEquals(assignmentName)
    }

    fun assertAssignmentSubmitted() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.submissionStatusSuccessTitle)).performScrollTo().assertIsDisplayed()
        assertAssignmentSubmittedStatus()
    }

    fun assertAssignmentSubmittedStatus() {
        waitForStatus(getStringFromResource(R.string.submitted))
    }

    fun assertAssignmentGraded(score: String) {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("gradeCellScore", useUnmergedTree = true).assertTextContains(score, substring = true)
        waitForStatus(getStringFromResource(R.string.gradedSubmissionLabel))
    }

    fun assertGradeDisplayed(grade: String) {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("gradeCellGrade", useUnmergedTree = true).assertTextContains(grade, substring = true)
    }

    fun assertGradeNotDisplayed() {
        composeTestRule.onNodeWithTag("gradeCellGrade", useUnmergedTree = true).assertDoesNotExist()
    }

    fun assertOutOfTextDisplayed(outOfText: String) {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo()
        composeTestRule.onNodeWithTag("gradeCellOutOf", useUnmergedTree = true).assertTextContains(outOfText, substring = true)
    }

    fun assertOutOfTextNotDisplayed() {
        composeTestRule.onNodeWithTag("gradeCellOutOf", useUnmergedTree = true).assertDoesNotExist()
    }

    fun assertScoreDisplayed(score: String) {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo()
        composeTestRule.onNodeWithTag("gradeCellScore", useUnmergedTree = true).assertTextContains(score, substring = true)
    }

    fun assertScoreNotDisplayed() {
        composeTestRule.onNodeWithTag("gradeCellScore", useUnmergedTree = true).assertDoesNotExist()
    }

    fun assertAssignmentLocked() {
        composeTestRule.onNodeWithTag("lockedMessage").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("lockedMessage").assertTextContains("this assignment is locked by the module", substring = true, ignoreCase = true)
    }

    fun refresh() {
        composeTestRule.waitForIdle()
        // Deliberate (non-fling) drag starting inside the content area so the Compose pull-to-refresh
        // engages rather than the gesture being treated as a scroll/fling.
        composeTestRule.onNodeWithTag("assignmentDetailsPage").performTouchInput {
            swipeDown(startY = centerY, endY = bottom, durationMillis = 600)
        }
        composeTestRule.waitForIdle()
    }

    fun waitForSubmissionComplete() {
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodes(hasText(getStringFromResource(R.string.submissionStatusSuccessTitle))).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun goToSubmissionDetails() {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo().performClick()
        // The grade-cell click posts a navigation consumed by the host on the next recomposition;
        // wait so the submission-details screen is in place before callers interact with it.
        composeTestRule.waitForIdle()
    }

    fun assertSubmissionAndFeedbackLabel() {
        composeTestRule.onNodeWithTag("submissionAndRubricLabel", useUnmergedTree = true).assertTextEquals(getStringFromResource(R.string.submissionAndFeedback))
    }

    private fun waitForStatus(text: String) {
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasTestTag("submissionStatus") and hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun assertCustomStatus(status: String) {
        waitForStatus(status)
    }

    fun assertStatusSubmitted() {
        waitForStatus(getStringFromResource(R.string.submitted))
    }

    fun assertStatusLate() {
        waitForStatus(getStringFromResource(R.string.lateSubmissionLabel))
    }

    fun assertStatusNotSubmitted() {
        waitForStatus(getStringFromResource(R.string.notSubmitted))
    }

    fun assertStatusMissing() {
        waitForStatus(getStringFromResource(R.string.missingAssignment))
    }

    fun assertStatusGraded() {
        waitForStatus(getStringFromResource(R.string.gradedSubmissionLabel))
    }

    fun viewQuiz() {
        composeTestRule.onNodeWithTag("submitButton").assertTextContains(getStringFromResource(R.string.viewQuiz), ignoreCase = true).performClick()
    }

    fun clickSubmit() {
        composeTestRule.onNodeWithTag("submitButton").performClick()
    }

    fun scrollToAssignmentDescription() {
        Thread.sleep(3000)
        // The description WebView is an Android View hosted inside the Compose screen's vertical
        // scroll, so Espresso's scrollTo() (which needs a ScrollView/NestedScrollView ancestor)
        // can't reach it. Scroll the Compose content to the description wrapper instead, then wait
        // for the hosted WebView to be present.
        composeTestRule.onNodeWithTag("assignmentDescription").performScrollTo()
        composeTestRule.waitForIdle()
        waitForMatcherWithSleeps(withId(R.id.contentWebView), timeout = 30000, pollInterval = 1000)
    }

    fun openOverflowMenu() {
        try {
            // View-based screens (e.g. the course home page) expose the overflow as an Android view.
            Espresso.onView(
                allOf(
                    ViewMatchers.withContentDescription(stringContainsTextCaseInsensitive("More options")),
                    ViewMatchers.isDisplayed()
                )
            ).click()
        } catch (e: NoMatchingViewException) {
            // The migrated (Compose) Assignment Details screen; scope to its root so the list
            // screen's overflow lingering behind it isn't matched as a second node.
            composeTestRule.onNode(
                hasTestTag("overflowMenu") and hasAnyAncestor(hasTestTag("assignmentDetailsPage"))
            ).performClick()
        }
    }

    fun assertDisplaysAddBookmarkButton() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.addBookmark)).assertIsDisplayed()
    }

    fun assertSelectedAttempt(attemptNumber: Int) {
        composeTestRule.onNode(hasTestTag("attemptTitle") and hasText("Attempt $attemptNumber"), useUnmergedTree = true).assertExists()
    }

    fun assertNoAttemptSpinner() {
        composeTestRule.onNodeWithTag("attemptSpinner").assertDoesNotExist()
    }

    fun assertAttemptSpinnerDisplayed() {
        // A refresh reloads attempts asynchronously, so wait for the spinner (shown once there is
        // more than one attempt) to appear rather than asserting immediately.
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithTag("attemptSpinner").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("attemptSpinner").assertIsDisplayed()
    }

    fun selectAttempt(attemptNumber: Int) {
        composeTestRule.onNodeWithTag("attemptSpinner").performClick()
        // The dropdown opens in a Popup composed after the click; wait for the target item.
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasTestTag("attemptListItem") and hasText("Attempt $attemptNumber", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNode(hasTestTag("attemptListItem") and hasText("Attempt $attemptNumber", substring = true)).performClick()
    }

    fun assertAttemptInformation() {
        composeTestRule.onNodeWithTag("attemptSpinner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("attemptTitle", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("attemptDate", useUnmergedTree = true).assertExists()
    }

    fun assertSubmissionTypeDisplayed(submissionType: String) {
        composeTestRule.onNodeWithTag("submissionTypes").performScrollTo().assertTextContains(submissionType, substring = true)
    }

    fun assertSubmissionOptionDisplayed(submissionType: String) {
        // Submit-type picker is still an AlertDialog (Android View).
        onView(withText(submissionType)).inRoot(isDialog()).check(matches(ViewMatchers.isDisplayed()))
    }

    fun assertReminderViewDisplayed(position: Int = 0) {
        composeTestRule.onNodeWithTag("reminderView-$position").assertExists()
    }

    fun assertCheckpointDisplayed(position: Int, name: String, grade: String) {
        composeTestRule.onNode(
            hasTestTag("checkpointName")
                .and(hasText(name))
                .and(hasAnyAncestor(hasTestTag("checkpointItem-$position"))),
            useUnmergedTree = true
        ).assertExists()
        composeTestRule.onNode(
            hasTestTag("checkpointGrade")
                .and(hasText(grade))
                .and(hasAnyAncestor(hasTestTag("checkpointItem-$position"))),
            useUnmergedTree = true
        ).assertExists()
    }

    fun assertNoDescriptionViewDisplayed() {
        composeTestRule.onNodeWithTag("noDescription").performScrollTo().assertIsDisplayed()
    }

    fun clickCustom() {
        // Custom reminder picker is still an AlertDialog (Android View).
        Espresso.onData(org.hamcrest.Matchers.anything()).inRoot(isDialog()).atPosition(6).perform(click())
    }

    fun fillQuantity(quantity: String) {
        onView(withId(R.id.quantity)).scrollTo().typeText(quantity)
        Espresso.closeSoftKeyboard()
    }

    fun clickHoursBefore() {
        onView(withId(R.id.hours)).scrollTo().click()
    }

    fun clickDaysBefore() {
        onView(withId(R.id.days)).scrollTo().click()
    }

    fun assertDoneButtonIsDisabled() {
        onView(withText(R.string.done)).check(matches(not(isEnabled())))
    }

    fun clickDone() {
        onView(withText(R.string.done)).click()
    }

    fun clickDraftSubmission() {
        composeTestRule.onNodeWithTag("draftContainer").performScrollTo().performClick()
    }

    fun clickSubmissionAndRubric() {
        composeTestRule.onNodeWithTag("gradeCell").performScrollTo().performClick()
        // The grade-cell click posts a navigation that the host consumes via a LaunchedEffect on
        // the next recomposition; wait so the resulting WebView (and its initial cookies) is in
        // place before callers assert on it.
        composeTestRule.waitForIdle()
    }

    fun clickComposeMessageFAB() {
        composeTestRule.onNodeWithTag("messageFab").performClick()
    }

    fun assertDraftAvailableInformation() {
        composeTestRule.onNodeWithTag("draftTitle", useUnmergedTree = true).assertTextEquals(getStringFromResource(R.string.submissionDraftAvailableTitle))
        composeTestRule.onNodeWithTag("draftSubtitle", useUnmergedTree = true).assertTextEquals(getStringFromResource(R.string.submissionDraftAvailableSubtitle))
        composeTestRule.onNodeWithTag("draftDivider").assertExists()
    }

    fun assertAttachmentIconDisplayed() {
        composeTestRule.onNodeWithTag("attachmentIcon").performScrollTo().assertIsDisplayed()
    }

    fun clickAttachmentIcon() {
        composeTestRule.onNodeWithTag("attachmentIcon").performScrollTo().performClick()
    }

    // PDF viewer method for PSPDFKit toolbar verification (Android View)
    fun assertPdfViewerToolbarDisplayed() {
        Espresso.onView(withResourceIdContaining("pspdf__toolbar_main"))
            .assertDisplayed()
    }

    //OfflineMethod
    fun assertSubmitButtonDisabled() {
        composeTestRule.onNodeWithTag("submitButton").assertIsNotEnabled()
    }
}
