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

import android.annotation.SuppressLint
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.loginapi.login.R
import org.hamcrest.CoreMatchers.containsString

/**
 * Represents the Wrong Domain error page.
 *
 * This page provides functionality for interacting with the Canvas 404 error page that appears
 * in a WebView when a user enters a domain that doesn't exist or is not authorized.
 * It extends the BasePage class and contains methods to assert the error message and navigate back.
 */
@Suppress("unused")
class WrongDomainPage : BasePage() {

    private val signInRoot by OnViewWithId(R.id.signInRoot, autoAssert = false)
    private val toolbar by OnViewWithId(R.id.toolbar, autoAssert = false)

    /**
     * Asserts that the page objects are displayed (toolbar and webview container).
     */
    override fun assertPageObjects(duration: Long) {
        signInRoot.assertDisplayed()
        toolbar.assertDisplayed()
    }

    /**
     * Asserts that the "You typed:" message is displayed, confirming the error page loaded.
     * This text is immediately visible at the top of the error page.
     * @param domain The domain that was typed (e.g., "wrong-domain").
     */
    fun assertYouTypedMessageDisplayed(domain: String) {
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "body"))
            .check(webMatches(getText(), containsString("You typed: $domain.instructure.com")))
    }

    /**
     * Asserts that the Canvas 404 error page contains an image element.
     * This verifies the Canvas dinosaur (Instructure-saurus) error image is present.
     */
    fun assertErrorPageImageDisplayed() {
        onWebView().withElement(findElement(Locator.TAG_NAME, "img"))
    }

}