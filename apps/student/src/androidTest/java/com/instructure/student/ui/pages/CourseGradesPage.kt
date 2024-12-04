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

import android.os.SystemClock.sleep
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.getStringFromResource
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import java.util.concurrent.TimeUnit

class CourseGradesPage : BasePage(R.id.courseGradesPage) {
    private val gradeLabel by WaitForViewWithId(R.id.txtOverallGradeLabel)
    private val gradeValue by WaitForViewWithId(R.id.txtOverallGrade)
    private val baseOnGradedAssignmentsCheckBox by WaitForViewWithId(R.id.showTotalCheckBox)
    private val showWhatIfCheckbox by WaitForViewWithId(R.id.showWhatIfCheckBox)

    private fun scrollToItem(itemMatcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView,itemMatcher)
    }

    fun assertItemDisplayed(itemMatcher: Matcher<View>) {
        scrollToItem(itemMatcher)
        onView(itemMatcher).assertDisplayed()
    }

    fun openAssignment(assignmentName: String) {
        waitForView(withId(R.id.title) + withText(assignmentName)).scrollTo().click()
    }

    fun selectItem(itemMatcher: Matcher<View>) {
        scrollToItem(itemMatcher)
        Espresso.onView(itemMatcher).click()
    }

    fun assertTotalGrade(matcher: Matcher<View>) {
        // Maybe the total grade will take a beat to update properly?
        waitForView(allOf(withId(R.id.txtOverallGrade), matcher))
        gradeValue.check(matches(matcher))
    }

    fun assertAssignmentDueDate(assignmentName: String, dateString: String) {
        val assignmentTitleMatcher = withId(R.id.title) + withParent(R.id.textContainer) + withText(assignmentName) + withAncestor(R.id.courseGradesPage)
        if(dateString != getStringFromResource(R.string.gradesNoDueDate)) onView(withId(R.id.date) + withText(dateString) + hasSibling(assignmentTitleMatcher)).assertDisplayed()
        else onView(withId(R.id.date) + withText(R.string.gradesNoDueDate) + hasSibling(assignmentTitleMatcher)).assertDisplayed()
    }

    fun assertAssignmentStatus(assignmentName: String, status: String) {
        val assignmentTitleMatcher = withId(R.id.title) + withParent(R.id.textContainer) + withText(assignmentName) + withAncestor(R.id.courseGradesPage)
        onView(withId(R.id.submissionState) + withText(status) + hasSibling(assignmentTitleMatcher)).assertDisplayed()
    }

    fun assertEmptyView() {
        onView(withId(R.id.title) + withText(R.string.noItemsToDisplayShort) + withAncestor(R.id.gradesEmptyView)).assertDisplayed()
    }

    fun assertAssignmentDisplayed(name: String, gradeString: String) {
        val siblingMatcher = withId(R.id.title) + withParent(R.id.textContainer) + withText(name) + withAncestor(R.id.courseGradesPage)
        onView(withId(R.id.points) + hasSibling(siblingMatcher)).scrollTo().assertHasText(gradeString)
    }

    // Hopefully this will be sufficient.  We may need to add some logic to scroll
    // to the top of the list first.  We have to use the custom constraints because the
    // swipeRefreshLayout may extend below the screen, and therefore may not be 90% visible.
    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.courseGradesPage), isDisplayed()))
                .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(5)))
        sleep(1000) // Allow some time to react to the update.
    }

    fun swipeUp() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.courseGradesPage), isDisplayed()))
            .perform(withCustomConstraints(ViewActions.swipeUp(), isDisplayingAtLeast(5)))
        sleep(1000) // Allow some time to react to the update.
    }

    fun checkWhatIf() {
        showWhatIfCheckbox.check(matches(isNotChecked())).perform(click())
    }

    fun uncheckWhatIf() {
        showWhatIfCheckbox.check(matches(isChecked())).perform(click())
    }

    fun assertWhatIfChecked() {
        showWhatIfCheckbox.check(matches(isChecked()))
    }

    fun assertWhatIfUnChecked() {
        showWhatIfCheckbox.check(matches(isNotChecked()))
    }

    fun checkBaseOnGradedAssignments() {
        baseOnGradedAssignmentsCheckBox.check(matches(isNotChecked())).perform(click())
    }

    fun uncheckBaseOnGradedAssignments() {
        baseOnGradedAssignmentsCheckBox.check(matches(isChecked())).perform(click())
    }

    fun assertBaseOnGradedAssignmentsChecked() {
        baseOnGradedAssignmentsCheckBox.check(matches(isChecked()))
    }

    fun assertBaseOnGradedAssignmentsUnChecked() {
        baseOnGradedAssignmentsCheckBox.check(matches(isNotChecked()))
    }

    private fun openWhatIfDialog(itemMatcher: Matcher<View>) {
        scrollToItem(itemMatcher)
        val pencilIcon = onView(allOf(withId(R.id.edit), hasSibling(withChild(itemMatcher))))
        pencilIcon.click()
    }

    fun enterWhatIfGrade(itemMatcher: Matcher<View>, whatIfGrade: String) {
        openWhatIfDialog(itemMatcher)
        onView(withId(R.id.currentScore)).typeText(whatIfGrade)
        onView(containsTextCaseInsensitive("done")).click()
    }

    fun assertGradeNotDisplayed(itemMatcher: Matcher<View>) {
        scrollToItem(itemMatcher)
        onView(allOf(withId(R.id.points), hasSibling(itemMatcher))).assertNotDisplayed()
    }

    fun assertGradeDisplayed(itemMatcher: Matcher<View>, gradeMatcher: Matcher<View>) {
        scrollToItem(itemMatcher)
        onView(allOf(withId(R.id.points), hasSibling(itemMatcher), gradeMatcher)).assertDisplayed()
    }

    /**
     * Round 2, FIGHT!
     *
     * Attempt number 2 at getting the grade e2e test passing consistently.
     *
     * IF this works, we'll probs want to try to move it to a bit of a more generic solution that can be re-used.
     */
    fun refreshUntilAssertTotalGrade(matcher: Matcher<View>) {
        val waitTime = TimeUnit.SECONDS.toMillis(10)
        val endTime = System.currentTimeMillis() + waitTime
        val maxApiAttempts = 5
        var currentAttempt = 1
        do {
            try {
                gradeValue.check(matches(matcher))
                break; // or else we spin for 10 secs
            } catch(t: Throwable) {
                if(currentAttempt == maxApiAttempts) {
                    break
                } else {
                    refresh()
                    currentAttempt++
                }
            }
        } while(System.currentTimeMillis() < endTime)
        gradeValue.check(matches(matcher))
    }

    fun clickOnExpandCollapseButton() {
        onView(withId(R.id.expand_collapse) + hasSibling(withId(R.id.title) + withText(R.string.assignments))).click()
    }

    fun assertAssignmentCount(count: Int) {
        onView(withId(R.id.listView) + withAncestor(R.id.courseGradesPage)).check(
            ViewAssertions.matches(
                ViewMatchers.hasChildCount(count + 1) //because of the expandable 'header' we have to increase by 1.
            ))
    }

}