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

import android.view.View
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.swipeLeft
import com.instructure.espresso.swipeRight
import com.instructure.espresso.swipeUp
import com.instructure.pandautils.binding.BindableViewHolder
import com.instructure.student.R
import org.hamcrest.Matcher

class SchedulePage : BasePage(R.id.schedulePage) {

    private val pager by OnViewWithId(R.id.schedulePager)
    private val previousWeekButton by OnViewWithId(R.id.previousWeekButton)
    private val nextWeekButton by OnViewWithId(R.id.nextWeekButton)
    private val recyclerView by OnViewWithId(R.id.scheduleRecyclerView)
    private val swipeRefreshLayout by OnViewWithId(R.id.scheduleSwipeRefreshLayout)

    fun assertDayHeaderShownByPosition(dateText: String, dayText: String, position: Int, recyclerViewMatcherText: String? = null) {
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

    fun assertDayHeaderShownByItemName(dateText: String, dayText: String, itemName: String) {
        val dateTextMatcher = withId(R.id.dateText) + withText(dateText)
        val dayTextMatcher = withId(R.id.dayText) + withText(dayText)

        val dayHeaderMatcher = withId(R.id.scheduleHeaderLayout) + withDescendant(dateTextMatcher) + withDescendant(dayTextMatcher)

        scrollToItem(R.id.scheduleHeaderLayout, itemName)
        waitForView(dayHeaderMatcher).assertDisplayed()
    }

    fun assertNoScheduleItemDisplayed() {
        onView(withId(R.id.scheduleCourseItemLayout)).check(ViewAssertions.doesNotExist())
    }

    fun assertNothingPlannedYetDisplayed() {
        onViewWithText(R.string.nothing_planned_yet).assertDisplayed()
    }

    fun scrollToPosition(position: Int) {
        recyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
    }

     fun assertIfCourseHeaderAndScheduleItemDisplayed(courseName: String, assignmentName: String) {
        scrollToItem(R.id.scheduleCourseItemLayout, courseName)
        assertCourseHeaderDisplayed(courseName)
        scrollToItem(R.id.title, assignmentName, withAncestor(R.id.plannerItems))
        assertScheduleItemDisplayed(assignmentName)
    }

    fun scrollToItem(itemId: Int, itemName: String, target: Matcher<View>? = null) {
        val matcher = if (target == null) {
            withParent(itemId) + withText(itemName)
        } else {
            target + withText(itemName)
        }

        var i = 0
        while (i <= 200) {
            try {
                recyclerView.perform(
                    RecyclerViewActions.scrollToPosition<BindableViewHolder>(i)
                )
                onView(matcher).perform(scrollTo())
                waitForView(matcher).assertDisplayed()
                return
            } catch (_: Exception) {
                i += 3
            }
        }

        throw AssertionError("Item not found: $itemName")
    }

    fun assertCourseHeaderDisplayed(courseName: String) {
        waitForView(withId(R.id.scheduleCourseHeaderText) + withText(courseName)).scrollTo().assertDisplayed()
    }

    fun assertScheduleItemDisplayed(scheduleItemName: String) {
        waitForView(withAncestor(R.id.plannerItems) + withText(scheduleItemName)).assertDisplayed()
    }

    fun assertMissingItemDisplayedOnPlannerItem(itemName: String, courseName: String, pointsPossible: String) {
        val titleMatcher = withId(R.id.title) + withText(itemName)
        val courseNameMatcher = withId(R.id.scheduleCourseHeaderText) + withText(courseName)
        val pointsPossibleMatcher = withId(R.id.points) + withText(pointsPossible)

        onView(withId(R.id.plannerItems) + hasSibling(courseNameMatcher) + withDescendant(titleMatcher)  + withDescendant(pointsPossibleMatcher) + withDescendant(withText(R.string.missingAssignment)))
            .scrollTo()
            .assertDisplayed()
    }

    fun assertMissingItemDisplayedInMissingItemSummary(itemName: String, courseName: String, pointsPossible: String) {
        val titleMatcher = withId(R.id.title) + withText(itemName)
        val courseNameMatcher = withId(R.id.courseName) + withText(courseName)
        val pointsPossibleMatcher = withId(R.id.points) + withText(pointsPossible)

        onView(withId(R.id.missingItemLayout) + withDescendant(courseNameMatcher) + withDescendant(titleMatcher)  + withDescendant(pointsPossibleMatcher))
            .scrollTo()
            .assertDisplayed()
    }

    fun refresh() {
        swipeRefreshLayout.swipeDown()
    }

    fun swipeDown() {
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
        onView(withId(R.id.scheduleCourseHeaderText) + withText(courseName)).scrollTo().click()
    }

    fun clickScheduleItem(name: String) {
        onView(withAncestor(R.id.plannerItems) + withText(name)).scrollTo().click()
    }

    fun clickDoneCheckbox() {
        waitForView(withId(R.id.checkbox)).click()
    }

    fun assertMarkedAsDoneShown() {
        waitForViewWithText(R.string.schedule_marked_as_done).assertDisplayed()
    }

    fun assertMarkedAsDoneNotShown() {
        onViewWithText(R.string.schedule_marked_as_done).assertNotDisplayed()
    }
}