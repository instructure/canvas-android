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
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.espresso.ModuleItemInteractions
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
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.swipeDown
import com.instructure.teacher.R
import org.hamcrest.Matchers

/**
 * Assignment details page
 *
 * @constructor Create empty Assignment details page
 */
@Suppress("unused")
class AssignmentDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage(pageResId = R.id.assignmentDetailsPage) {

    private val backButton by OnViewWithContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description,false)
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
    private val descriptionWebView by WaitForViewWithId(R.id.contentWebView, autoAssert = false)
    private val noDescriptionTextView by WaitForViewWithId(R.id.noDescriptionTextView, autoAssert = false)
    private val availabilityTextView by OnViewWithId(R.id.availabilityTextView, autoAssert = false)
    private val availabilityLayout by OnViewWithId(R.id.availabilityLayout, autoAssert = false)
    private val availableFromTextView by OnViewWithId(R.id.availableFromTextView, autoAssert = false)
    private val availableToTextView by OnViewWithId(R.id.availableToTextView, autoAssert = false)
    private val gradedDonutWrapper by OnViewWithId(R.id.gradedWrapper, autoAssert = false)
    private val ungradedDonutWrapper by OnViewWithId(R.id.ungradedWrapper, autoAssert = false)
    private val notSubmittedDonutWrapper by OnViewWithId(R.id.notSubmittedWrapper, autoAssert = false)

    /**
     * Assert that the description webview is visible within the content web view.
     *
     */
    fun assertDisplaysInstructions() {
        scrollTo(R.id.contentWebView)
        descriptionWebView.assertVisible()
    }

    /**
     * Assert displays no instructions view.
     *
     */
    fun assertDisplaysNoInstructionsView() {
        noDescriptionTextView.assertVisible()
    }

    /**
     * Open all dates page (by clicking on the due dates layout).
     *
     */
    fun openAllDatesPage() {
        dueDatesLayout.click()
    }

    /**
     * Open edit page (by clicking on the Edit button).
     *
     */
    fun openEditPage() {
        editButton.click()
    }

    /**
     * Open all submissions page (by clicking on the View All Submissions button).
     *
     */
    fun openAllSubmissionsPage() {
        scrollTo(R.id.viewAllSubmissions)
        viewAllSubmissions.click()
    }

    /**
     * Open graded submissions
     *
     */
    fun openGradedSubmissions() {
        gradedDonutWrapper.click()
    }

    /**
     * Open ungraded submissions
     *
     */
    fun openUngradedSubmissions() {
        ungradedDonutWrapper.click()
    }

    /**
     * Open not submitted submissions
     *
     */
    fun openNotSubmittedSubmissions() {
        notSubmittedDonutWrapper.click()
    }

    /**
     * Assert assignment details
     *
     * @param assignment
     */
    fun assertAssignmentDetails(assignment: Assignment) {
        assertAssignmentDetails(assignmentNameTextView, assignment.name!!, assignment.published)
    }

    /**
     * Assert assignment details
     *
     * @param assignment
     */
    fun assertAssignmentDetails(assignment: AssignmentApiModel) {
        assertAssignmentDetails(assignmentNameTextView, assignment.name, assignment.published)
    }

    /**
     * Assert assignment closed
     *
     */
    fun assertAssignmentClosed() {
        availableFromTextView.assertNotDisplayed()
        availableToTextView.assertNotDisplayed()
        availabilityLayout.assertDisplayed()
        availabilityTextView.assertHasText(com.instructure.teacher.R.string.closed)
    }

    /**
     * Assert to filled and from empty
     *
     */
    fun assertToFilledAndFromEmpty() {
        availableFromTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableToTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    /**
     * Assert from filled and to empty
     *
     */
    fun assertFromFilledAndToEmpty() {
        availableToTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableFromTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    /**
     * Assert submission type none
     *
     */
    fun assertSubmissionTypeNone() {
        scrollToSubmissionType()
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_none)
    }

    /**
     * Assert submission type on paper
     *
     */
    fun assertSubmissionTypeOnPaper() {
        scrollToSubmissionType()
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onPaper)
    }

    /**
     * Assert submission type online text entry
     *
     */
    fun assertSubmissionTypeOnlineTextEntry() {
        scrollToSubmissionType()
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineTextEntry)
    }

    /**
     * Assert submission type online url
     *
     */
    fun assertSubmissionTypeOnlineUrl() {
        scrollToSubmissionType()
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineURL)
    }

    /**
     * Assert submission type online upload
     *
     */
    fun assertSubmissionTypeOnlineUpload() {
        scrollToSubmissionType()
        submissionTypesTextView.assertDisplayed().assertHasText(R.string.canvasAPI_onlineUpload)
    }

    /**
     * Assert assignment name changed
     *
     * @param newAssignmentName
     */
    fun assertAssignmentName(newAssignmentName: String) {
        assignmentNameTextView.assertHasText(newAssignmentName)
    }

    /**
     * Assert assignment points changed
     *
     * @param newAssignmentPoints
     */
    fun assertAssignmentPointsChanged(newAssignmentPoints: String) {
        pointsTextView.assertContainsText(newAssignmentPoints)
    }

    /**
     * Assert displays description
     *
     * @param text
     */
    fun assertDisplaysDescription(text: String) {
        descriptionWebView.assertVisible()
        Web.onWebView().withElement(
            DriverAtoms.findElement(
                Locator.XPATH,
                "//div[@id='content' and contains(text(),'$text')]"
            )
        ).check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(text)))    }

    /**
     * Assert needs grading
     *
     * @param actual
     * @param outOf
     */
    fun assertNeedsGrading(actual: Int = 1, outOf: Int = 1) {
        val resources = InstrumentationRegistry.getTargetContext()
        ungradedDonutWrapper.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_needs_grading).format(actual, outOf))
    }

    /**
     * Assert not submitted
     *
     * @param actual
     * @param outOf
     */
    fun assertNotSubmitted(actual: Int = 1, outOf: Int = 1) {
        val resources = InstrumentationRegistry.getTargetContext()
        notSubmittedDonutWrapper.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_unsubmitted).format(actual, outOf))
    }

    /**
     * Assert has graded
     *
     * @param actual
     * @param outOf
     */
    fun assertHasGraded(actual: Int =1, outOf: Int = 1) {
        val resources = InstrumentationRegistry.getTargetContext()
        gradedDonutWrapper.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_graded).format(actual, outOf))
    }

    /**
     * View all submission
     *
     */
    fun viewAllSubmission() {
        onView(withId(R.id.viewAllSubmissions)).click()
    }

    private fun scrollToSubmissionType() {
        scrollTo(R.id.submissionTypesTextView)
    }

    /**
     * Wait for render
     *
     */
    fun waitForRender() {
        waitForView(withId(R.id.assignmentDetailsPage))
    }

    /**
     * Refresh
     *
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Assert published status
     *
     * @param published
     */
    fun assertPublishedStatus(published: Boolean) {
        if (published) {
            publishStatusTextView.assertHasText(R.string.published)
        } else {
            publishStatusTextView.assertHasText(R.string.not_published)
        }
    }

    /**
     * Assert multiple due dates
     *
     */
    fun assertMultipleDueDates() {
        onView(withId(R.id.otherDueDateTextView) + withText(R.string.multiple_due_dates)).assertDisplayed()
    }

    /**
     * Assert module item details
     *
     * @param moduleItemName
     * @param published
     */
    private fun assertAssignmentDetails(viewInteraction: ViewInteraction, moduleItemName: String, published: Boolean) {
        viewInteraction.assertHasText(moduleItemName)
        assertPublishedStatus(published)
    }
}
