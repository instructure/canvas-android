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
package com.instructure.teacher.ui.renderTests.pages

import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.teacher.ui.pages.SyllabusPage
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers

class SyllabusRenderPage : SyllabusPage() {

    private val tabs by OnViewWithId(R.id.syllabusTabLayout)
    private val webView by WaitForViewWithId(R.id.syllabusWebView)
    private val eventsRecycler by WaitForViewWithId(R.id.syllabusEventsRecyclerView)
    private val eventsEmpty by WaitForViewWithId(R.id.syllabusEmptyView)
    private val eventsError by WaitForViewWithId(R.id.syllabusEventsError)
    private val editIcon by WaitForViewWithId(R.id.menu_edit)

    fun assertDisplaysToolbarText(text: String) {
        findChildTextInToolbar(text).assertDisplayed()
    }

    private fun findChildTextInToolbar(text: String) = onView(Matchers.allOf(withText(text), withParent(R.id.toolbar)))

    fun assertDoesNotDisplaySyllabus() {
        tabs.assertNotDisplayed()
        webView.assertNotDisplayed()
    }

    fun assertDisplaysSyllabus(text: String, shouldDisplayTabs: Boolean = true) {
        if (shouldDisplayTabs) tabs.assertDisplayed() else tabs.assertNotDisplayed()
        webView.assertDisplayed()
        Web.onWebView().withElement(DriverAtoms.findElement(Locator.TAG_NAME, "p")).check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(text)))
    }

    fun assertDisplaysEmpty() {
        eventsEmpty.assertDisplayed()
    }

    fun assertDisplaysError() {
        eventsError.assertDisplayed()
    }

    fun assertDisplaysEvents() {
        eventsRecycler.assertDisplayed()
    }

    fun clickEventsTab() {
        onView(CoreMatchers.allOf(withAncestor(R.id.syllabusTabLayout), withText("Summary"))).click()
    }

    fun swipeToEventsTab() {
        onView(withId(R.id.syllabusPager)).perform(ViewActions.swipeLeft())
    }

    fun swipeToSyllabusTab() {
        onView(withId(R.id.syllabusPager)).perform(ViewActions.swipeRight())
    }

    fun assertDisplayEditIcon() {
        editIcon.assertDisplayed()
    }
}