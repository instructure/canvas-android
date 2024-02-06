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
package com.instructure.student.ui.pages

import android.view.View
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.stringContainsTextCaseInsensitive
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getPluralFromResource
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.swipeUp
import com.instructure.espresso.typeText
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything

open class AssignmentDetailsPage : BasePage(R.id.assignmentDetailsPage) {
    val toolbar by OnViewWithId(R.id.toolbar)
    val points by OnViewWithId(R.id.points)
    val date by OnViewWithId(R.id.dueDateTextView)
    val submissionTypes by OnViewWithId(R.id.submissionTypesTextView)

    fun assertDisplayToolbarTitle() {
        onView(allOf(withText(R.string.assignmentDetails), withParent(R.id.toolbar))).assertDisplayed()
    }

    fun assertDisplayToolbarSubtitle(courseNameText: String) {
        onView(allOf(withText(courseNameText), withParent(R.id.toolbar))).assertDisplayed()
    }

    fun assertDisplaysDate(dateText: String) {
        date.assertHasText(dateText)
    }

    fun assertAssignmentDetails(assignment: Assignment) {
        onView(withId(R.id.assignmentName)).assertHasText(assignment.name!!)
        onView(allOf(withId(R.id.points), isDisplayed()))
                .check(matches(containsTextCaseInsensitive(assignment.pointsPossible.toInt().toString())))
    }

    fun assertAssignmentTitle(assignmentName: String) {
        onView(withId(R.id.assignmentName)).assertHasText(assignmentName)
    }

    fun assertAssignmentSubmitted() {
        onView(withText(R.string.submissionStatusSuccessTitle)).scrollTo().assertDisplayed()
        onView(allOf(withId(R.id.submissionStatus), withText(R.string.submitted))).scrollTo().assertDisplayed()
    }

    fun assertAssignmentGraded(score: String) {
        onView(withId(R.id.gradeCell)).scrollTo().assertDisplayed()
        onView(withId(R.id.score)).scrollTo().assertContainsText(score)
        onView(allOf(withId(R.id.submissionStatus), withText(R.string.gradedSubmissionLabel))).scrollTo().assertDisplayed()
    }

    fun assertGradeDisplayed(grade: String) {
        onView(withId(R.id.gradeCell)).scrollTo().assertDisplayed()
        onView(withId(R.id.grade)).scrollTo().assertContainsText(grade)
    }

    fun assertGradeNotDisplayed() {
        onView(withId(R.id.grade)).assertNotDisplayed()
    }

    fun assertOutOfTextDisplayed(outOfText: String) {
        onView(withId(R.id.outOf)).scrollTo().assertContainsText(outOfText)
    }

    fun assertOutOfTextNotDisplayed() {
        onView(withId(R.id.outOf)).assertNotDisplayed()
    }

    fun assertScoreDisplayed(score: String) {
        onView(withId(R.id.score)).scrollTo().assertContainsText(score)
    }

    fun assertScoreNotDisplayed() {
        onView(withId(R.id.score)).assertNotDisplayed()
    }

    fun assertAssignmentLocked() {
        if(CanvasTest.isLandscapeDevice()) onView(withId(R.id.swipeRefreshLayout) + withAncestor(R.id.assignmentDetailsPage)).swipeUp()
        onView(withId(R.id.lockedMessageTextView)).assertDisplayed()
        onView(withId(R.id.lockedMessageTextView)).check(matches(containsTextCaseInsensitive("this assignment is locked by the module")))
    }

    fun refresh() {
        // Scroll up to the top in case we are not there already.
        onView(allOf(isAssignableFrom(ScrollView::class.java), isDisplayed())).perform(ScrollToTop())

        // Now swipe down -- twice, just for good measure (may update twice)
        onView(allOf(withId(R.id.swipeRefreshLayout),  isDisplayed())).swipeDown().swipeDown()
    }

    fun waitForSubmissionComplete() {
        waitForViewWithText(R.string.submissionStatusSuccessTitle)
    }

    fun goToSubmissionDetails() {
        onView(withId(R.id.gradeCell)).scrollTo().click()
    }

    fun assertSubmissionAndRubricLabel() {
        onView(allOf(withId(R.id.submissionAndRubricLabel), withText(R.string.submissionAndRubric))).assertDisplayed()
    }

    private fun assertStatus(statusResourceId: Int) {
        onView(withId(R.id.submissionStatus)).waitForCheck(matches(withText(statusResourceId)))
    }

    fun assertStatusSubmitted() {
        assertStatus(R.string.submitted)
    }

    fun assertStatusNotSubmitted() {
        assertStatus(R.string.notSubmitted)
    }

    fun assertStatusMissing() {
        assertStatus(R.string.missingAssignment)
    }

    fun viewQuiz() {
        onView(withId(R.id.submitButton)).assertHasText(R.string.viewQuiz).click()
    }

    fun clickSubmit() {
        onView(withId(R.id.submitButton)).click()
    }

    fun scrollToAssignmentDescription() {
        Thread.sleep(3000)
        waitForMatcherWithSleeps(withId(R.id.contentWebView), timeout = 30000, pollInterval = 1000).scrollTo()
    }

    fun addBookmark(bookmarkName: String) {
        openOverflowMenu()
        Espresso.onView(withText("Add Bookmark")).click()
        Espresso.onView(withId(R.id.bookmarkEditText)).clearText()
        Espresso.onView(withId(R.id.bookmarkEditText)).typeText(bookmarkName)
        if(CanvasTest.isLandscapeDevice()) Espresso.pressBack()
        Espresso.onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("Save"))).click()
    }

    fun openOverflowMenu() {
        Espresso.onView(
            allOf(
                ViewMatchers.withContentDescription(stringContainsTextCaseInsensitive("More options")),
                isDisplayed()
            )).click()
    }

    fun assertDisplaysAddBookmarkButton() {
        onViewWithText(R.string.addBookmark).assertDisplayed()
    }

    fun assertSelectedAttempt(attemptNumber: Int) {
        assertAttemptInformation()
        onView(allOf(withId(R.id.attemptTitle), withAncestor(withId(R.id.attemptSpinner)), withText("Attempt $attemptNumber"))).assertDisplayed()
    }

    fun assertNoAttemptSpinner() {
        onView(allOf(withId(R.id.attemptTitle), withAncestor(withId(R.id.attemptSpinner)))).check(doesNotExist())
        onView(allOf(withId(R.id.attemptDate), withAncestor(withId(R.id.attemptSpinner)))).check(doesNotExist())
    }

    fun assertAttemptSpinnerDisplayed() {
        onView(withId(R.id.attemptSpinner)).assertDisplayed()
    }

    fun selectAttempt(attemptNumber: Int) {
        onView(withId(R.id.attemptSpinner)).click()
        onView(allOf(withId(R.id.attemptTitle), withText("Attempt $attemptNumber"))).click()
    }

    private fun assertAttemptInformation() {
        waitForView(allOf(withId(R.id.attemptTitle), withAncestor(withId(R.id.attemptSpinner)))).assertDisplayed()
        waitForView(allOf(withId(R.id.attemptDate), withAncestor(withId(R.id.attemptSpinner)))).assertDisplayed()
    }

    fun selectSubmissionType(submissionType: SubmissionType) {
        val viewMatcher = when (submissionType) {
            SubmissionType.ONLINE_TEXT_ENTRY -> withId(R.id.submissionEntryText)
            SubmissionType.ONLINE_UPLOAD -> withId(R.id.submissionEntryFile)
            SubmissionType.ONLINE_URL -> withId(R.id.submissionEntryWebsite)
            SubmissionType.MEDIA_RECORDING -> withId(R.id.submissionEntryMedia)

            else -> {withId(R.id.submissionEntryText)}
        }

        onView(viewMatcher).click()
    }

    fun assertSubmissionTypeDisplayed(submissionType: String) {
        onView(withText(submissionType) + withAncestor(R.id.customPanel)).assertDisplayed()
    }

    fun assertReminderSectionNotDisplayed() {
        onView(withId(R.id.reminderTitle)).assertNotDisplayed()
        onView(withId(R.id.reminderDescription)).assertNotDisplayed()
        onView(withId(R.id.reminderAdd)).assertNotDisplayed()
    }

    fun assertReminderSectionDisplayed() {
        onView(withId(R.id.reminderTitle)).scrollTo().assertDisplayed()
        onView(withId(R.id.reminderDescription)).scrollTo().assertDisplayed()
        onView(withId(R.id.reminderAdd)).scrollTo().assertDisplayed()
    }

    fun clickAddReminder() {
        onView(withId(R.id.reminderAdd)).scrollTo().click()
    }

    fun clickOneHourBefore() {
        onView(
            withText(
                getStringFromResource(
                    R.string.reminderBefore,
                    getPluralFromResource(R.plurals.reminderHour, 1, 1)
                )
            )
        ).scrollTo().click()
    }

    fun assertReminderDisplayedWithText(text: String) {
        onView(withText(text)).scrollTo().assertDisplayed()
    }

    fun removeReminderWithText(text: String) {
        onView(
            allOf(
                withId(R.id.remove),
                hasSibling(withText(text))
            )
        ).click()
        onView(withText(R.string.yes)).scrollTo().click()
    }

    fun assertReminderNotDisplayedWithText(text: String) {
        onView(withText(text)).check(doesNotExist())
    }

    fun clickCustom() {
        onData(anything()).inRoot(isDialog()).atPosition(6).perform(click())
    }

    fun fillQuantity(quantity: String) {
        onView(withId(R.id.quantity)).scrollTo().typeText(quantity)
        Espresso.closeSoftKeyboard()
    }

    fun clickHoursBefore() {
        onView(withId(R.id.hours)).scrollTo().click()
    }

    fun clickDone() {
        onView(withText(R.string.done)).click()
    }
}

/**
 * Custom view action to scroll to top of scrollview.
 * TODO: Move this to a central location, as we will probably want to reuse it.
 */
class ScrollToTop : ViewAction {
    override fun getDescription(): String {
        return "Scroll to top of scroll view"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(ScrollView::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        when(view) {
            is ScrollView -> view.smoothScrollTo(0,0)
        }
    }

}

