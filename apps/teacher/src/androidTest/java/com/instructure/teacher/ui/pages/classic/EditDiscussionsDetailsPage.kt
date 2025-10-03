/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

import androidx.test.espresso.Espresso
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TypeInRCETextEditor

/**
 * The `EditDiscussionsDetailsPage` class represents a page for editing discussion details.
 * It extends the `BasePage` class.
 *
 * @constructor Creates an instance of `EditDiscussionsDetailsPage`.
 */
class EditDiscussionsDetailsPage : BasePage() {

    private val contentRceView by WaitForViewWithId(R.id.rce_webView)

    /**
     * Edits the title of the discussion.
     *
     * @param newTitle The new title of the discussion.
     */
    fun editDiscussionTitle(newTitle: String) {
        onView(withId(R.id.editDiscussionName)).replaceText(newTitle)
        Espresso.closeSoftKeyboard()
    }

    /**
     * Toggles the published state of the discussion.
     */
    fun togglePublished() {
        onView(withId(R.id.publishSwitch)).scrollTo().click()
    }

    /**
     * Deletes the discussion.
     */
    fun deleteDiscussion() {
        onView(withId(R.id.deleteText)).scrollTo().click()
        onView(withId(android.R.id.button1)).click() // button1 is actually the 'DELETE' button on the UI pop-up dialog.
    }

    /**
     * Clicks the save button. This method is used when editing an existing discussion.
     */
    fun saveDiscussion() {
        onView(withId(R.id.menuSave)).click()
    }

    /**
     * Clicks the send new discussion button. This method is used when creating a new discussion via mobile UI.
     */
    fun clickSendNewDiscussion() {
        onView(withId(R.id.menuSaveDiscussion)).click()
    }

    /**
     * Edits the description of the discussion.
     *
     * @param newDescription The new description of the discussion.
     */
    fun editDiscussionDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }
}
