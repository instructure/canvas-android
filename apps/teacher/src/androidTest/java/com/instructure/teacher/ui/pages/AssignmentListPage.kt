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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

class AssignmentListPage : BasePage() {

    private val assignmentListToolbar by OnViewWithId(R.id.assignmentListToolbar)

    private val assignmentRecyclerView by OnViewWithId(R.id.assignmentRecyclerView)

    private val searchButton by OnViewWithId(R.id.search)

    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    //Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    //Only displayed when there are grading periods
    private val gradingPeriodHeader by WaitForViewWithId(R.id.gradingPeriodContainer)

    fun clickAssignment(assignment: AssignmentApiModel) {
        waitForViewWithText(assignment.name).click()
    }

    fun clickAssignment(assignment: Assignment) {
        waitForViewWithText(assignment.name!!).click()
    }

    fun assertDisplaysNoAssignmentsView() {
        emptyPandaView.assertDisplayed()
    }

    fun assertHasAssignment(assignment: Assignment) {
        waitForViewWithText(assignment.name!!).assertDisplayed()
    }

    fun assertHasGradingPeriods() {
        gradingPeriodHeader.assertDisplayed()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertAssignmentCount(count: Int) {
        assignmentRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }
}
