/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithId
import com.instructure.pandautils.R
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import org.hamcrest.Matchers.allOf

/**
 * Represents the Legal Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the legal
 * page. It contains various view elements such as privacy policy label, terms of use label,
 * and open source label.
 */
class LegalPage : BasePage(R.id.legalPage) {

    private val privacyPolicyLabel by OnViewWithId(R.id.privacyPolicyLabel)
    private val termsOfUseLabel by OnViewWithId(R.id.termsOfUseLabel)

    /**
     * Opens the privacy policy.
     */
    fun openPrivacyPolicy() {
        privacyPolicyLabel.click()
    }

    /**
     * Opens the terms of use.
     */
    fun openTermsOfUse() {
        termsOfUseLabel.click()
    }

    fun assertTermsOfUseDisplayed() {
        // This is the safest thing to assert on.  The content of the page could be dicey.
        onView(allOf(withParent(withId(R.id.toolbar)), containsTextCaseInsensitive("Terms of Use"))).assertDisplayed()
    }
}
