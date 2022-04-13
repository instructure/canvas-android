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

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class BookmarkPage : BasePage() {

    fun assertBookmarkDisplayed(bookmarkName: String) {
        val matcher = allOf(withId(R.id.title), withText(bookmarkName))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertEmptyView() {
        onView(withText(R.string.no_bookmarks)).assertDisplayed()
    }

    fun clickBookmark(bookmarkName: String) {
        val matcher = allOf(withId(R.id.title), withText(bookmarkName))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun changeBookmarkName(originalName: String, newName: String) {

        // Open the overflow menu for the bookmark
        clickOnMoreMenu(originalName)
        // Click on "Edit"
        onView(allOf(withId(R.id.title), withText("Edit"), isDisplayed())).click()

        // Type in the new name
        onView(withId(R.id.bookmarkEditText)).clearText()
        onView(withId(R.id.bookmarkEditText)).typeText(newName)

        // Save
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DONE"))).click()
    }

    fun clickOnMoreMenu(bookmarkName: String) {
        val matcher = allOf(
            withId(R.id.overflow),
            hasSibling(withText(bookmarkName))
        )
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun deleteBookmark(bookmarkName: String) {
        clickOnMoreMenu(bookmarkName)
        onView(allOf(withId(R.id.title), withText("Delete"), isDisplayed())).click()
        onView(withText(R.string.ok) + withAncestor(R.id.buttonPanel)).click()
    }
}