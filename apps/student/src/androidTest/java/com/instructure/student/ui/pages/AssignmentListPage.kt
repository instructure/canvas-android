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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.student.R
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class AssignmentListPage : BasePage(pageResId = R.id.assignmentListPage) {

    private val assignmentListToolbar by OnViewWithId(R.id.toolbar)

    // Only displayed when assignment list is empty
    private val emptyView by WaitForViewWithId(R.id.emptyView, autoAssert = false)

    // Only displayed when there are grading periods
    private val gradingPeriodHeader by WaitForViewWithId(R.id.termSpinnerLayout, autoAssert = false)

    // Only displayed when there are no assignments
    private val emptyText by WaitForViewWithText(R.string.noItemsToDisplayShort, autoAssert = false)

    fun clickAssignment(assignment: AssignmentApiModel) {
        waitForViewWithText(assignment.name).click()
    }

    fun assertDisplaysNoAssignmentsView() {
        emptyView.assertDisplayed()
    }

    fun assertHasAssignment(assignment: AssignmentApiModel, expectedGrade: String? = null) {

        waitForViewWithText(assignment.name).assertDisplayed()

        // Check that either the assignment due date is present, or "No Due Date" is displayed
        if(assignment.dueAt != null) {
            val matcher = allOf(
                    withText(containsString("Due: ")),
                    withId(R.id.date),
                    withParent(withParent(withChild(withText(assignment.name)))))
            scrollToAndAssertDisplayed(matcher)
        }
        else {
            val matcher = allOf(
                    withText(R.string.toDoNoDueDate),
                    withId(R.id.date),
                    withParent(withParent(withChild(withText(assignment.name)))))
            scrollToAndAssertDisplayed(matcher)
        }

        // Check that grade is present, if that is specified
        //println("expectedGrade=$expectedGrade")
        if(expectedGrade != null) {
            val matcher =  allOf(
                    withText(containsString(expectedGrade)), // grade might be "13", total string "13/15"
                    withId(R.id.points),
                    withParent(withParent(withChild(withText(assignment.name)))))
            scrollToAndAssertDisplayed(matcher)
        }
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed())).swipeDown()
    }

    private fun scrollToAndAssertDisplayed(matcher: Matcher<View>) {
        onView(allOf(withId(R.id.listView), ViewMatchers.isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))
                .assertDisplayed()
    }

    fun assertHasGradingPeriods() {
        gradingPeriodHeader.assertDisplayed()
    }
}
