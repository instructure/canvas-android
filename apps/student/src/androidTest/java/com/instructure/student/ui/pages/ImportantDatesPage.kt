/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.ui.pages

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.RecyclerViewItemCountGreaterThanAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasChild
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.swipeUp
import com.instructure.pandautils.binding.BindableViewHolder
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers


class ImportantDatesPage : BasePage(R.id.importantDatesPage) {

    private val importantDatesRecyclerView by OnViewWithId(R.id.importantDatesRecyclerView)
    private val importantDatesEmptyView by OnViewWithId(R.id.importantDatesEmptyView, autoAssert = false)

    fun assertItemDisplayed(itemName: String) {
        val itemMatcher = withAncestor(R.id.importantDatesRecyclerView) + withText(itemName)
        scrollToItem(R.id.importantDatesRecyclerView, itemName, itemMatcher)
        waitForView(itemMatcher).assertDisplayed()
    }

    fun assertItemNotDisplayed(itemName: String) {
        onView(ViewMatchers.withText(itemName)).check(ViewAssertions.doesNotExist())
    }

    fun assertEmptyViewDisplayed() {
        importantDatesEmptyView.assertDisplayed().assertDisplayed()
    }

    fun pullToRefresh() {
        onView(withId(R.id.importantDatesRecyclerView)).swipeDown()
    }

    fun clickImportantDatesItem(title: String) {
        waitForView(withAncestor(R.id.importantDatesRecyclerView) + withText(title)).click()
    }

    fun assertRecyclerViewItemCount(expectedCount: Int) {
        importantDatesRecyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    fun assertDayTextIsDisplayed(dayText: String) {
        importantDatesRecyclerView.assertHasChild(withText(dayText))
    }

    fun swipeUp() {
        importantDatesRecyclerView.swipeUp()
    }

    fun scrollToPosition(position: Int) {
        importantDatesRecyclerView.perform(RecyclerViewActions.scrollToPosition<BindableViewHolder>(position))
    }

    fun scrollToItem(parentItemId: Int, itemName: String, target: Matcher<View>? = null) {
        var i: Int = 0
        while (true) {
            scrollToPosition(i)
            Thread.sleep(500)
            try {
                if(target == null) onView(withAncestor(parentItemId) + withText(itemName)).scrollTo()
                else onView(target + withText(itemName)).scrollTo()
                break
            } catch(e: NoMatchingViewException) {
                i++
            }
        }
    }

}