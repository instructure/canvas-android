/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R
import com.instructure.student.ui.pages.SyllabusPage
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf


class SyllabusRenderPage : SyllabusPage() {

    private val tabs by OnViewWithId(R.id.syllabusTabLayout)
    private val webView by WaitForViewWithId(R.id.contentWebView)
    private val eventsRecycler by WaitForViewWithId(R.id.syllabusEventsRecycler)
    private val eventsEmpty by WaitForViewWithId(R.id.syllabusEmptyView)
    private val eventsError by WaitForViewWithId(R.id.syllabusEventsError)

    fun assertDisplaysToolbarTitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertDisplaysToolbarSubtitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertDoesNotDisplaySyllabus() {
        tabs.assertNotDisplayed()
        webView.assertNotDisplayed()
    }

    fun assertDisplaysSyllabus(text: String, shouldDisplayTabs: Boolean = true) {
        if (shouldDisplayTabs) tabs.assertDisplayed() else tabs.assertNotDisplayed()
        webView.assertDisplayed()
        onWebView().withElement(findElement(Locator.TAG_NAME, "p")).check(webMatches(getText(), Matchers.comparesEqualTo(text)))
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
        onView(withId(R.id.syllabusPager)).perform(swipeLeft())
    }

    fun swipeToSyllabusTab() {
        onView(withId(R.id.syllabusPager)).perform(swipeRight())
    }

    private fun findChildText(text: String, parentId: Int) = onView(allOf(withText(text), withParent(parentId)))
}
