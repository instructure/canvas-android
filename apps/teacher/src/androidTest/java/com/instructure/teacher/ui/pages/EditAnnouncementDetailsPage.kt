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
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

/**
 * Represents the Edit Announcement Page.
 */
class EditAnnouncementDetailsPage : BasePage() {

    /**
     * Edits the name of the announcement with the specified [newName].
     *
     * @param newName The new name for the announcement.
     */
    fun editAnnouncementTitle(newName: String) {
        onView(withId(R.id.announcementNameEditText)).replaceText(newName)
    }

    /**
     * Saves the edited announcement.
     */
    fun saveAnnouncement() {
        onView(withId(R.id.menuSaveAnnouncement)).click()
    }

    /**
     * Deletes the announcement.
     */
    fun deleteAnnouncement() {
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.deleteAnnouncementButton)).scrollTo()
        onView(withId(R.id.deleteAnnouncementButton)).click()
        onView(withId(android.R.id.button1)).click()
    }
}