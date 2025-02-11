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

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.parentapp.R
import org.hamcrest.Matchers

class LeftSideNavigationDrawerPage: BasePage(R.id.drawer_layout) {

    fun clickManageStudents() {
        onViewWithText(R.string.screenTitleManageStudents).click()
    }

    fun clickSettings() {
        onViewWithText(R.string.settings).click()
    }

    fun clickInbox() {
        onViewWithText(R.string.inbox).click()
    }

    fun clickHelpMenu() {
        onViewWithText(R.string.help).click()
    }

    fun clickLogout() {
        onViewWithText(R.string.logout).click()
    }

    fun clickOk() {
        onViewWithText(android.R.string.ok).click()
    }

    fun clickSwitchUsers() {
        onViewWithText(R.string.navigationDrawerSwitchUsers).click()
    }

    fun assertLogoutDialog() {
        onViewWithText(R.string.logout_warning).assertDisplayed()
        onViewWithText(Matchers.equalToIgnoringCase(getStringFromResource(android.R.string.cancel))).assertDisplayed()
        onViewWithText(Matchers.equalToIgnoringCase(getStringFromResource(android.R.string.ok))).assertDisplayed()
    }

    fun logout() {
        clickLogout()
        assertLogoutDialog()
        clickOk()
        waitForMatcherWithSleeps(ViewMatchers.withId(com.instructure.loginapi.login.R.id.canvasLogo), 20000).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        onViewWithId(R.id.navigationButtonHolder).click()
        onViewWithText(user.name).assertDisplayed()
        Espresso.pressBack()
    }

}