/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages


import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers

class AssignmentSubmissionListPage : BasePage() {

    private val assignmentSubmissionListToolbar by OnViewWithId(R.id.assignmentSubmissionListToolbar)

    private val assignmentSubmissionRecyclerView by OnViewWithId(R.id.submissionsRecyclerView)

    private val assignmentSubmissionListFilterLabel by OnViewWithId(R.id.filterTitle)

    private val assignmentSubmissionClearFilter by OnViewWithId(R.id.clearFilterTextView, false)

    private val assignmentSubmissionFilterButton by OnViewWithId(R.id.submissionFilter, false)

    private val assignmentSubmissionFilterBySubmissionsButton by WaitForViewWithText(R.string.filterSubmissionsLowercase)

    private val assignmentSubmissionStatus by OnViewWithId(R.id.submissionStatus)

    private val addMessageFAB by OnViewWithId(R.id.addMessage)

    private val enableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOnAnonymousGrading)

    private val disableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOffAnonymousGrading)

    private val anonStatusView by WaitForViewWithId(R.id.anonGradingStatusView)

    //Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    fun assertDisplaysNoSubmissionsView() {
        onView(withText("No items") + withAncestor(R.id.emptyPandaView)).assertDisplayed()
    }

    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        waitForViewWithText(canvasUser.name).assertDisplayed()
    }

    fun assertFilterLabelAllSubmissions() {
        assignmentSubmissionListFilterLabel.assertHasText(R.string.all_submissions)
    }

    fun clickOnPostPolicies() {
        waitForViewWithId(R.id.menuPostPolicies).click()
    }

    fun assertDisplaysClearFilter() {
        assignmentSubmissionClearFilter.assertDisplayed()
    }

    fun assertClearFilterGone() {
        assignmentSubmissionClearFilter.assertGone()
    }

    fun assertStudentHasGrade(grade: String) {
        onView(withId(R.id.submissionGrade)).assertHasText(grade)
    }

    fun clickFilterButton() {
        assignmentSubmissionFilterButton.click()
    }

    fun clickFilterSubmissions() {
        assignmentSubmissionFilterBySubmissionsButton.click()
    }

    fun clickSubmission(student: CanvasUserApiModel) {
        waitForMatcherWithRefreshes(withText(student.name))
        waitForViewWithText(student.name).click()
    }

    fun clickSubmission(student: User) {
        waitForMatcherWithRefreshes(withId(R.id.submissionsRecyclerView))
        scrollRecyclerView(R.id.submissionsRecyclerView, student.name)
        waitForViewWithText(student.name).click()
    }

    fun clickFilterSubmittedLate() {
        onView(withText(R.string.submitted_late)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    fun clickFilterUngraded() {
        onView(withText(R.string.not_graded)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    fun assertFilterLabelText(text: Int) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }

    fun assertHasSubmission(expectedCount: Int = 1) {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    fun assertHasNoSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(0))
    }

    fun assertSubmissionStatusMissing() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_missing)
    }

    fun assertSubmissionStatusSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_submitted)
    }

    fun assertSubmissionStatusNotSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_not_submitted)
    }

    fun assertSubmissionStatusLate() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_late)
    }

    fun clickAddMessage() {
        addMessageFAB.click()
    }

    fun assertDisplaysEnableAnonymousOption() {
        enableAnonymousGradingMenuItem.assertDisplayed()
    }

    fun assertDisplaysDisableAnonymousOption() {
        disableAnonymousGradingMenuItem.assertDisplayed()
    }

    fun clickAnonymousOption() {
        enableAnonymousGradingMenuItem.click()
    }

    fun assertDisplaysAnonymousGradingStatus() {
        anonStatusView.assertHasText(R.string.anonymousGradingLabel)
    }

    fun assertDisplaysAnonymousName() {
        waitForViewWithId(R.id.studentName).assertHasText(R.string.anonymousStudentLabel)
    }

    fun clickFilterDialogOk() {
        waitForViewWithText(android.R.string.ok).click()
    }

    fun assertFileDisplayed(fileName: String) {
        val matcher =
            Matchers.allOf(ViewMatchers.withId(R.id.fileNameText), ViewMatchers.withText(fileName))
        Espresso.onView(matcher).assertDisplayed()
    }

    fun assertCommentAttachmentDisplayedCommon(fileName: String, displayName: String) {
        val commentMatcher = Matchers.allOf(
            ViewMatchers.withId(R.id.commentHolder),
            ViewMatchers.hasDescendant(
                Matchers.allOf(
                    ViewMatchers.withText(displayName),
                    ViewMatchers.withId(R.id.userNameTextView)
                )
            ),
            ViewMatchers.hasDescendant(
                Matchers.allOf(
                    ViewMatchers.withText(fileName),
                    ViewMatchers.withId(R.id.attachmentNameTextView)
                )
            )
        )
        onView(commentMatcher).assertDisplayed()
    }

    fun assertGradesHidden(studentName: String) {
        onView(allOf(withId(R.id.studentName), withText(studentName), withAncestor(allOf(withId(R.id.submissionsRecyclerView), withDescendant(withId(R.id.hiddenIcon)))))).check(matches(isDisplayed()))
    }
}
