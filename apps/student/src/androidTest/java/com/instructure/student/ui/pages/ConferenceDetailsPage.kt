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
package com.instructure.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf

open class ConferenceDetailsPage : BasePage(R.id.conferenceDetailsPage) {

    fun assertConferenceTitleDisplayed(conferenceTitle: String) {
        onView(allOf(withId(R.id.title), withText(conferenceTitle), hasSibling(withId(R.id.statusDetails)))).assertDisplayed()
    }

    fun assertConferenceStatus(expectedStatus: String) {
        onView(allOf(withId(R.id.status), withText(expectedStatus), withParent(R.id.statusDetails))).assertDisplayed()
    }

    fun assertDescription(expectedDescription: String) {
        onView(allOf(withText(R.string.description), hasSibling(withId(R.id.statusDetails)), hasSibling(withId(R.id.description)))).assertDisplayed()
        onView(allOf(withId(R.id.description) + withText(expectedDescription), hasSibling(withId(R.id.statusDetails)))).assertDisplayed()
    }

    fun assertConferenceDetailsToolbarText(courseName: String) {
        onView(withText(R.string.conferenceDetails) + withParent(R.id.toolbar) + withAncestor(R.id.conferenceDetailsPage)).assertDisplayed()
        onView(withText(courseName) + withParent(R.id.toolbar) + withAncestor(R.id.conferenceDetailsPage)).assertDisplayed()
    }

}
