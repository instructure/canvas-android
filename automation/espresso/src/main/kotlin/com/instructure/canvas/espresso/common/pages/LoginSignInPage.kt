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
package com.instructure.canvas.espresso.common.pages

import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withText
import com.instructure.loginapi.login.R
import org.hamcrest.CoreMatchers.containsString

@Suppress("unused")
class LoginSignInPage: BasePage() {

    private val EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]"
    private val PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]"
    private val LOGIN_BUTTON_CSS = "button[type=\"submit\"]"
    private val FORGOT_PASSWORD_BUTTON_CSS = "a[class=\"forgot-password flip-to-back\"]"
    private val AUTHORIZE_BUTTON_CSS = "button[type=\"submit\"]"
    private val LOGIN_ERROR_MESSAGE_HOLDER_CSS = "div[class='error']"

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

    private fun authorizeButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, AUTHORIZE_BUTTON_CSS))
    }

    private fun loginErrorMessageHolder(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_ERROR_MESSAGE_HOLDER_CSS))
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

    fun assertLoginRequiredDialog() {
        waitForViewWithText(R.string.loginRequired).assertDisplayed()
    }

    //endregion

    //region UI Action Helpers

    fun clickLogInOnLoginRequiredDialog() {
        onView(withText(R.string.login)).click()
    }

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

    fun clickForgotPasswordButton() {
        forgotPasswordButton().perform(webClick())
    }

    fun assertLoginErrorMessage(errorMessage: String) {
        loginErrorMessageHolder().check(webMatches(getText(), containsString(errorMessage)))
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
