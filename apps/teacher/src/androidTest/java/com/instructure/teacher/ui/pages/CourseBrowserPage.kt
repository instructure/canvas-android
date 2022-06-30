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

import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import com.instructure.teacher.holders.CourseBrowserViewHolder
import org.hamcrest.Matchers.allOf

class CourseBrowserPage : BasePage() {

    // TODO: Add recycler view scrolling to support small screen size devices.
    private val courseBrowserRecyclerView by WaitForViewWithId(R.id.courseBrowserRecyclerView)
    private val courseImage by OnViewWithId(R.id.courseImage)
    private val courseTitle by OnViewWithId(R.id.courseBrowserTitle)
    private val courseSubtitle by OnViewWithId(R.id.courseBrowserSubtitle)
    private val courseSettingsMenuButton by OnViewWithId(R.id.menu_course_browser_settings)
    private val magicNumberForScroll = 10

    fun openAssignmentsTab() {
        scrollOpen("Assignments", scrollPosition = magicNumberForScroll)
    }

    private fun scrollDownToCourseBrowser(scrollPosition: Int)
    {
        /* The course browser RecyclerView is inside a CoordinatorLayout and is therefore only partially
           visible, causing some clicks to fail. We need to perform a swipe up first to make it fully visible. */
        Espresso.onView(ViewMatchers.withId(android.R.id.content)).perform(ViewActions.swipeUp())
        Espresso.onView(ViewMatchers.withId(R.id.courseBrowserRecyclerView))
                .perform(scrollToPosition<CourseBrowserViewHolder>(scrollPosition))

    }
    fun openQuizzesTab() {
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText(R.string.tab_quizzes).click()
    }

    fun openDiscussionsTab() {
        scrollOpen(textName = "Discussions", scrollPosition = 1)
    }

    fun openAnnouncementsTab() {
        scrollOpen("Announcements", scrollPosition = magicNumberForScroll)
    }

    fun openPeopleTab() {
        scrollOpen("People", scrollPosition = 4)
    }

    fun clickSettingsButton() {
        courseSettingsMenuButton.click()
    }

    fun openPagesTab() {
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText(R.string.tab_pages).click()
    }

    fun openSyllabus() {
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText("Syllabus").click()
    }

    fun openModulesTab() {
        //modules sits at the end of the list, so on smaller resolutions it may be necessary to scroll down twitce
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText("Modules").click()
    }

    fun assertCourseBrowserPageDisplayed() {
        onView(withId(R.id.courseBrowserRecyclerView)).assertDisplayed()
        onView(withId(R.id.courseBrowserTitle)).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout))).swipeDown()
    }

    private fun scrollOpen(textName: String, scrollPosition: Int) {
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

    fun assertCourseTitle(courseTitle: String) {
        onView(withId(R.id.courseBrowserTitle) + withText(courseTitle)).assertDisplayed()
    }

    fun assertTabLabelTextColor(tabTitle: String, expectedColor: String) {
        onView(ViewMatchers.withText(tabTitle)).check(TextViewColorAssertion(expectedColor))
    }
}
