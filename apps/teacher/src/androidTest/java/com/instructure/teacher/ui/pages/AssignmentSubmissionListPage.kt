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
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.actions.ForceClick
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.getStringFromResource
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithId
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withDescendant
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers

/**
 * Represents a page for managing assignment submissions.
 *
 * This class extends the `BasePage` class and provides methods for interacting with various elements on the page.
 * It contains functions for asserting the presence of specific views, clicking on elements, and performing filter actions.
 *
 * @constructor Creates an instance of the `AssignmentSubmissionListPage` class.
 */
class AssignmentSubmissionListPage : BasePage() {

    private val assignmentSubmissionListToolbar by OnViewWithId(R.id.assignmentSubmissionListToolbar)
    private val assignmentSubmissionRecyclerView by OnViewWithId(R.id.submissionsRecyclerView)
    private val assignmentSubmissionListFilterLabel by OnViewWithId(R.id.filterTitle)
    private val assignmentSubmissionClearFilter by WaitForViewWithId(R.id.clearFilterTextView, false)
    private val assignmentSubmissionFilterButton by OnViewWithId(R.id.submissionFilter, false)
    private val assignmentSubmissionFilterBySubmissionsButton by WaitForViewWithText(R.string.filterSubmissionsLowercase)
    private val assignmentSubmissionFilterBySectionButton by WaitForViewWithText(R.string.filterBySection)
    private val assignmentSubmissionStatus by OnViewWithId(R.id.submissionStatus)
    private val addMessageFAB by OnViewWithId(R.id.addMessage)
    private val enableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOnAnonymousGrading)
    private val disableAnonymousGradingMenuItem by WaitForViewWithText(R.string.turnOffAnonymousGrading)
    private val anonStatusView by WaitForViewWithId(R.id.anonGradingStatusView)
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    /**
     * Assert displays no submissions view
     *
     */
    fun assertDisplaysNoSubmissionsView() {
        onView(withText("No items") + withAncestor(R.id.emptyPandaView)).assertDisplayed()
    }

    /**
     * Assert has student submission
     *
     * @param canvasUser
     */
    fun assertHasStudentSubmission(canvasUser: CanvasUserApiModel) {
        waitForViewWithText(canvasUser.name).assertDisplayed()
    }

    /**
     * Assert filter label all submissions
     *
     */
    fun assertFilterLabelAllSubmissions() {
        assignmentSubmissionListFilterLabel.assertHasText(R.string.all_submissions)
    }

    /**
     * Click on post policies
     *
     */
    fun clickOnPostPolicies() {
        waitForViewWithId(R.id.menuPostPolicies).click()
    }

    /**
     * Assert displays clear filter
     *
     */
    fun assertDisplaysClearFilter() {
        assignmentSubmissionClearFilter.assertDisplayed()
    }

    /**
     * Assert clear filter gone
     *
     */
    fun assertClearFilterGone() {
        onView(withId(R.id.clearFilterTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**
     * Clear the existing filter by clicking on the 'Clear filter' button.
     */
    fun clearFilter() {
        assignmentSubmissionClearFilter.perform(ForceClick())
    }

    /**
     * Assert student has grade
     *
     * @param grade
     */
    fun assertStudentHasGrade(grade: String) {
        onView(withId(R.id.submissionGrade)).assertHasText(grade)
    }

    /**
     * Click on student avatar.
     *
     * @param studentName
     */
    fun clickOnStudentAvatar(studentName: String) {
        onView(withId(R.id.studentAvatar) + hasSibling(withChild(withId(R.id.studentName) + withText(studentName)))).click()
    }

    /**
     * Click filter button
     *
     */
    fun clickFilterButton() {
        assignmentSubmissionFilterButton.click()
    }

    /**
     * Click filter submissions (types)
     *
     */
    fun clickFilterSubmissions() {
        assignmentSubmissionFilterBySubmissionsButton.click()
    }

    /**
     * Click filter section(s)
     *
     */
    fun clickFilterBySection() {
        assignmentSubmissionFilterBySectionButton.click()
    }

    /**
     * Click submission
     *
     * @param student
     */
    fun clickSubmission(student: CanvasUserApiModel) {
        waitForMatcherWithRefreshes(withText(student.name))
        waitForViewWithText(student.name).click()
    }

    /**
     * Click submission
     *
     * @param student
     */
    fun clickSubmission(student: User) {
        waitForMatcherWithRefreshes(withId(R.id.submissionsRecyclerView))
        scrollRecyclerView(R.id.submissionsRecyclerView, student.name)
        waitForViewWithText(student.name).click()
    }

    /**
     * Click filter submitted late
     *
     */
    fun clickFilterSubmittedLate() {
        waitForView(withText(R.string.submitted_late)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    /**
     * Click filter ungraded
     *
     */
    fun clickFilterUngraded() {
        waitForView(withText(R.string.not_graded)).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
    }

    /**
     * Assert filter label text
     *
     * @param text
     */
    fun assertFilterLabelText(text: Int) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }

    /**
     * Assert filter label text
     *
     * @param text
     */
    fun assertFilterLabelText(text: String) {
        assignmentSubmissionListFilterLabel.assertHasText(text)
    }


    /**
     * Assert has submission
     *
     * @param expectedCount
     */
    fun assertHasSubmission(expectedCount: Int = 1) {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    /**
     * Assert has no submission
     *
     */
    fun assertHasNoSubmission() {
        assignmentSubmissionRecyclerView.check(RecyclerViewItemCountAssertion(0))
    }

    /**
     * Assert submission status missing
     *
     */
    fun assertSubmissionStatusMissing() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_missing)
    }

    /**
     * Assert submission status submitted
     *
     */
    fun assertSubmissionStatusSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_submitted)
    }

    /**
     * Assert submission status not submitted
     *
     */
    fun assertSubmissionStatusNotSubmitted() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_not_submitted)
    }

    /**
     * Assert submission status late
     *
     */
    fun assertSubmissionStatusLate() {
        assignmentSubmissionStatus.assertHasText(R.string.submission_status_late)
    }

    /**
     * Click add message
     *
     */
    fun clickAddMessage() {
        addMessageFAB.click()
    }

    /**
     * Assert displays enable anonymous option
     *
     */
    fun assertDisplaysEnableAnonymousOption() {
        enableAnonymousGradingMenuItem.assertDisplayed()
    }

    /**
     * Assert displays disable anonymous option
     *
     */
    fun assertDisplaysDisableAnonymousOption() {
        disableAnonymousGradingMenuItem.assertDisplayed()
    }
    /**

    Clicks on the "Enable Anonymous Grading" option.
     */
    fun clickAnonymousOption() {
        enableAnonymousGradingMenuItem.click()
    }
    /**

    Asserts that the "Anonymous Grading" status view is displayed.
     */
    fun assertDisplaysAnonymousGradingStatus() {
        anonStatusView.assertHasText(R.string.anonymousGradingLabel)
    }
    /**

    Asserts that the "Anonymous Name" is displayed.
     */
    fun assertDisplaysAnonymousName() {
        waitForViewWithId(R.id.studentName).assertHasText(R.string.anonymousStudentLabel)
    }
    /**

    Clicks on the "OK" button in the filter dialog.
     */
    fun clickFilterDialogOk() {
        waitForViewWithText(android.R.string.ok).click()
    }
    /**
     *
    * Asserts that the file with the given filename is displayed.
    * @param fileName The name of the file.
     */
    fun assertFileDisplayed(fileName: String) {
        val matcher =
            Matchers.allOf(ViewMatchers.withId(R.id.fileNameText), ViewMatchers.withText(fileName))
        Espresso.onView(matcher).assertDisplayed()
    }
    /**

    Asserts that the comment attachment with the given filename and display name is displayed.
    @param fileName The name of the attachment file.
    @param displayName The display name of the attachment.
     */
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

    /**
     * Asserts that the grades are hidden for the student with the given name.
     *
     * @param studentName The name of the student.
     */
    fun assertGradesHidden(studentName: String) {
        onView(
            allOf(
                withId(R.id.studentName),
                withText(studentName),
                withAncestor(
                    allOf(
                        withId(R.id.submissionsRecyclerView),
                        withDescendant(withId(R.id.hiddenIcon))
                    )
                )
            )
        ).check(matches(isDisplayed()))
    }

    /**
     * Assert the 'Filter By..' (section) dialog details like title, subtitle, buttons.
     */
    fun assertSectionFilterDialogDetails() {
        waitForView(withId(R.id.alertTitle) + withText(getStringFromResource(R.string.filterBy)) + withAncestor(R.id.topPanel)).assertDisplayed()
        onView(withText(getStringFromResource(R.string.sections)) + withAncestor(R.id.customPanel)).assertDisplayed()
        onView(withId(android.R.id.button2) + withText(getStringFromResource(R.string.cancel)) + withAncestor(R.id.buttonPanel)).assertDisplayed()
        onView(withId(android.R.id.button1) + withText(getStringFromResource(R.string.ok)) + withAncestor(R.id.buttonPanel)).assertDisplayed()
    }

    /**
     * Filter by the given section name.
     * @param sectionName: The section to filter by.
     */
    fun filterBySection(sectionName: String) {
        waitForView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(sectionName) + withAncestor(R.id.customPanel))).click()
        onView(withId(android.R.id.button1) + withText(getStringFromResource(R.string.ok)) + withAncestor(R.id.buttonPanel)).click()
    }
}
