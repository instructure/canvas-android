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


import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotHasText
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.onViewWithText
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.teacher.R

/**
 * Assignment due dates page
 *
 * @constructor Create empty Assignment due dates page
 */
@Suppress("unused")
class AssignmentDueDatesPage : BasePage(pageResId = R.id.dueDatesPage) {

    private val backButton by OnViewWithContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description)
    private val titleView by OnViewWithText(R.string.page_title_due_dates)
    private val editButton by OnViewWithId(R.id.menu_edit)
    private val recyclerView by WaitForViewWithId(R.id.recyclerView)

    /**
     * Open edit page (by clicking on Edit button).
     *
     */
    fun openEditPage() = editButton.click()

    /**
     * Assert that the 'No Due Date' string is displayed as due date text.
     *
     */
    fun assertDisplaysNoDueDate() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        assertNoAvailabilityDates()
        onViewWithId(R.id.dueForTextView).assertDisplayed().assertHasText(R.string.everyone)
        onViewWithId(R.id.dueDateTextView).assertDisplayed().assertHasText(R.string.no_due_date)
    }

    /**
     * Assert that the due date count is the expected.
     *
     * @param expectedCount: The expected due date count parameter.
     */
    fun assertDueDatesCount(expectedCount: Int) {
        recyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    /**
     * Assert that the due date count is 1.
     *
     */
    fun assertDisplaysSingleDueDate() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        assertNoAvailabilityDates()
        onViewWithId(R.id.dueForTextView).assertDisplayed().assertHasText(R.string.everyone)
        onViewWithId(R.id.dueDateTextView).assertDisplayed().assertNotHasText(R.string.no_due_date)
    }

    /**
     * Assert that the 'Due For' text is the expected.
     *
     * @param dueForString: The expected due date integer parameter.
     */
    fun assertDueFor(dueForString: Int) {
        onView(withId(R.id.dueForTextView) + withText(dueForString)).assertDisplayed()
    }

    /**
     * Assert that the 'Due For' text is the expected.
     *
     * @param dueForString: The expected due date string parameter.
     */
    fun assertDueFor(dueForString: String) {
        onView(withId(R.id.dueForTextView) + withText(dueForString)).assertDisplayed()
    }

    /**
     * Assert that the 'Due Date' text is the expected.
     *
     * @param dueDateString: The expected due date string parameter.
     */
    fun assertDueDateTime(dueDateString: String) {
        onView(withId(R.id.dueDateTextView) + withText(dueDateString))
    }

    /**
     * Assert displays availability dates.
     *
     */
    fun assertDisplaysAvailabilityDates() {
        recyclerView.check(RecyclerViewItemCountAssertion(1))
        assertLabelsDisplayedOnce()
        onViewWithId(R.id.availableFromTextView).assertDisplayed().assertNotHasText(R.string.no_date_filler)
        onViewWithId(R.id.availableToTextView).assertDisplayed().assertNotHasText(R.string.no_date_filler)
    }

    /**
     * Assert that the corresponding labels ('For', 'Available from', 'Available to') are displayed.
     *
     */
    private fun assertLabelsDisplayedOnce() {
        onViewWithText(R.string.details_due_for_label).assertDisplayed()
        onViewWithText(R.string.details_available_from_label).assertDisplayed()
        onViewWithText(R.string.details_available_to_label).assertDisplayed()
    }

    /**
     * Assert that no availability dates given.
     *
     */
    private fun assertNoAvailabilityDates() {
        onViewWithId(R.id.availableFromTextView).assertDisplayed().assertHasText(R.string.no_date_filler)
        onViewWithId(R.id.availableToTextView).assertDisplayed().assertHasText(R.string.no_date_filler)
    }

}
