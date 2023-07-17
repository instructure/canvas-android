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
 *
 */
package com.instructure.teacher.ui.pages

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withText
import com.instructure.teacher.R

/**
 * Represents the Quiz Submission List page.
 *
 * This page extends the BasePage class and provides functionality for interacting with quiz submissions.
 * It includes methods for asserting various states of the submission list, filtering submissions,
 * clicking on a submission, and adding a message. The page includes multiple view elements that can be accessed
 * for performing assertions and interactions. The page does not have a specific resource ID associated with it.
 */
class QuizSubmissionListPage : BasePage() {

    private val assignmentSubmissionListToolbar by OnViewWithId(R.id.assignmentSubmissionListToolbar)
    private val assignmentSubmissionRecyclerView by OnViewWithId(R.id.submissionsRecyclerView)
    private val assignmentSubmissionListFilterLabel by OnViewWithId(R.id.filterTitle)
    private val assignmentSubmissionClearFilter by WaitForViewWithId(R.id.clearFilterTextView, false)
    private val assignmentSubmissionFilterButton by OnViewWithId(R.id.submissionFilter)
    private val assignmentSubmissionStatus by OnViewWithId(R.id.submissionStatus)
    private val addMessageFAB by OnViewWithId(R.id.addMessage)
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    /**
     * Asserts that the submission list view displays "No items" indicating no submissions.
     */
    fun assertDisplaysNoSubmissionsView() {
        onView(withText("No items") + withAncestor(R.id.emptyPandaView)).assertDisplayed()
    }

    /**
     * Asserts that the student submission is displayed in the submission list.
     *
     * @param canvasUser The student's CanvasUserApiModel.
     */
    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        waitForViewWithText(canvasUser.name).assertDisplayed()
    }

    /**
     * Asserts that the filter label displays "All Submissions".
     */
    fun assertFilterLabelAllSubmissions() {
        assignmentSubmissionListFilterLabel.assertHasText(R.string.all_submissions)
    }

    /**
     * Asserts that the clear filter button is displayed.
     */
    fun assertDisplaysClearFilter() {
        assignmentSubmissionClearFilter.assertDisplayed()
    }

    /**
     * Asserts that the clear filter button is not displayed.
     */
    fun assertClearFilterGone() {
        assignmentSubmissionClearFilter.assertGone()
    }

    /**
     * Clicks on the filter button.
     */
    fun clickFilterButton() {
        assignmentSubmissionFilterButton.click()
    }

    /**
     * Clicks on the "Filter submissions" option in the filter dialog.
     */
    fun clickFilterSubmissions() {
        onViewWithText("Filter submissions").click()
    }

    /**
     * Clicks the positive button in the dialog.
     */
    fun clickDialogPositive() {
        onViewWithId(android.R.id.button1).click()
    }

    /**
     * Clicks on a specific submission in the submission list.
     *
     * @param student The student's CanvasUserApiModel.
     */
    fun clickSubmission(student: CanvasUserApiModel) {
        waitForViewWithText(student.name).click()
    }

    /**
     * Clicks on a specific submission in the submission list.
     *
     * @param student The student.
     */
    fun clickSubmission(student: User) {
        waitForViewWithText(student.name).click()
    }

    /**
     * Filters the submissions by "Submitted Late".
     */
    fun filterSubmittedLate() {
        onView(withText(R.string.submitted_late)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    /**
     * Filters the submissions by "Pending Review".
     */
    fun filterPendingReview() {
        onView(withText(R.string.havent_been_graded)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    /**
     * Filters the submissions by "Not Graded".
     */
    fun filterNotGraded() {
        onView(withText(R.string.not_graded)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    /**
     * Asserts that the filter label text matches the specified resource string.
     *
     * @param text The resource string representing the filter label text.
     */
    fun assertFilterLabelText(text: Int) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }

    /**
     * Asserts that the submission list has at least one submission.
     */
    fun assertHasSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(1))
    }

    /**
     * Asserts that the submission list does not have any submissions.
     */
    fun assertHasNoSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(0))
    }

    /**
     * Asserts that the submission status is displayed as "Missing".
     */
    fun assertSubmissionStatusMissing() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_missing)
    }

    /**
     * Asserts that the submission status is displayed as "Submitted".
     */
    fun assertSubmissionStatusSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_submitted)
    }

    /**
     * Clicks on the "Add Message" floating action button.
     */
    fun clickAddMessage() {
        addMessageFAB.click()
    }
}
