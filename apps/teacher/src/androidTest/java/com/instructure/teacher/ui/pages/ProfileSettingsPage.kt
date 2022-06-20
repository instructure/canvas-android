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

import androidx.test.espresso.assertion.ViewAssertions.matches
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.teacher.R

open class ProfileSettingsPage : BasePage(R.id.profileSettingsPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileBanner by OnViewWithId(R.id.profileBanner)
    private val usersAvatar by OnViewWithId(R.id.usersAvatar)
    private val usersName by OnViewWithId(R.id.usersName)
    private val usersEmail by OnViewWithId(R.id.usersEmail)
    private val usersBio by OnViewWithId(R.id.usersBio)

    fun clickEditPencilIcon() {
        onView(withId(R.id.menu_edit)).click()
    }

    fun assertUserNameIs(expectedName: String) {
        usersName.check(matches(withText(expectedName)))
    }

}
