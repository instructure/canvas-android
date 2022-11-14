/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

class EditAnnouncementPage : BasePage() {

    fun openEdit() {
        onView(withId(R.id.menu_edit)).click()
    }

    fun editAnnouncementName(newName: String) {
        onView(withId(R.id.editDiscussionName)).replaceText(newName)
    }

    fun saveEditAnnouncement() {
        onView(withId(R.id.menuSave)).click()
    }

    fun deleteAnnouncement() {
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.deleteText)).scrollTo()
        onView(withId(R.id.deleteText)).click()
        onView(withId(android.R.id.button1)).click()
    }
}