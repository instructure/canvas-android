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
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.stringContainsTextCaseInsensitive
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.canvasapi2.models.Assignment
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

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

    fun assertAssignmentLocked() {
        onView(withId(R.id.lockMessageTextView)).assertDisplayed()
        onView(withId(R.id.lockMessageTextView)).check(matches(containsTextCaseInsensitive("this assignment is locked")))
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
        onView(withId(R.id.gradeCell)).click()
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

