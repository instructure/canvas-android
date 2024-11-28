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
 *
 */
package com.instructure.teacher.ui.pages

import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf

/**
 * AssignmentListPage represents a page that displays a list of assignments.
 * It provides methods to interact with the assignment list, such as clicking on assignments, performing searches, and asserting the presence of assignments and grading periods.
 *
 * @constructor Creates an instance of the AssignmentListPage.
 */
class AssignmentListPage(val searchable: Searchable) : BasePage() {

    private val assignmentListToolbar by OnViewWithId(R.id.assignmentListToolbar)
    private val assignmentRecyclerView by OnViewWithId(R.id.assignmentRecyclerView)
    private val searchButton by OnViewWithId(R.id.search)
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)
    private val gradingPeriodHeader by WaitForViewWithId(R.id.gradingPeriodContainer)

    /**
     * Clicks on the given assignment.
     *
     * @param assignment The assignment to click on.
     */
    fun clickAssignment(assignment: AssignmentApiModel) {
        waitForViewWithText(assignment.name).click()
    }

    /**
     * Clicks on the given assignment.
     *
     * @param assignment The assignment to click on.
     */
    fun clickAssignment(assignment: Assignment) {
        waitForViewWithText(assignment.name!!).click()
    }

    /**
     * Asserts that the "No Assignments" view is displayed.
     */
    fun assertDisplaysNoAssignmentsView() {
        emptyPandaView.assertDisplayed()
    }

    /**
     * Asserts that the given assignment is present in the list.
     *
     * @param assignment The assignment to check.
     */
    fun assertHasAssignment(assignment: Assignment) {
        assertAssignmentName(assignment.name!!)
    }

    /**
     * Asserts that the given assignment is present in the list.
     *
     * @param assignment The assignment to check.
     */
    fun assertHasAssignment(assignment: AssignmentApiModel) {
        assertAssignmentName(assignment.name)
    }

    /**
     * Asserts that the given assignment is NOT present in the list.
     *
     * @param assignment The assignment to check.
     */
    fun assertAssignmentNotDisplayed(assignment: AssignmentApiModel) {
        onView(withText(assignment.name)).check(DoesNotExistAssertion(10))
    }

    /**
     * Asserts that grading periods are present.
     */
    fun assertHasGradingPeriods() {
        gradingPeriodHeader.assertDisplayed()
    }

    /**
     * Opens the search field.
     */
    fun openSearch() {
        searchButton.click()
    }

    /**
     * Enters the given search query.
     *
     * @param query The search query to enter.
     */
    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    /**
     * Asserts the number of assignments in the list.
     *
     * @param count The expected number of assignments.
     */
    fun assertAssignmentCount(count: Int) {
        assignmentRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    /**
     * Refreshes the assignment list.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    private fun assertAssignmentName(assignmentName: String) {
        waitForView(withText(assignmentName) + withAncestor(R.id.assignmentLayout)).assertDisplayed()
    }

    /**
     * Asserts the "needs grading" count of the given assignment.
     *
     * @param assignmentName The name of the assignment to check.
     * @param needsGradingCount The expected "needs grading" count.
     */
    fun assertNeedsGradingCountOfAssignment(assignmentName: String, needsGradingCount: Int) {
        onView(withId(R.id.ungradedCount) + withText("$needsGradingCount needs grading") + hasSibling(withId(R.id.assignmentTitle) + withText(assignmentName))).assertDisplayed()
    }

    /**
     * Asserts that the given assignment status is published (so the published icon is displayed).
     *
     * @param assignmentName The name of the assignment to check.
     */
    fun assertAssignmentPublished(assignmentName: String) {
        onView(allOf(withId(R.id.publishedStatusIcon), withContentDescription(R.string.published),
            withParent(hasSibling(withChild(withText(assignmentName) + withId(R.id.assignmentTitle))
            )))).assertDisplayed()
    }

    /**
     * Asserts that the given assignment status is unpublished (so the unpublished icon is displayed).
     *
     * @param assignmentName The name of the assignment to check.
     */
    fun assertAssignmentUnPublished(assignmentName: String) {
        onView(allOf(withId(R.id.publishedStatusIcon), withContentDescription(R.string.not_published),
            withParent(hasSibling(withChild(withText(assignmentName) + withId(R.id.assignmentTitle))
            )))).assertDisplayed()
    }
}
