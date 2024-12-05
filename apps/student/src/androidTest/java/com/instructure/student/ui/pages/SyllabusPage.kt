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
package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.matchToolbarText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.getStringFromResource
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.swipeDown
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

open class SyllabusPage : BasePage(R.id.syllabusPage) {

    fun assertToolbarCourseTitle(courseName: String) {
        onView(withId(R.id.toolbar) + withAncestor(R.id.syllabusPage)).assertDisplayed().check(ViewAssertions.matches(matchToolbarText(Matchers.`is`(getStringFromResource(R.string.syllabus)), true)))
        onView(withId(R.id.toolbar) + withAncestor(R.id.syllabusPage)).assertDisplayed().check(ViewAssertions.matches(matchToolbarText(Matchers.`is`(courseName), false)))
    }

    fun assertItemDisplayed(itemText: String) {
        scrollRecyclerView(R.id.syllabusEventsRecycler, itemText)
    }

    fun assertEmptyView() {
        onView(withId(R.id.syllabusEmptyView)).assertDisplayed()
    }

    fun selectSummaryTab() {
        onView(containsTextCaseInsensitive("summary") + withAncestor(R.id.syllabusTabLayout)).click()
    }

    fun selectSyllabusTab() {
        onView(containsTextCaseInsensitive("syllabus") + withAncestor(R.id.syllabusTabLayout)).click()
    }

    fun selectSummaryEvent(name: String) {
        onView(containsTextCaseInsensitive(name)).click()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.syllabusPage))).swipeDown()
    }

    fun assertNoTabs() {
        onView(withId(R.id.syllabusTabLayout)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    fun assertSyllabusBody(syllabusBody: String) {
            Web.onWebView(withId(R.id.contentWebView) + withAncestor(R.id.syllabusPage))
                .withElement(DriverAtoms.findElement(Locator.ID, "content"))
                .check(
                    WebViewAssertions.webMatches(
                        DriverAtoms.getText(),
                        Matchers.comparesEqualTo(syllabusBody)
                    )
                )
    }
}
