/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.CoreMatchers.allOf

@Suppress("unused")
class LoginLandingPage : BasePage() {

    private val canvasLogoImageView by OnViewWithId(R.id.canvasLogo)
    private val findMySchoolButton by OnViewWithId(R.id.findMySchool)
    private val canvasNetworkTextView by OnViewWithId(R.id.canvasNetwork)
    private val previousLoginWrapper by OnViewWithId(R.id.previousLoginWrapper, autoAssert = false)
    private val previousLoginTitleText by OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)
    private val previousLoginDivider by OnViewWithId(R.id.previousLoginDivider, autoAssert = false)
    private val previousLoginRecyclerView by OnViewWithId(R.id.previousLoginRecyclerView, autoAssert = false)
    private val canvasWordmarkView by OnViewWithId(R.id.canvasWordmark, autoAssert = false)
    private val appDescriptionTypeTextView by OnViewWithId(R.id.appDescriptionType, autoAssert = false)
    private val qrCodeButton by OnViewWithId(R.id.qrLogin, autoAssert = false)

    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    fun clickQRCodeButton() {
        qrCodeButton.click()
    }

    fun assertDisplaysCanvasWordmark() {
        canvasWordmarkView.assertDisplayed()
    }

    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }

    fun assertDisplaysPreviousLogins() {
        previousLoginTitleText.assertDisplayed()
    }

    fun assertNotDisplaysPreviousLogins() {
        previousLoginTitleText.assertNotDisplayed()
    }

    fun assertPreviousLoginUserDisplayed(userName: String) {
        onView(withText(userName)).assertDisplayed()
    }

    fun assertPreviousLoginUserNotExist(userName: String) {
        onView(withText(userName)).check(doesNotExist())
    }

    fun removeUserFromPreviousLogins(userName: String) {
        onView(allOf(withId(R.id.removePreviousUser), hasSibling(withChild(withText(userName))))).click()
    }

    fun loginWithPreviousUser(previousUser: CanvasUserApiModel) {
        onViewWithText(previousUser.name).click()
    }

    fun loginWithPreviousUser(previousUser: User) {
        onViewWithText(previousUser.name).click()
    }
}
