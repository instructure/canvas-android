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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.*
import com.instructure.espresso.page.onViewWithText
import com.instructure.student.R
import com.instructure.student.ui.pages.AssignmentDetailsPage
import org.hamcrest.Matchers

class AssignmentDetailsRenderPage : AssignmentDetailsPage() {

    val toolbar by OnViewWithId(R.id.toolbar)
    val assignmentName by OnViewWithId(R.id.assignmentName)
    val points by OnViewWithId(R.id.points)
    val submissionStatus by OnViewWithId(R.id.submissionStatus)
    val date by OnViewWithId(R.id.dueDateTextView)
    val submissionTypes by OnViewWithId(R.id.submissionTypesTextView)
    val fileTypes by OnViewWithId(R.id.fileTypesTextView)
    val noDescription by OnViewWithId(R.id.noDescriptionContainer)
    val descriptionWebView by OnViewWithId(R.id.descriptionWebView)
    val gradeContainer by OnViewWithId(R.id.gradeContainer)
    val submissionSuceeded by OnViewWithText(R.string.submissionStatusSuccessTitle)
    val submissionStatusUploading by OnViewWithId(R.id.submissionStatusUploading)
    val submissionStatusFailed by OnViewWithId(R.id.submissionStatusFailed)
    val quizDetails by OnViewWithId(R.id.quizDetails)
    val questionCountText by OnViewWithId(R.id.questionCountText)
    val timeLimitText by OnViewWithId(R.id.timeLimitText)
    val allowedAttemptsText by OnViewWithId(R.id.allowedAttemptsText)
    val discussionTopicHeaderContainer by OnViewWithId(R.id.discussionTopicHeaderContainer)
    val authorAvatar by OnViewWithId(R.id.authorAvatar)
    val authorName by OnViewWithId(R.id.authorName)
    val authoredDate by OnViewWithId(R.id.authoredDate)
    val attachmentIcon by OnViewWithId(R.id.attachmentIcon)
    val submitButton by OnViewWithId(R.id.submitButton)

    fun assertDisplaysToolbarTitle(text: String) {
        onViewWithText(text).assertDisplayed()
    }

    fun assertDisplaysToolbarSubtitle(text: String) {
        onViewWithText(text).assertDisplayed()
    }

    fun assertDisplaysAssignmentName(name: String) {
        assignmentName.assertHasText(name)
    }

    fun assertDisplaysPoints(text: String) {
        points.assertHasText(text)
    }

    fun assertPointsContentDescription(text: String) {
        points.assertHasContentDescription(text)
    }

    fun assertDisplaysSubmissionStatus(text: String) {
        submissionStatus.assertHasText(text)
    }

    fun assertDisplaysDate(text: String) {
        date.assertHasText(text)
    }

    fun assertDisplaysSubmissionTypes(text: String) {
        submissionTypes.assertHasText(text)
    }

    fun assertDisplaysFileTypes(text: String) {
        fileTypes.assertHasText(text)
    }

    fun assertDisplaysNoDescription() {
        noDescription.assertVisible()
        descriptionWebView.assertGone()
    }

    fun assertDisplaysGrade() {
        gradeContainer.assertVisible()
    }

    fun assertDisplaysSuccessfulSubmit() {
        assertDisplaysGrade()
        submissionSuceeded.assertVisible()
    }

    fun assertDisplaysUploadingSubmission() {
        submissionStatusUploading.assertVisible()
    }

    fun assertDisplaysFailedSubmission() {
        submissionStatusFailed.assertVisible()
    }

    fun assertDisplaysDescription(text: String) {
        descriptionWebView.assertVisible()
        noDescription.assertGone()
        onWebView().withElement(findElement(Locator.TAG_NAME, "p")).check(webMatches(getText(), Matchers.comparesEqualTo(text)))    }

    fun assertDisplaysDiscussionDescription(text: String) {
        descriptionWebView.assertVisible()
        noDescription.assertGone()
        onWebView().withElement(findElement(Locator.ID, "header_content")).check(webMatches(getText(), Matchers.comparesEqualTo(text)))
    }

    fun assertQuizDescription(timeLimit: String, allowedAttempts: String, questionCount: String) {
        quizDetails.assertVisible()
        questionCountText.assertHasText(questionCount)
        allowedAttemptsText.assertHasText(allowedAttempts)
        timeLimitText.assertHasText(timeLimit)
    }

    fun assertQuizDescription(timeLimit: Int, allowedAttempts: String, questionCount: String) {
        quizDetails.assertVisible()
        questionCountText.assertHasText(questionCount)
        allowedAttemptsText.assertHasText(allowedAttempts)
        timeLimitText.assertHasText(timeLimit)
    }

    fun assertQuizDescription(timeLimit: String, allowedAttempts: Int, questionCount: String) {
        quizDetails.assertVisible()
        questionCountText.assertHasText(questionCount)
        allowedAttemptsText.assertHasText(allowedAttempts)
        timeLimitText.assertHasText(timeLimit)
    }

    fun assertDiscussionHeader(name: String, date: String, iconVisibility: Boolean) {
        discussionTopicHeaderContainer.assertVisible()
        authorAvatar.assertVisible()
        authorName.assertHasText(name)
        authoredDate.assertHasText(date)
        if (iconVisibility) attachmentIcon.assertVisible()
        else attachmentIcon.assertGone()
    }

    fun assertSubmitButton(submitButtonText: Int) {
        submitButton.assertVisible()
        submitButton.assertHasText(submitButtonText)
    }
}
