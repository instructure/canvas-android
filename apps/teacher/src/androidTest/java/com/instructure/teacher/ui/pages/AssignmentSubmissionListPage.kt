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

import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage


import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

class AssignmentSubmissionListPage : BasePage() {

    private val assignmentSubmissionListToolbar by OnViewWithId(R.id.assignmentSubmissionListToolbar)

    private val assignmentSubmissionRecyclerView by OnViewWithId(R.id.submissionsRecyclerView)

    private val assignmentSubmissionListFilterLabel by OnViewWithId(R.id.filterTitle)

    private val assignmentSubmissionClearFilter by OnViewWithId(R.id.clearFilterTextView, false)

    private val assignmentSubmissionFilterButton by OnViewWithId(R.id.submissionFilter)

    private val assignmentSubmissionFilterBySubmissionsButton by WaitForViewWithText(R.string.filterSubmissionsLowercase)

    private val assignmentSubmissionStatus by OnViewWithId(R.id.submissionStatus)

    private val addMessageFAB by OnViewWithId(R.id.addMessage)

    private val enableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOnAnonymousGrading)

    private val disableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOffAnonymousGrading)

    private val anonStatusView by WaitForViewWithId(R.id.anonGradingStatusView)

    //Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    fun assertDisplaysNoSubmissionsView() {
        emptyPandaView.assertDisplayed()
    }

    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        waitForViewWithText(canvasUser.name).assertDisplayed()
    }

    fun assertFilterLabelAllSubmissions() {
        assignmentSubmissionListFilterLabel.assertHasText(R.string.all_submissions)
    }

    fun assertDisplaysClearFilter() {
        assignmentSubmissionClearFilter.assertDisplayed()
    }

    fun assertClearFilterGone() {
        assignmentSubmissionClearFilter.assertGone()
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
        waitForMatcherWithRefreshes(withText(student.name))
        waitForViewWithText(student.name).click()
    }

    fun clickFilterSubmittedLate() {
        onViewWithText(R.string.submitted_late).click()
    }

    fun clickFilterUngraded() {
        onViewWithText(R.string.not_graded).click()
    }

    fun assertFilterLabelText(text: Int) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }

    fun assertHasSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(1))
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
}
