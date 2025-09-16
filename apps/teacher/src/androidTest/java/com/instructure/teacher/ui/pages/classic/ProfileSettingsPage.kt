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

import androidx.test.espresso.assertion.ViewAssertions.matches
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.teacher.R

/**
 * Represents the Profile Settings Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Profile Settings" page.
 * It contains properties for accessing various views on the page such as the toolbar, profile banner, user's avatar, user's name, user's email, and user's bio.
 * Additionally, it provides methods for clicking on the edit pencil icon and asserting the user's name.
 *
 * @param pageId The ID of the profile settings page.
 */
open class ProfileSettingsPage : BasePage(R.id.profileSettingsPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileBanner by OnViewWithId(R.id.profileBanner)
    private val usersAvatar by OnViewWithId(R.id.usersAvatar)
    private val usersName by OnViewWithId(R.id.usersName)
    private val usersEmail by OnViewWithId(R.id.usersEmail)
    private val usersBio by OnViewWithId(R.id.usersBio)

    /**
     * Clicks on the edit pencil icon.
     */
    fun clickEditPencilIcon() {
        onView(withId(R.id.menu_edit)).click()
    }

    /**
     * Asserts that the user's name is as expected.
     *
     * @param expectedName The expected name of the user.
     */
    fun assertUserNameIs(expectedName: String) {
        usersName.check(matches(withText(expectedName)))
    }

    /**
     * Asserts that the user's name contains the given pronoun.
     *
     * @param pronounString The expected pronoun string of the user.
     */
    fun assertPronouns(pronounString: String) {
        usersName.assertContainsText(pronounString)
    }

}
