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

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.holders.CourseBrowserViewHolder
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import java.lang.RuntimeException

class CourseBrowserPage : BasePage() {

    // TODO: Add recycler view scrolling to support small screen size devices.
    private val courseBrowserRecyclerView by WaitForViewWithId(R.id.courseBrowserRecyclerView)
    private val courseImage by OnViewWithId(R.id.courseImage)
    private val courseTitle by OnViewWithId(R.id.courseBrowserTitle)
    private val courseSubtitle by OnViewWithId(R.id.courseBrowserSubtitle)
    private val courseSettingsMenuButton by OnViewWithId(R.id.menu_course_browser_settings)

    fun openAssignmentsTab() {
        scrollOpen("Assignments")
    }

    private fun scrollDownToCourseBrowser(position: Int = 10)
    {
        /* The course browser RecyclerView is inside a CoordinatorLayout and is therefore only partially
           visible, causing some clicks to fail. We need to perform a swipe up first to make it fully visible. */
        Espresso.onView(ViewMatchers.withId(android.R.id.content)).perform(ViewActions.swipeUp())
        Espresso.onView(ViewMatchers.withId(R.id.courseBrowserRecyclerView))
                .perform(scrollToPosition<CourseBrowserViewHolder>(position))

    }
    fun openQuizzesTab() {
        scrollDownToCourseBrowser()
        waitForViewWithText(R.string.tab_quizzes).click()
    }

    fun openDiscussionsTab() {
        scrollOpen(textName = "Discussions", scrollPosition = 1)
    }

    fun openAnnouncementsTab() {
        scrollOpen("Announcements")
    }

    fun openPeopleTab() {
        scrollDownToCourseBrowser()
        waitForViewWithText("People").click()
    }

    fun clickSettingsButton() {
        courseSettingsMenuButton.click()
    }

    /**
     * Taken from https://stackoverflow.com/questions/37736616/espresso-how-to-find-a-specific-item-in-a-recycler-view-order-is-random
     *
     * This allows us to match a specific view with specific text in a specific RecyclerView.Holder
     */
    fun withTitle(title: String): Matcher<RecyclerView.ViewHolder> =
            object: BoundedMatcher<RecyclerView.ViewHolder, CourseBrowserViewHolder>(CourseBrowserViewHolder::class.java) {
                override fun matchesSafely(item: CourseBrowserViewHolder?): Boolean {
                    return item?.let {
                        it.labelText.text.toString().equals(title, true)
                    } ?: false
                }

                override fun describeTo(description: Description?) {
                    description?.appendText("view holder with title: " + title)
                }
            }

    fun openPagesTab() {
        scrollDownToCourseBrowser()
        waitForViewWithText(R.string.tab_pages).click()
    }

    fun openSyllabus() {
        scrollDownToCourseBrowser()
        waitForViewWithText("Syllabus").click()
    }

    fun openModulesTab() {
        //modules sits at the end of the list, so on smaller resolutions it may be necessary to scroll down twitce
        scrollDownToCourseBrowser()
        scrollDownToCourseBrowser()
        waitForViewWithText("Modules").click()
    }

    fun assertCourseBrowserPageDisplayed() {
        onView(withId(R.id.courseBrowserRecyclerView)).assertDisplayed()
        onView(withId(R.id.courseBrowserTitle)).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout))).swipeDown()
    }

    private fun scrollOpen(textName: String, scrollPosition: Int = 10) {
        try {
            waitForViewWithText(textName).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
        } catch (e: Exception) {
            when(e) {
                is NoMatchingViewException, is PerformException -> {
                    scrollDownToCourseBrowser(scrollPosition)
                    waitForViewWithText(textName).perform(withCustomConstraints(click(), isDisplayingAtLeast(50)))
                }
                else -> throw e
            }
        }
    }

    fun waitForRender() {
        onView(withId(R.id.menu_course_browser_settings)).waitForCheck(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
