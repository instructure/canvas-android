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
import androidx.test.espresso.Espresso
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
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.withAncestor
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.comparesEqualTo

open class SyllabusPage : BasePage(R.id.syllabusPage) {

    private val tabs by OnViewWithId(R.id.syllabusTabLayout)
    private val webView by WaitForViewWithId(R.id.syllabusWebView)

    fun assertItemDisplayed(itemText: String) {
        scrollRecyclerView(R.id.syllabusEventsRecyclerView, itemText)
    }

    fun assertEmptyView() {
        Espresso.onView(ViewMatchers.withId(R.id.syllabusEmptyView)).assertDisplayed()
    }

    fun selectSummaryTab() {
        Espresso.onView(containsTextCaseInsensitive("summary")).click()
    }

    fun selectSummaryEvent(name: String) {
        Espresso.onView(containsTextCaseInsensitive(name)).click()
    }

    fun refresh() {
        Espresso.onView(allOf(ViewMatchers.withId(R.id.swipeRefreshLayout), withAncestor(R.id.syllabusPage))).swipeDown()
    }

    fun openEditSyllabus() {
        onViewWithId(R.id.menu_edit).click()
    }

    fun assertDisplaysSyllabus(syllabusBody: String, shouldDisplayTabs: Boolean = true) {
        if (shouldDisplayTabs) tabs.assertDisplayed() else tabs.assertNotDisplayed()
        webView.assertDisplayed()
        Web.onWebView()
            .withElement(findElement(Locator.TAG_NAME, "html"))
            .check(WebViewAssertions.webMatches(getText(), comparesEqualTo(syllabusBody)))
    }

    fun assertSuccessfulSave(activity: Activity) {
        checkToastText(R.string.syllabusSuccessfullyUpdated, activity)
    }
}