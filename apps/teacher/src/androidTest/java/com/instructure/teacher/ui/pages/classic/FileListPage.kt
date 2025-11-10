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
package com.instructure.teacher.ui.pages.classic

import androidx.appcompat.widget.AppCompatButton
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

/**
 * Represents the File List page.
 *
 * This page extends the BasePage class and provides functionality for managing files.
 * It contains various view elements such as buttons for adding files and folders,
 * and methods for asserting the presence or absence of items, selecting items,
 * refreshing the view, renaming and deleting files, creating and deleting folders,
 * performing search operations, and asserting the count of search results and file lists.
 *
 * @constructor Creates an instance of the FileListPage class.
 */
class FileListPage(val searchable: Searchable) : BasePage(R.id.fileListPage) {

    private val addButton by OnViewWithId(R.id.addFab)
    private val uploadFileButton by OnViewWithId(R.id.addFileFab, autoAssert = false)
    private val newFolderButton by OnViewWithId(R.id.addFolderFab, autoAssert = false)

    /**
     * Asserts that the specified item is displayed in the file list.
     *
     * @param itemName The name of the item to assert.
     */
    fun assertItemDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        waitForView(matcher).scrollTo().assertDisplayed()
    }

    /**
     * Asserts that the specified item is not displayed in the file list.
     *
     * @param itemName The name of the item to assert.
     */
    fun assertItemNotDisplayed(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        onView(matcher).check(doesNotExist())
    }

    /**
     * Selects the specified item in the file list.
     *
     * @param itemName The name of the item to select.
     */
    fun selectItem(itemName: String) {
        val matcher = allOf(withId(R.id.fileName), withText(itemName))
        scrollRecyclerView(R.id.fileListRecyclerView, matcher)
        onView(matcher).click()
    }

    /**
     * Refreshes the file list by performing a swipe-down gesture.
     */
    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayingAtLeast(50)))
            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }

    /**
     * Opens the option menu for the specified item.
     *
     * @param itemName The name of the item to open the option menu for.
     */
    private fun openOptionMenuFor(itemName: String) {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(itemName))
            .perform(click());
    }

    /**
     * Renames the currently selected file with the specified new name.
     *
     * @param newName The new name for the file.
     */
    fun renameFile(newName: String) {
        openOptionMenuFor("Edit")
        onView(withId(R.id.titleEditText)).clearText()
        onView(withId(R.id.titleEditText)).typeText(newName)
        onView(withText("Save"))
            .perform(click());
    }

    /**
     * Deletes the specified file.
     *
     * @param itemName The name of the file to delete.
     */
    fun deleteFile(itemName: String) {
        selectItem(itemName)
        openOptionMenuFor("Edit")
        onView(withId(R.id.deleteText)).click()
        Thread.sleep(3000) //Wait for the Delete pop-up window to be displayed. (We need this because sometimes on Bitrise it's a bit slow)
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DELETE"), isDisplayed())).click()
    }

    /**
     *Asserts that the view is empty.
     */
    fun assertViewEmpty() {
        waitForView(allOf(withId(R.id.emptyPandaView), isDisplayed())).assertDisplayed()
    }

    /**
     * Creates a folder with the specified folder name.
     *
     * @param folderName The name of the folder to create.
     */
    fun createFolder(folderName: String) {
        onView(withId(R.id.addFab)).click()
        waitForView(withId(R.id.addFolderFab)).click()
        waitForView(withId(R.id.alertTitle)).assertDisplayed()
        onView(withId(R.id.newFolderName)).typeText(folderName)
        onView(withText(android.R.string.ok)).click()
    }

    /**
     * Deletes the specified folder.
     *
     * @param folderName The name of the folder to delete.
     */
    fun deleteFolder(folderName: String) {
        selectItem(folderName)
        onView(withId(R.id.edit)).click()
        onView(withId(R.id.deleteText)).click()
        Thread.sleep(3000) //Wait for the Delete pop-up window to be displayed. (We need this because sometimes on Bitrise it's a bit slow)
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DELETE"), isDisplayed())).click()
    }

    /**
     * Asserts the count of search results matches the expected count.
     *
     * @param expectedCount The expected count of search results.
     */
    fun assertSearchResultCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.fileSearchRecyclerView) + withAncestor(R.id.container)).check(
            ViewAssertions.matches(hasChildCount(expectedCount))
        )
    }

    /**
     * Asserts the count of file lists matches the expected count.
     *
     * @param expectedCount The expected count of file lists.
     */
    fun assertFileListCount(expectedCount: Int) {
        Thread.sleep(2000)
        onView(withId(R.id.fileListRecyclerView) + withAncestor(R.id.container)).check(
            ViewAssertions.matches(hasChildCount(expectedCount))
        )
    }
}
