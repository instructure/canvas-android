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
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

// Tests that files submitted for submissions, submission comments and discussions are
// properly displayed.
class FileListPage : BasePage(R.id.fileListPage) {
    fun assertItemDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectItem(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }
}