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

/**
 * Represents the WebView Login Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the WebView login page.
 * It contains private constants for the CSS selectors of the email field, password field, and login button.
 * Additionally, it provides methods for locating the email field, password field, and login button in the WebView, asserting the presence of these elements on the page,
 * entering an email and password, clicking the login button, and logging in as a teacher.
 */
@Suppress("unused")
class WebViewLoginPage : BasePage() {

    private val EMAIL_FIELD_CSS = "input[name=\"pseudonym_session[unique_id]\"]"
    private val PASSWORD_FIELD_CSS = "input[name=\"pseudonym_session[password]\"]"
    private val LOGIN_BUTTON_CSS = "button[type=\"submit\"]"

    /**
     * Locates the email field in the WebView.
     *
     * @return The WebInteraction object representing the email field.
     */
    private fun emailField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS))
    }

    /**
     * Locates the password field in the WebView.
     *
     * @return The WebInteraction object representing the password field.
     */
    private fun passwordField(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS))
    }

    /**
     * Locates the login button in the WebView.
     *
     * @return The WebInteraction object representing the login button.
     */
    private fun loginButton(): Web.WebInteraction<*> {
        return onWebView().withElement(findElement(Locator.CSS_SELECTOR, LOGIN_BUTTON_CSS))
    }

    /**
     * Asserts the presence of the email field, password field, and login button on the page.
     *
     * @param duration The duration to wait for the elements to be displayed.
     */
    override fun assertPageObjects(duration: Long) {
        emailField()
        passwordField()
        loginButton()
    }

    /**
     * Enters an email into the email field in the WebView.
     *
     * @param email The email to be entered.
     */
    fun enterEmail(email: String) {
        emailField().perform(webKeys(email))
    }

    /**
     * Enters a password into the password field in the WebView.
     *
     * @param password The password to be entered.
     */
    fun enterPassword(password: String) {
        passwordField().perform(webKeys(password))
    }

    /**
     * Clicks the login button in the WebView.
     */
    fun clickLoginButton() {
        loginButton().perform(webClick())
    }

    /**
     * Logs in as a teacher by entering the teacher's email and password, and clicking the login button.
     *
     * @param teacher The teacher object representing the teacher's login credentials.
     */
    fun loginAs(teacher: CanvasUserApiModel) {
        enterEmail(teacher.loginId)
        enterPassword(teacher.password)
        clickLoginButton()
    }
}

