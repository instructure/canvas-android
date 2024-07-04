/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withText
import com.instructure.parentapp.R
import org.hamcrest.Matchers.equalToIgnoringCase

class DashboardPage : BasePage(R.id.drawer_layout) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val bottomNavigationView by OnViewWithId(R.id.bottom_nav)

    fun assertObserverData(user: User) {
        onViewWithText(user.name).assertDisplayed()
        onViewWithText(user.email.orEmpty()).assertDisplayed()
    }

    fun openNavigationDrawer() {
        onViewWithContentDescription(R.string.navigation_drawer_open).click()
    }

    fun assertSelectedStudent(name: String) {
        onView(withText(name) + withAncestor(R.id.selected_student_container)).assertDisplayed()
    }

    fun openStudentSelector() {
        toolbar.click()
    }

    fun selectStudent(name: String) {
        onView(withText(name) + withAncestor(R.id.student_list)).click()
    }

    fun tapLogout() {
        onViewWithText(R.string.logout).click()
    }

    fun assertLogoutDialog() {
        onViewWithText(R.string.logout_warning).assertDisplayed()
        onViewWithText(equalToIgnoringCase(getStringFromResource(android.R.string.cancel))).assertDisplayed()
        onViewWithText(android.R.string.ok).assertDisplayed()
    }

    fun tapOk() {
        onViewWithText(R.string.ok).click()
    }

    fun tapSwitchUsers() {
        onViewWithText(R.string.navigationDrawerSwitchUsers).click()
    }
}
