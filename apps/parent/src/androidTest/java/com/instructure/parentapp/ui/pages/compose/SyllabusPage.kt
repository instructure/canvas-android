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
 */
package com.instructure.parentapp.ui.pages.compose

import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.page.BasePage
import com.instructure.parentapp.R
import org.hamcrest.Matchers

class SyllabusPage : BasePage() {

    fun assertSyllabusBody(body: String) {
        Web.onWebView(withId(R.id.contentWebView))
            .withElement(DriverAtoms.findElement(Locator.ID, "content"))
            .check(
                WebViewAssertions.webMatches(
                    DriverAtoms.getText(),
                    Matchers.comparesEqualTo(body)
                )
            )
    }

    fun clickLink(linkId: String) {
        Web.onWebView(withId(R.id.contentWebView))
            .withElement(DriverAtoms.findElement(Locator.ID, linkId))
            .perform(DriverAtoms.webClick())
    }
}