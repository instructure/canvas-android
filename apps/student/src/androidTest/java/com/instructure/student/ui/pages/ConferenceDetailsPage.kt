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
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf

open class ConferenceDetailsPage : BasePage(R.id.conferenceDetailsPage) {

    fun assertConferenceTitleDisplayed() {
        onView(allOf(withId(R.id.title), hasSibling(withId(R.id.statusDetails)))).assertDisplayed()
    }

    fun assertConferenceStatus(expectedStatus: String) {
        onView(allOf(withId(R.id.status), withText(expectedStatus), withParent(R.id.statusDetails))).assertDisplayed()
    }

    fun assertDescription(expectedDescription: String) {
        onView(allOf(withId(R.id.description) + withText(expectedDescription), hasSibling(withId(R.id.statusDetails)))).assertDisplayed()
    }
}
