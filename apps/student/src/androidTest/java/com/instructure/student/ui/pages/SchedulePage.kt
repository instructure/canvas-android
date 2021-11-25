/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.pandautils.binding.BindableViewHolder
import com.instructure.student.R

class SchedulePage : BasePage(R.id.schedulePage) {

    private val pager by OnViewWithId(R.id.schedulePager)
    private val previousWeekButton by OnViewWithId(R.id.previousWeekButton)
    private val nextWeekButton by OnViewWithId(R.id.nextWeekButton)
    private val recyclerView by OnViewWithId(R.id.scheduleRecyclerView)
    private val swipeRefreshLayout by OnViewWithId(R.id.scheduleSwipeRefreshLayout)

    fun assertDayHeaderShown(dateText: String, dayText: String, position: Int, recyclerViewMatcherText: String? = null) {
        val dateTextMatcher = withId(R.id.dateText) + withText(dateText)
        val dayTextMatcher = withId(R.id.dayText) + withText(dayText)

        val todayHeaderMatcher = withId(R.id.scheduleHeaderLayout) + withDescendant(dateTextMatcher) + withDescendant(dayTextMatcher)
        if (recyclerViewMatcherText != null) {
            val recyclerViewInteraction = onView(withId(R.id.scheduleRecyclerView) + withDescendant(withText(recyclerViewMatcherText)))
            recyclerViewInteraction.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
        } else {
            recyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
        }
        waitForView(todayHeaderMatcher).assertDisplayed()
    }

    fun assertNoScheduleItemDisplayed() {
        onView(withId(R.id.scheduleCourseItemLayout)).check(ViewAssertions.doesNotExist())
    }

    fun scrollToPosition(position: Int) {
        recyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
    }

    fun assertCourseHeaderDisplayed(courseName: String) {
        onView(withId(R.id.scheduleCourseHeaderText) + withText(courseName)).assertDisplayed()
    }

    fun assertScheduleItemDisplayed(scheduleItemName: String) {
        onView(withAncestor(R.id.plannerItems) + withText(scheduleItemName)).assertDisplayed()
    }

    fun assertMissingItemDisplayed(itemName: String, courseName: String, pointsPossible: String) {
        val titleMatcher = withId(R.id.title) + withText(itemName)
        val courseNameMatcher = withId(R.id.courseName) + withText(courseName)
        val pointsPossibleMatcher = withId(R.id.points) + withText(pointsPossible)

        onView(withId(R.id.missingItemLayout) + withDescendant(titleMatcher) + withDescendant(courseNameMatcher) + withDescendant(pointsPossibleMatcher))
            .assertDisplayed()
    }

    fun refresh() {
        swipeRefreshLayout.swipeDown()
    }

    fun previousWeekButtonClick() {
        previousWeekButton.click()
    }

    fun swipeRight() {
        pager.swipeRight()
    }

    fun nextWeekButtonClick() {
        nextWeekButton.click()
    }

    fun swipeLeft() {
        pager.swipeLeft()
    }

    fun swipeUp() {
        swipeRefreshLayout.swipeUp()
    }

    fun assertTodayButtonDisplayed() {
        onView(withId(R.id.todayButton)).assertDisplayed()
    }

    fun clickOnTodayButton() {
        onView(withId(R.id.todayButton)).click()
    }

    fun clickCourseHeader(courseName: String) {
        onView(withId(R.id.scheduleCourseHeaderText) + withText(courseName)).click()
    }

    fun clickScheduleItem(name: String) {
        onView(withAncestor(R.id.plannerItems) + withText(name)).click()
    }

    fun clickDoneCheckbox() {
        onView(withId(R.id.checkbox)).click()
    }

    fun assertMarkedAsDoneShown() {
        onViewWithText(R.string.schedule_marked_as_done).assertDisplayed()
    }

    fun assertMarkedAsDoneNotShown() {
        onViewWithText(R.string.schedule_marked_as_done).assertNotDisplayed()
    }
}