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
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.QuizApiModel
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
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.swipeDown
import com.instructure.teacher.R

/**
 * Represents the Quiz Details page.
 *
 * This page extends the BasePage class and provides functionality for interacting with quiz details.
 * It includes methods for asserting various aspects of the quiz details such as instructions,
 * availability, submissions, quiz status, and quiz information. The page also includes view elements
 * that can be accessed for performing assertions and interactions. The page has a specific resource ID
 * associated with it, which is R.id.quizDetailsPage.
 */
class QuizDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage(pageResId = R.id.quizDetailsPage) {

    private val backButton by OnViewWithContentDescription(R.string.abc_action_bar_up_description,false)
    private val toolbarTitle by OnViewWithText(R.string.quiz_details)
    private val quizTitleTextView by OnViewWithId(R.id.quizTitleTextView)
    private val pointsTextView by OnViewWithId(R.id.pointsTextView, autoAssert = false)
    private val publishStatusIconView by OnViewWithId(R.id.publishStatusIconView)
    private val publishStatusTextView by OnViewWithId(R.id.publishStatusTextView)
    private val dueSectionLabel by OnViewWithId(R.id.dueSectionLabel)
    private val instructionsSectionLabel by OnViewWithId(R.id.instructionsSectionLabel, autoAssert = false)
    private val editButton by OnViewWithId(R.id.menu_edit)
    private val dueDatesLayout by OnViewWithId(R.id.dueLayout)
    private val submissionsLayout by OnViewWithId(R.id.submissionsLayout)
    private val viewAllSubmissions by OnViewWithId(R.id.viewAllSubmissions)
    private val instructionsWebView by WaitForViewWithId(R.id.contentWebView, autoAssert = false)
    private val noInstructionsTextView by WaitForViewWithId(R.id.noInstructionsTextView, autoAssert = false)
    private val availabilityTextView by OnViewWithId(R.id.availabilityTextView, autoAssert = false)
    private val availabilityLayout by OnViewWithId(R.id.availabilityLayout, autoAssert = false)
    private val availableFromTextView by OnViewWithId(R.id.availableFromTextView, autoAssert = false)
    private val availableToTextView by OnViewWithId(R.id.availableToTextView, autoAssert = false)
    private val gradedDonut by OnViewWithId(R.id.gradedWrapper)
    private val ungradedDonut by OnViewWithId(R.id.ungradedWrapper)
    private val notSubmittedDonut by OnViewWithId(R.id.notSubmittedWrapper)

    /**
     * Asserts that the instructions for the quiz are displayed.
     */
    fun assertDisplaysInstructions() {
        scrollTo(R.id.contentWebView)
        instructionsWebView.assertVisible()
    }

    /**
     * Asserts that the "No Instructions" view is displayed.
     */
    fun assertDisplaysNoInstructionsView() {
        noInstructionsTextView.assertVisible()
    }

    /**
     * Opens the All Dates page for the quiz.
     */
    fun openAllDatesPage() {
        dueDatesLayout.click()
    }

    /**
     * Opens the Edit page for the quiz.
     */
    fun openEditPage() {
        editButton.click()
    }

    /**
     * Opens the Submissions page for the quiz.
     */
    fun openSubmissionsPage() {
        scrollTo(R.id.viewAllSubmissions)
        viewAllSubmissions.click()
    }

    /**
     * Asserts the quiz details such as title and publish status.
     *
     * @param quiz The Quiz object representing the quiz details.
     */
    fun assertQuizDetails(quiz: Quiz) {
        assertQuizDetails(quiz.title!!, quiz.published)
    }

    /**
     * Asserts the quiz details such as title and publish status.
     *
     * @param quiz The Quiz object representing the quiz details.
     */
    fun assertQuizDetails(quiz: QuizApiModel) {
        assertQuizDetails(quiz.title, quiz.published)
    }

    /**
     * Assert quiz details
     * Private method used for overloading.
     * @param quizTitle The quiz's title
     * @param published The quiz's published status
     */
    private fun assertQuizDetails(quizTitle: String, published: Boolean) {
        quizTitleTextView.assertHasText(quizTitle)
        if (published) {
            publishStatusTextView.assertHasText(R.string.published)
        } else {
            publishStatusTextView.assertHasText(R.string.not_published)
        }
    }

    /**
     * Asserts that the quiz is closed and displays the "Closed" availability status.
     */
    fun assertQuizClosed() {
        availableFromTextView.assertNotDisplayed()
        availableToTextView.assertNotDisplayed()
        availabilityLayout.assertDisplayed()
        availabilityTextView.assertHasText(com.instructure.teacher.R.string.closed)
    }

    /**
     * Asserts that the "From" date is filled and the "To" date is empty in the availability section.
     */
    fun assertToFilledAndFromEmpty() {
        availableFromTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableToTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    /**
     * Asserts that the "To" date is filled and the "From" date is empty in the availability section.
     */
    fun assertFromFilledAndToEmpty() {
        availableToTextView.assertDisplayed().assertHasText(R.string.no_date_filler)
        availableFromTextView.assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    /**
     * Asserts that the quiz name has changed to the specified new quiz name.
     *
     * @param newQuizName The new quiz name to assert.
     */
    fun assertQuizNameChanged(newQuizName: String) {
        quizTitleTextView.assertHasText(newQuizName)
    }

    /**
     * Asserts that the quiz points have changed to the specified new quiz points.
     *
     * @param newQuizPoints The new quiz points to assert.
     */
    fun assertQuizPointsChanged(newQuizPoints: String) {
        pointsTextView.assertContainsText(newQuizPoints)
    }

    /**
     * Asserts that at least one submission has been made for the quiz.
     */
    fun assertHasSubmitted() {
        val resources = InstrumentationRegistry.getTargetContext()
        gradedDonut.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_graded).format(1, 1))
    }

    /**
     * Asserts that there are submissions that need grading for the quiz.
     */
    fun assertNeedsGrading() {
        val resources = InstrumentationRegistry.getTargetContext()
        ungradedDonut.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_needs_grading).format(1, 1))
    }

    /**
     * Asserts that there are submissions that have not been submitted for the quiz.
     */
    fun assertNotSubmitted() {
        val resources = InstrumentationRegistry.getTargetContext()
        notSubmittedDonut.assertHasContentDescription(resources.getString(R.string.content_description_submission_donut_unsubmitted).format(1, 1))
    }

    /**
     * Asserts that the quiz title has changed to the specified new quiz title.
     *
     * @param newQuizTitle The new quiz title to assert.
     */
    fun assertQuizTitleChanged(newQuizTitle: String) {
        quizTitleTextView.assertHasText(newQuizTitle)
    }

    /**
     * Asserts that the access code has changed to the specified new code.
     *
     * @param newCode The new access code to assert.
     */
    fun assertAccessCodeChanged(newCode: String) {
        //TODO: accessCodeTextView.assertHasText(newCode)
    }

    /**
     * Waits for the page to finish rendering.
     */
    fun waitForRender() {
        waitForView(withId(R.id.quizDetailsPage))
    }

    /**
     * Performs a refresh action on the page.
     */
    fun refresh() {
        waitForView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Asserts that the quiz is unpublished.
     */
    fun assertQuizUnpublished() {
        onView(withId(R.id.publishStatusTextView)).assertHasText("Unpublished")
    }

    /**
     * Asserts that the quiz is published.
     */
    fun assertQuizPublished() {
        onView(withId(R.id.publishStatusTextView)).assertHasText("Published")
    }
}
