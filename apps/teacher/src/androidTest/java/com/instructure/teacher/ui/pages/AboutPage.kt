/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

/**
 * About page
 *
 * @constructor Create empty About page
 */
class AboutPage : BasePage(R.id.aboutPage) {

    private val domainLabel by OnViewWithText(R.string.domain)
    private val loginIdLabel by OnViewWithText(R.string.loginId)
    private val emailLabel by OnViewWithText(R.string.email)

    /**
     * Checks whether the domains is displayed or not.
     *
     * @param domain string.
     */
    fun domainIs(domain: String) {
        onView(withId(R.id.domain) + withText(domain)).assertDisplayed()

    }

    /**
     * Checks whether the login ID is displayed or not.
     *
     * @param loginId string of a user.
     */
    fun loginIdIs(loginId: String) {
        onView(withId(R.id.loginId) + withText(loginId)).assertDisplayed()
    }

    /**
     * Checks whether the email is displayed or not.
     *
     * @param email string of a user.
     */
    fun emailIs(email: String) {
        onView(withId(R.id.email) + withText(email)).assertDisplayed()
    }

    /**
     * Assert that the Instructure logo is displayed.
     *
     */
    fun assertInstructureLogoDisplayed() {
        onView(withId(R.id.instructureLogo)).scrollTo().assertDisplayed()
    }
}