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
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import com.instructure.teacher.holders.CourseBrowserViewHolder
import org.hamcrest.Matchers.allOf

/**
 * Represents the course browser page (aka. when you opens a course from the dashboard, you can see the Course Browser Page).
 *
 * This class extends the `BasePage` class and provides methods for interacting with the course browser,
 * such as opening different tabs, clicking the settings button, refreshing the page, and asserting the visibility
 * of the course browser page and specific elements.
 *
 * @constructor Creates an instance of the `CourseBrowserPage` class.
 */
class CourseBrowserPage : BasePage() {

    // TODO: Add recycler view scrolling to support small screen size devices.
    private val courseBrowserRecyclerView by WaitForViewWithId(R.id.courseBrowserRecyclerView)
    private val courseImage by OnViewWithId(R.id.courseImage)
    private val courseTitle by OnViewWithId(R.id.courseBrowserTitle)
    private val courseSubtitle by OnViewWithId(R.id.courseBrowserSubtitle)
    private val courseSettingsMenuButton by OnViewWithId(R.id.menu_course_browser_settings)
    private val magicNumberForScroll = 10

    /**
     * Opens the assignments tab in the course browser.
     */
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

    /**
     * Opens the quizzes tab in the course browser.
     */
    fun openQuizzesTab() {
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText(R.string.tab_quizzes).click()
    }

    /**
     * Opens the discussions tab in the course browser.
     */
    fun openDiscussionsTab() {
        scrollOpen(textName = "Discussions", scrollPosition = 1)
    }

    /**
     * Opens the announcements tab in the course browser.
     */
    fun openAnnouncementsTab() {
        scrollOpen("Announcements", scrollPosition = magicNumberForScroll)
    }

    /**
     * Opens the people tab in the course browser.
     */
    fun openPeopleTab() {
        scrollOpen("People", scrollPosition = 3)
    }

    /**
     * Clicks the settings button in the course browser.
     */
    fun clickSettingsButton() {
        courseSettingsMenuButton.click()
    }

    /**
     * Opens the pages tab in the course browser.
     */
    fun openPagesTab() {
        waitForViewWithText(R.string.tab_pages).scrollTo().click()
    }

    /**
     * Opens the syllabus in the course browser.
     */
    fun openSyllabus() {
        waitForViewWithText("Syllabus").scrollTo().click()
    }

    /**
     * Opens the modules tab in the course browser.
     */
    fun openModulesTab() {
        //modules sits at the end of the list, so on smaller resolutions it may be necessary to scroll down twitce
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        scrollDownToCourseBrowser(scrollPosition = magicNumberForScroll)
        waitForViewWithText("Modules").click()
    }

    /**
     * Asserts that the course browser page is displayed.
     *
     * @throws AssertionError if the course browser page is not displayed.
     */
    fun assertCourseBrowserPageDisplayed() {
        onView(withId(R.id.courseBrowserRecyclerView)).assertDisplayed()
        onView(withId(R.id.courseBrowserTitle)).assertDisplayed()
    }

    /**
     * Refreshes the course browser page.
     */
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

    /**
     * Waits for the course browser to finish rendering.
     */
    fun waitForRender() {
        onView(withId(R.id.menu_course_browser_settings)).waitForCheck(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    /**
     * Asserts the course title displayed in the course browser.
     *
     * @param courseTitle The expected course title.
     * @throws AssertionError if the course title is not displayed or does not match the expected title.
     */
    fun assertCourseTitle(courseTitle: String) {
        waitForView(withId(R.id.courseBrowserTitle) + withText(courseTitle)).assertDisplayed()
    }

    /**
     * Asserts the text color of a tab label in the course browser.
     *
     * @param tabTitle The title of the tab.
     * @param expectedColor The expected text color of the tab label.
     * @throws AssertionError if the text color of the tab label does not match the expected color.
     */
    fun assertTabLabelTextColor(tabTitle: String, expectedColor: String) {
        onView(ViewMatchers.withText(tabTitle)).check(TextViewColorAssertion(expectedColor))
    }
}

