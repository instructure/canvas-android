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

import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.page.BasePage

@Suppress("unused")
class WebViewLoginPage : BasePage() {

    private val EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]"
    private val PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]"
    private val LOGIN_BUTTON_CSS = "button[type=\"submit\"]"

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

    //endregion

    //region Assertion Helpers

    override fun assertPageObjects(duration: Long) {
        emailField()
        passwordField()
        loginButton()
    }

    //endregion

    //region UI Action Helpers

    fun enterEmail(email: String) {
        emailField().perform(webKeys(email))
    }

    fun enterPassword(password: String) {
        passwordField().perform(webKeys(password))
    }

    fun clickLoginButton() {
        loginButton().perform(webClick())
    }

    fun loginAs(teacher: CanvasUserApiModel) {
        enterEmail(teacher.loginId)
        enterPassword(teacher.password)
        clickLoginButton()
    }

    //endregion
}
