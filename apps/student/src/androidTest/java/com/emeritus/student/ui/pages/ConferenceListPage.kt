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
package com.emeritus.student.ui.pages

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.emeritus.student.R
import org.hamcrest.CoreMatchers.allOf

open class ConferenceListPage : BasePage(R.id.conferenceListPage) {

    fun assertEmptyView() {
        onView(withId(R.id.conferenceListEmptyView)).assertDisplayed()
        onView(allOf(withId(R.id.emptyTitle), withText(R.string.noConferencesTitle))).assertDisplayed()
        onView(allOf(withId(R.id.emptyMessage), withText(R.string.noConferencesMessage))).assertDisplayed()

    }

    fun assertConferenceStatus(conferenceTitle: String, expectedStatus: String) {
        onView(allOf(withId(R.id.statusLabel), withText(expectedStatus), hasSibling(allOf(withId(R.id.title), withText(conferenceTitle)))))
    }

    fun assertConferenceDisplayed(conferenceTitle: String) {
        onView(allOf(withId(R.id.title), withText(conferenceTitle))).assertDisplayed()
    }

    fun clickOnOpenExternallyButton() {
        onView(withId(R.id.openExternallyButton)).click()
    }

    fun assertOpenExternallyButtonNotDisplayed() {
        onView(withId(R.id.openExternallyButton)).check(doesNotExist())
    }

    fun openConferenceDetails(conferenceTitle: String) {
        onView(withText(conferenceTitle)).click()
    }

}
