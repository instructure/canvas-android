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
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import com.instructure.student.ui.e2e.interfaces.SearchablePage
import org.hamcrest.Matchers.allOf

// Tests that files submitted for submissions, submission comments and discussions are
// properly displayed.
class FileListPage : BasePage(R.id.fileListPage), SearchablePage {

    private val addButton by OnViewWithId(R.id.addFab)
    private val uploadFileButton by OnViewWithId(R.id.addFileFab, autoAssert = false)
    private val newFolderButton by OnViewWithId(R.id.addFolderFab, autoAssert = false)

    fun assertItemDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
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
        onView(withText(R.string.ok)).click()
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
        Espresso.pressBack() //Close soft keyboard
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

    override fun clickOnSearchButton() {
        onView(withId(R.id.search)).click()
    }

    override fun typeToSearchBar(textToType: String) {
        onView(withId(R.id.queryInput)).replaceText(textToType)
    }

    override fun clickOnClearSearchButton() {
        waitForView(withId(R.id.clearButton)).click()
        onView(withId(R.id.backButton)).click()
    }

    override fun pressSearchBackButton() {
        onView(withId(R.id.backButton)).click()
    }

    fun assertSearchResultCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.fileSearchRecyclerView) + withAncestor(R.id.container)).check(
            ViewAssertions.matches(ViewMatchers.hasChildCount(expectedCount))
        )
    }

    fun assertFileListCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.listView) + withAncestor(R.id.container)).check(
            ViewAssertions.matches(ViewMatchers.hasChildCount(expectedCount))
        )
    }

    fun assertFolderSize(folderName: String, expectedSize: Int) {
        waitForView(allOf(withId(R.id.fileSize), hasSibling(withId(R.id.fileName) + withText(folderName)))).check(matches(containsTextCaseInsensitive("$expectedSize ${if (expectedSize == 1) "item" else "items"}")))
    }
}