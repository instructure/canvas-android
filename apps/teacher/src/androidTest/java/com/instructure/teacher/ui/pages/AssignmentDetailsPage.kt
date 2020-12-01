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


import androidx.test.InstrumentationRegistry
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.canvasapi2.models.Assignment
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasContentDescription
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.assertNotHasText
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForView
import com.instructure.teacher.R

@Suppress("unused")
class AssignmentDetailsPage : BasePage(pageResId = R.id.assignmentDetailsPage) {

    private val backButton by OnViewWithContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description)
    private val toolbarTitle by OnViewWithText(R.string.assignment_details)
    private val assignmentNameTextView by OnViewWithId(R.id.assignmentNameTextView)
    private val pointsTextView by OnViewWithId(R.id.pointsTextView)
    private val publishStatusIconView by OnViewWithId(R.id.publishStatusIconView)
    private val publishStatusTextView by OnViewWithId(R.id.publishStatusTextView)
    private val dueSectionLabel by OnViewWithId(R.id.dueSectionLabel)
    private val submissionTypesSectionLabel by OnViewWithId(R.id.submissionTypesSectionLabel)
    private val submissionTypesTextView by OnViewWithId(R.id.submissionTypesTextView)
    private val instructionsSectionLabel by OnViewWithId(R.id.instructionsSectionLabel, autoAssert = false)
    private val editButton by OnViewWithId(R.id.menu_edit)
    private val dueDatesLayout by OnViewWithId(R.id.dueLayout)
    private val submissionsLayout by OnViewWithId(R.id.submissionsLayout)
    private val viewAllSubmissions by OnViewWithId(R.id.viewAllSubmissions)
    private val descriptionWebView by WaitForViewWithId(R.id.descriptionWebView, autoAssert = false)
    private val noDescriptionTextView by WaitForViewWithId(R.id.noDescriptionTextView, autoAssert = false)
    private val availabilityTextView by OnViewWithId(R.id.availabilityTextView, autoAssert = false)
    private val availabilityLayout by OnViewWithId(R.id.availabilityLayout, autoAssert = false)
    private val availableFromTextView by OnViewWithId(R.id.availableFromTextView, autoAssert = false)
    private val availableToTextView by OnViewWithId(R.id.availableToTextView, autoAssert = false)
    private val gradedDonutWrapper by OnViewWithId(R.id.gradedWrapper, autoAssert = false)
    private val ungradedDonutWrapper by OnViewWithId(R.id.ungradedWrapper, autoAssert = false)
    private val notSubmittedDonutWrapper by OnViewWithId(R.id.notSubmittedWrapper, autoAssert = false)

    fun assertDisplaysInstructions() {
        scrollTo(R.id.descriptionWebView)
        descriptionWebView.assertVisible()
    }

    fun assertDisplaysNoInstructionsView() {
        noDescriptionTextView.assertVisible()
    }

    fun openAllDatesPage() {
        dueDatesLayout.click()
    }

    fun openEditPage() {
        editButton.click()
    }

    fun openSubmissionsPage() {
        scrollTo(R.id.viewAllSubmissions)
        viewAllSubmissions.click()
    }

    fun openGradedSubmissions() {
        gradedDonutWrapper.click()
    }

    fun openUngradedSubmissions() {
        ungradedDonutWrapper.click()
    }

    fun openNotSubmittedSubmissions() {
        notSubmittedDonutWrapper.click()
    }

    fun assertAssignmentDetails(assignment: Assignment) {
        assignmentNameTextView.assertHasText(assignment.name!!)
        if (assignment.published) {
            publishStatusTextView.assertHasText(R.string.published)
        } else {
            publishStatusTextView.assertHasText(R.string.not_published)
        }
    }

    fun assertAssignmentClosed() {
        availableFromTextView.assertNotDisplayed()
        availableToTextView.assertNotDisplayed()
        availabilityLayout.assertDisplayed()
        availabilityTextView.assertHasText(com.instructure.teacher.R.string.closed)
    }

    fun assertToFilledAndFromEmpty() {
        availableFromTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableToTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    fun assertFromFilledAndToEmpty() {
        availableToTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableFromTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    fun assertSubmissionTypeNone() {
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_none)
    }

    fun assertSubmissionTypeOnPaper() {
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onPaper)
    }

    fun assertSubmissionTypeOnlineTextEntry() {
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineTextEntry)
    }

    fun assertSubmissionTypeOnlineUrl() {
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineURL)
    }

    fun assertSubmissionTypeOnlineUpload() {
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineUpload)
    }

    fun assertAssignmentNameChanged(newAssignmentName: String) {
        assignmentNameTextView.assertHasText(newAssignmentName)
    }

    fun assertAssignmentPointsChanged(newAssignmentPoints: String) {
        pointsTextView.assertContainsText(newAssignmentPoints)
    }

    fun assertHasSubmitted(actual: Int = 1, outOf: Int = 1) {
        val resources = InstrumentationRegistry.getTargetContext()
        ungradedDonutWrapper.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_needs_grading).format(actual, outOf))
    }

    fun assertNotSubmitted(actual: Int = 1, outOf: Int = 1) {
        val resources = InstrumentationRegistry.getTargetContext()
        notSubmittedDonutWrapper.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_unsubmitted).format(actual, outOf))
    }

    fun waitForRender() {
        waitForView(withId(R.id.assignmentDetailsPage))
    }
}
