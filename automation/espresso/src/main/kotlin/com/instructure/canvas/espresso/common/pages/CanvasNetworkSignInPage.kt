/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.common.pages

import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.loginapi.login.R

class CanvasNetworkSignInPage: BasePage() {

    private val EMAIL_FIELD_CSS = "input[id=\"username\"]"
    private val PASSWORD_FIELD_CSS = "input[id=\"password\"]"
    private val LOGIN_BUTTON_CSS = "button[data-testid=\"login-button\"]"
    private val FORGOT_PASSWORD_BUTTON_CSS = "a[href*='forgot-password']"

    private val signInRoot by OnViewWithId(R.id.signInRoot, autoAssert = false)
    private val toolbar by OnViewWithId(R.id.toolbar, autoAssert = false)

    //region UI Element Locator Methods

    private fun emailField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS))
    }

    private fun passwordField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS))
    }

    private fun loginButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_BUTTON_CSS))
    }

    private fun forgotPasswordButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, FORGOT_PASSWORD_BUTTON_CSS))
    }

    //endregion

    //region Assertion Helpers

    override fun assertPageObjects(duration: Long) {
        signInRoot.assertDisplayed()
        toolbar.assertDisplayed()

        emailField()
        passwordField()
        loginButton()
        forgotPasswordButton()
    }

    //endregion

    //region UI Action Helpers

    private fun enterEmail(email: String) {
        emailField().perform(clearElement())
        emailField().perform(webKeys(email))
    }

    private fun enterPassword(password: String) {
        passwordField().perform(clearElement())
        passwordField().perform(webKeys(password))
    }

    private fun clickLoginButton() {
        loginButton().perform(webClick())
    }

    fun loginAs(user: CanvasUserApiModel) {
        loginAs(user.loginId, user.password)
    }

    fun loginAs(loginId: String, password: String) {
        enterEmail(loginId)
        enterPassword(password)
        clickLoginButton()
    }

    //endregion
}
