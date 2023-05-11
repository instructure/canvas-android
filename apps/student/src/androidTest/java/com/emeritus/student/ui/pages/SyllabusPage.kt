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
package com.emeritus.student.ui.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.swipeDown
import com.emeritus.student.R
import org.hamcrest.Matchers.allOf

open class SyllabusPage : BasePage(R.id.syllabusPage) {

    fun assertItemDisplayed(itemText: String) {
        scrollRecyclerView(R.id.syllabusEventsRecycler, itemText)
    }

    fun assertEmptyView() {
        onView(withId(R.id.syllabusEmptyView)).assertDisplayed()
    }

    fun selectSummaryTab() {
        onView(containsTextCaseInsensitive("summary")).click()
    }

    fun selectSummaryEvent(name: String) {
        onView(containsTextCaseInsensitive(name)).click()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.syllabusPage))).swipeDown()
    }

}
