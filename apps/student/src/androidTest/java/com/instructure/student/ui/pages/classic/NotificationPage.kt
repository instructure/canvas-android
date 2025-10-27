/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.refresh
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.RecyclerViewItemCountGreaterThanAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matchers

class NotificationPage : BasePage() {

    fun assertNotificationDisplayed(title: String) {
        val matcher = withText(title)
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertNotificationNotDisplayed(notificationTitle: String) {
        val matcher = allOf(withText(notificationTitle), withAncestor(R.id.listView))
        onView(matcher).check(doesNotExist())
    }

    fun assertHasGrade(title: String, grade: String) {
        val matcher = allOf(containsTextCaseInsensitive(title.dropLast(1)) + hasSibling(withId(R.id.description) + withText("Grade: $grade")))
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertGradeUpdated(title: String) {
        val matcher = allOf(containsTextCaseInsensitive(title.dropLast(1)) + hasSibling(withId(R.id.description) + withText("Grade updated")))
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun assertExcused(title: String) {
        val matcher = allOf(containsTextCaseInsensitive(title.dropLast(1)) + hasSibling(withId(R.id.description) + withText("Excused")))
        onView(matcher).scrollTo().assertDisplayed()
    }

    fun clickNotification(title: String) {
        val matcher = withText(title)
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun assertNotificationWithPoll(title: String, times: Int, pollIntervalSeconds: Long) {
        var iteration = 0
        while (iteration < times) {
            Thread.sleep(pollIntervalSeconds * 1000)
            try {
                val words = title.split(" ")
                onView(containsTextCaseInsensitive(words[0] + " " + words[1] + " " + words[2])).assertDisplayed()
            } catch (e: NoMatchingViewException) {
                iteration++
                refresh()
            }
        }
    }

    fun assertNotificationCountIsGreaterThan(count: Int) {
        val itemMatcher = Matchers.allOf(
            withAncestor(R.id.swipeRefreshLayout),
            hasSibling(withId(R.id.notificationsFragment)),
            withId(R.id.listView)
        )
        onView(itemMatcher).waitForCheck(RecyclerViewItemCountGreaterThanAssertion(count))
    }
}