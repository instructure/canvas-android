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
package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.clearText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.typeText
import com.instructure.teacher.R

/**
 * The `EditProfileSettingsPage` class represents a page for editing profile settings.
 * It extends the `BasePage` class.
 *
 * @constructor Creates an instance of `EditProfileSettingsPage`.
 */
class EditProfileSettingsPage : BasePage(R.id.editProfileSettingsPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileBanner by OnViewWithId(R.id.profileBanner)
    private val usersAvatar by OnViewWithId(R.id.usersAvatar)
    private val usersName by OnViewWithId(R.id.usersName)
    private val profileCameraIcon by OnViewWithId(R.id.profileCameraIcon)

    /**
     * Clicks on the save button to save the profile settings.
     */
    fun clickOnSave() {
        onView(withId(R.id.menuSave)).click()
    }

    /**
     * Edits the user name with the specified new user name.
     *
     * @param newUserName The new user name to be set.
     */
    fun editUserName(newUserName: String) {
        clearUserNameInputField()
        usersName.typeText(newUserName)
    }

    /**
     * Clears the user name input field.
     */
    private fun clearUserNameInputField() {
        usersName.clearText()
    }
}

