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
package com.instructure.teacher.ui.pages

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

class EditDiscussionsDetailsPage : BasePage() {

    private val contentRceView by WaitForViewWithId(R.id.rce_webView)

    fun editTitle(newTitle: String) {
        onView(withId(R.id.editDiscussionName)).replaceText(newTitle)
        Espresso.closeSoftKeyboard()
    }

    fun togglePublished() {
        onView(withId(R.id.publishSwitch)).scrollTo().click()
    }

    fun deleteDiscussion() {
        onView(withId(R.id.deleteText)).scrollTo().click()
        onView(withId(android.R.id.button1)).click() //button1 is actually the 'DELETE' button on the UI pop-up dialog.
    }

    fun clickSave() { //This method is used when editing an existing discussion.
        onView(withId(R.id.menuSave)).click()
    }

    fun clickSendNewDiscussion() { //This method is used when creating a new discussion via mobile UI.
        onView(withId(R.id.menuSaveDiscussion)).click()
    }

    fun editDescription(newDescription: String) {
        contentRceView.perform(TypeInRCETextEditor(newDescription))
    }

}