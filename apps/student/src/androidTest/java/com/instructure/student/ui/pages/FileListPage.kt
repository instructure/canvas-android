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

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithId
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

// Tests that files submitted for submissions, submission comments and discussions are
// properly displayed.
class FileListPage(val searchable: Searchable) : BasePage(R.id.fileListPage) {

    private val addButton by OnViewWithId(R.id.addFab)
    private val uploadFileButton by OnViewWithId(R.id.addFileFab, autoAssert = false)
    private val newFolderButton by OnViewWithId(R.id.addFolderFab, autoAssert = false)

    fun assertItemDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        waitForView(matcher).scrollTo().assertDisplayed()
    }

    fun assertSearchItemDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withAncestor(R.id.fileSearchRecyclerView), withText(itemName))
        waitForView(matcher).scrollTo().assertDisplayed()
    }

    fun assertItemNotDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        onView(matcher).assertNotDisplayed()
    }

    fun selectItem(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun clickAddButton() {
        onView(allOf(withId(R.id.addFab), isDisplayed())).perform(click())
    }

    fun clickUploadFileButton() {
        onView(allOf(withId(R.id.addFileFab), isDisplayed())).perform(click())
    }

    fun clickCreateNewFolderButton() {
        newFolderButton.click()
    }

    fun createNewFolder(folderName: String) {
        waitForViewWithId(R.id.textInput).typeText(folderName)
        onView(withText(android.R.string.ok)).click()
    }

    fun assertPdfPreviewDisplayed() {
        waitForViewWithId(R.id.pspdf__menu_option_edit_annotations).assertDisplayed()
    }

    // Doesn't worry about having scrolling to the top of the page first...
    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayingAtLeast(50)))
                .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }

    fun openOptionMenuFor(itemName: String) {
        val matcher = allOf(withId(R.id.overflowButton), hasSibling(withChild(withText(itemName))))
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    fun renameFile(itemName: String, newName: String) {
        openOptionMenuFor(itemName)
        onView(allOf(withId(R.id.title), withText("Rename"))).click()
        onView(withId(R.id.textInput)).clearText()
        onView(withId(R.id.textInput)).typeText(newName)
        onView(containsTextCaseInsensitive("OK")).click()
        Espresso.closeSoftKeyboard()
        refresh()
    }

    fun deleteFile(itemName: String) {
        openOptionMenuFor(itemName)
        onView(allOf(withId(R.id.title), withText("Delete"))).click()
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DELETE"), isDisplayed())).click() // confirm
    }

    fun assertViewEmpty() {
        // Weird to have "displayed" as the filter and the check, but it's the only way
        // to distinguish from other emptyViews in the stack.
        onView(allOf(withId(R.id.emptyView), isDisplayed())).assertDisplayed()
    }

    fun assertSearchResultCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.fileSearchRecyclerView) + withAncestor(R.id.container)).check(
            matches(hasChildCount(expectedCount))
        )
    }

    fun assertFileListCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.listView) + withAncestor(R.id.fileListPage)).check(
            matches(hasChildCount(expectedCount))
        )
    }

    fun assertFolderSize(folderName: String, expectedSize: Int) {
        waitForView(allOf(withId(R.id.fileSize), hasSibling(withId(R.id.fileName) + withText(folderName)))).check(matches(containsTextCaseInsensitive("$expectedSize ${if (expectedSize == 1) "item" else "items"}")))
    }
}