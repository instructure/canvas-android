/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import android.app.Activity
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.comparesEqualTo

/**
 * Represents the Syllabus Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the Syllabus page.
 * It contains methods for asserting the display of an item, the empty view, selecting tabs, selecting summary events,
 * refreshing the page, opening the edit syllabus screen, asserting the display of the syllabus content,
 * and asserting the successful save of the syllabus.
 *
 * @param pageId The ID of the Syllabus page.
 */
open class SyllabusPage : BasePage(R.id.syllabusPage) {

    private val tabs by OnViewWithId(R.id.syllabusTabLayout)

    /**
     * Asserts the display of an item on the Syllabus page.
     *
     * @param itemText The text of the item to be displayed.
     */
    fun assertItemDisplayed(itemText: String) {
        scrollRecyclerView(R.id.syllabusEventsRecyclerView, itemText)
    }

    /**
     * Asserts the presence of the empty view on the Syllabus page.
     */
    fun assertEmptyView() {
        onView(withId(R.id.syllabusEmptyView)).assertDisplayed()
    }

    /**
     * Selects the summary tab on the Syllabus page.
     */
    fun selectSummaryTab() {
        onView(containsTextCaseInsensitive("summary") + withAncestor(R.id.syllabusTabLayout)).click()
    }

    /**
     * Selects the syllabus tab on the Syllabus page.
     */
    fun selectSyllabusTab() {
        onView(containsTextCaseInsensitive("syllabus") + withAncestor(R.id.syllabusTabLayout)).click()
    }

    /**
     * Selects a summary event on the Syllabus page.
     *
     * @param name The name of the summary event to be selected.
     */
    fun selectSummaryEvent(name: String) {
        onView(containsTextCaseInsensitive(name)).click()
    }

    /**
     * Refreshes the Syllabus page by performing a swipe down action on the swipe refresh layout.
     */
    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.syllabusPage))).swipeDown()
    }

    /**
     * Opens the edit syllabus screen by clicking on the edit menu button.
     */
    fun openEditSyllabus() {
        onViewWithId(R.id.menu_edit).click()
    }

    /**
     * Asserts the display of the syllabus content on the Syllabus page.
     *
     * @param syllabusBody The expected body of the syllabus.
     * @param shouldDisplayTabs Indicates whether the tabs should be displayed. Default is true.
     */
    fun assertDisplaysSyllabus(syllabusBody: String, shouldDisplayTabs: Boolean = true) {
        if (shouldDisplayTabs) tabs.assertDisplayed() else tabs.assertNotDisplayed()
        waitForView(allOf(withId(R.id.contentWebView), ViewMatchers.isDisplayed()))
        Web.onWebView()
            .withElement(findElement(Locator.TAG_NAME, "html"))
            .check(WebViewAssertions.webMatches(getText(), comparesEqualTo(syllabusBody)))
    }

    /**
     * Asserts the successful save of the syllabus.
     *
     * @param activity The activity context for checking the toast message.
     */
    fun assertSuccessfulSave(activity: Activity) {
        checkToastText(R.string.syllabusSuccessfullyUpdated, activity)
    }
}