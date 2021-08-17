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
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.*
import com.instructure.pandautils.binding.BindableViewHolder
import com.instructure.student.R

class SchedulePage : BasePage(R.id.schedulePage) {

    private val pager by OnViewWithId(R.id.schedulePager)
    private val previousWeekButton by OnViewWithId(R.id.previousWeekButton)
    private val nextWeekButton by OnViewWithId(R.id.nextWeekButton)
    private val recyclerView by OnViewWithId(R.id.scheduleRecyclerView)
    private val swipeRefreshLayout by OnViewWithId(R.id.scheduleSwipeRefreshLayout)

    fun assertEmptyViewShown() {
        onViewWithText(getStringFromResource(R.string.nothing_planned_yet)).assertDisplayed()
    }

    fun assertDayHeaderShown(dateText: String, dayText: String, position: Int) {
        val dateTextMatcher = withId(R.id.dateText) + withText(dateText)
        val dayTextMatcher = withId(R.id.dayText) + withText(dayText)

        val todayHeaderMatcher = withId(R.id.scheduleHeaderLayout) + withDescendant(dateTextMatcher) + withDescendant(dayTextMatcher)
        recyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
        waitForView(todayHeaderMatcher).assertDisplayed()
    }

    fun assertNoScheduleItemDisplayed() {
        onView(withId(R.id.scheduleCourseItemLayout)).check(ViewAssertions.doesNotExist())
    }

    fun scrollToPosition(position: Int) {
        recyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
    }

    fun assertCourseHeaderDisplayed(courseName: String) {
        onView(withId(R.id.courseName) + withText(courseName)).assertDisplayed()
    }

    fun assertScheduleItemDisplayed(scheduleItemName: String) {
        onView(withAncestor(R.id.plannerItems) + withText(scheduleItemName))
    }

    fun assertMissingItemDisplayed(itemName: String, courseName: String, pointsPossible: String) {
        val titleMatcher = withId(R.id.title) + withText(itemName)
        val courseNameMatcher = withId(R.id.courseName) + withText(courseName)
        val pointsPossibleMatcher = withId(R.id.points) + withText(pointsPossible)

        onView(withId(R.id.missingItemLayout) + withDescendant(titleMatcher) + withDescendant(courseNameMatcher) + withDescendant(pointsPossibleMatcher))
            .assertDisplayed()
    }
}