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
 */
package com.instructure.parentapp.ui.pages

import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasChild
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.parentapp.R
import org.hamcrest.Matchers

class LegalPage : BasePage(R.id.legalPage) {

    private val privacyPolicy by WaitForViewWithId(R.id.privacyPolicy)

    private val termsOfUse by WaitForViewWithId(R.id.termsOfUse)

    private val openSource by WaitForViewWithId(R.id.openSource)

    override fun assertPageObjects() {
        super.assertPageObjects()
        privacyPolicy.assertHasChild(withText(R.string.privacyPolicy))
        termsOfUse.assertHasChild(withText(R.string.termsOfUse))
        openSource.assertHasChild(withText(R.string.canvasOnGithub))
    }

    fun clickPrivacyPolicy() = privacyPolicy.click()

    fun clickTermsOfUse() = termsOfUse.click()

    fun clickOpenSource() = openSource.click()

    fun assertDisplaysPrivacyPolicy() = assertContainsWebString("Privacy Policy")

    fun assertDisplaysTermsOfUse() = assertContainsWebString("TERMS OF USE")

    fun assertDisplaysOpenSource() {
        waitForViewWithText(R.string.canvasOnGithub).assertDisplayed()
    }

    private fun assertContainsWebString(contents: String) {
        waitForViewWithId(R.id.internalWebview)
        Web.onWebView(withId(R.id.internalWebview))
            .withElement(DriverAtoms.findElement(Locator.TAG_NAME, "body"))
            .check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.containsString(contents)))
    }

}
