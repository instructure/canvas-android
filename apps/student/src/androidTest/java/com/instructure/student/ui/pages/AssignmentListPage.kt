/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.waitForMatcherWithRefreshes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class AssignmentListPage : BasePage(pageResId = R.id.assignmentListPage) {

    private val assignmentListToolbar by OnViewWithId(R.id.toolbar)
    private val gradingPeriodHeader by WaitForViewWithId(R.id.termSpinnerLayout)
    private val sortByButton by OnViewWithId(R.id.sortByButton)
    private val sortByTextView by OnViewWithId(R.id.sortByTextView)

    // Only displayed when assignment list is empty
    private val emptyView by WaitForViewWithId(R.id.emptyView, autoAssert = false)

    // Only displayed when there are no assignments
    private val emptyText by WaitForViewWithText(R.string.noItemsToDisplayShort, autoAssert = false)

    fun clickAssignment(assignment: AssignmentApiModel) {
        waitForViewWithText(assignment.name).click()
    }

    fun clickAssignment(assignment: Assignment) {
        scrollRecyclerView(R.id.listView, withText(assignment.name!!))
        waitForViewWithText(assignment.name!!).click()
    }

    fun clickQuiz(quiz: QuizApiModel) {
        waitForViewWithText(quiz.title).click()
    }

    fun assertDisplaysNoAssignmentsView() {
        emptyView.assertDisplayed()
    }

    fun assertHasAssignment(assignment: AssignmentApiModel, expectedGrade: String? = null) {
        assertHasAssignmentCommon(assignment.name, assignment.dueAt, expectedGrade)
    }

    fun assertHasAssignment(assignment: Assignment, expectedGrade: String? = null) {
        assertHasAssignmentCommon(assignment.name!!, assignment.dueAt, expectedGrade)
    }

    fun clickOnSearchButton() {
        onView(withId(R.id.search)).click()
    }

    fun typeToSearchBar(textToType: String) {
        waitForViewWithId(R.id.search_src_text).replaceText(textToType)
    }

    fun assertAssignmentNotDisplayed(assignmentName: String) {
        onView(withText(assignmentName) + withId(R.id.title) + hasSibling(withId(R.id.description))).check(doesNotExist())
    }

    fun assertAssignmentItemCount(expectedItemCount: Int, groupCount: Int) {
        Espresso.onView(allOf(withId(R.id.listView) + ViewMatchers.withParent(withId(R.id.swipeRefreshLayout)))).waitForCheck(RecyclerViewItemCountAssertion(expectedItemCount + groupCount))
    }

    fun assertAssignmentGroupDisplayed(groupName: String) {
        onView(withId(R.id.title) + withText(groupName) + hasSibling(withId(R.id.expand_collapse))).scrollTo().assertDisplayed()
    }

    fun expandCollapseAssignmentGroup(groupName: String) {
        onView(withId(R.id.expand_collapse) + hasSibling(withId(R.id.title) + withText(groupName))).scrollTo().click()
    }

    private fun assertHasAssignmentCommon(assignmentName: String, assignmentDueAt: String?, expectedGrade: String? = null) {
        waitForMatcherWithRefreshes(withText(assignmentName))
        waitForView(allOf(withText(assignmentName), isDescendantOfA(withId(R.id.assignmentListPage)))).assertDisplayed()

        // Check that either the assignment due date is present, or "No Due Date" is displayed
        if(assignmentDueAt != null) {
            val matcher = allOf(
                    withText(containsString("Due: ")),
                    withId(R.id.date),
                    withParent(withParent(withChild(withText(assignmentName)))))
            scrollToAndAssertDisplayed(matcher)
        }
        else {
            val matcher = allOf(
                    withText(R.string.toDoNoDueDate),
                    withId(R.id.date),
                    withParent(withParent(withChild(withText(assignmentName)))))
            scrollToAndAssertDisplayed(matcher)
        }

        // Check that grade is present, if that is specified
        if(expectedGrade != null) {
            val matcher =  allOf(
                    withText(containsString(expectedGrade)), // grade might be "13", total string "13/15"
                    withId(R.id.points),
                    withParent(withParent(withChild(withText(assignmentName)))))
            scrollToAndAssertDisplayed(matcher)
        }

    }

    fun assertQuizDisplayed(quiz: QuizApiModel, gradePortion: String? = null) {
        scrollToAndAssertDisplayed(withText(quiz.title))
        if(gradePortion != null) {
            val matcher = allOf(
                    withText(containsString(gradePortion)), // grade might be "13", total string "13/15"
                    withId(R.id.points),
                    withParent(withParent(withChild(withText(quiz.title))))
            )
            scrollToAndAssertDisplayed(matcher)
        }
    }

    fun assertQuizNotDisplayed(quiz: QuizApiModel) {
        onView(withText(quiz.title)).check(doesNotExist())
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed())).swipeDown()
    }

    private fun scrollToAndAssertDisplayed(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertHasGradingPeriods() {
        gradingPeriodHeader.assertDisplayed()
    }

    fun assertSortByButtonShowsSortByTime() {
        sortByTextView.check(matches(withText(R.string.sortByTime)))
    }

    fun assertSortByButtonShowsSortByType() {
        sortByTextView.check(matches(withText(R.string.sortByType)))
    }

    fun assertFindsUndatedAssignmentLabel() {
        onView(withText(R.string.undatedAssignments)).assertVisible()
    }

    fun selectSortByType() {
        sortByButton.click()
        onView(withText(R.string.sortByDialogTypeOption)).click()
    }

    fun clickFilterMenu() {
        onView(withId(R.id.menu_filter_assignments)).click()
    }

    fun filterAssignments(filterType: AssignmentType) {
        onView(withText(filterType.assignmentType) + withParent(withId(R.id.select_dialog_listview))).click()
    }

    fun waitForPage() {
        waitForView(withText(R.string.assignments))
    }

    enum class AssignmentType(val assignmentType: Int) {
        ALL(R.string.filterAssignmentAll),
        LATE(R.string.filterAssignmentLate),
        MISSING(R.string.filterAssignmentMissing),
        GRADED(R.string.filterAssignmentGraded),
        UPCOMING(R.string.filterAssignmentUpcoming)
    }
}
