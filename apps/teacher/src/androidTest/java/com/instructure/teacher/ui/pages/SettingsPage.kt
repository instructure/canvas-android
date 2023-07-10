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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

/**
 * Represents the Settings page.
 *
 * This page provides functionality for interacting with the elements on the Settings page. It contains methods
 * for opening various settings pages such as profile settings, push notifications, rate app dialog, legal page, about page,
 * feature flags page, and remote config parameters page. It also includes methods for asserting the display of a
 * five-star rating, opening the app theme settings, selecting an app theme, and asserting the text color of the app theme title
 * and status. This page extends the BasePage class.
 */
class SettingsPage : BasePage(R.id.settingsPage) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileSettingLabel by OnViewWithId(R.id.profileButton)
    private val pushNotificationsLabel by OnViewWithId(R.id.notificationPreferenesButton)
    private val rateAppLabel by OnViewWithId(R.id.rateButton)
    private val legalLabel by OnViewWithId(R.id.legalButton)
    private val aboutLabel by OnViewWithId(R.id.aboutButton)
    private val featureFlagLabel by OnViewWithId(R.id.featureFlagButton)
    private val remoteConfigLabel by OnViewWithId(R.id.remoteConfigButton)
    private val appThemeTitle by OnViewWithId(R.id.appThemeTitle)
    private val appThemeStatus by OnViewWithId(R.id.appThemeStatus)

    /**
     * Opens the profile settings page.
     */
    fun openProfileSettingsPage() {
        profileSettingLabel.scrollTo().click()
    }

    /**
     * Opens the push notifications page.
     */
    fun openPushNotificationsPage() {
        pushNotificationsLabel.scrollTo().click()
    }

    /**
     * Opens the rate app dialog.
     */
    fun openRateAppDialog() {
        rateAppLabel.scrollTo().click()
    }

    /**
     * Opens the legal page.
     */
    fun openLegalPage() {
        legalLabel.scrollTo().click()
    }

    /**
     * Opens the about page.
     */
    fun openAboutPage() {
        aboutLabel.scrollTo().click()
    }

    /**
     * Opens the feature flags page.
     */
    fun openFeatureFlagsPage() {
        featureFlagLabel.scrollTo().click()
    }

    /**
     * Opens the remote config parameters page.
     */
    fun openRemoteConfigParamsPage() {
        remoteConfigLabel.scrollTo().click()
    }

    /**
     * Asserts the display of a five-star rating.
     */
    fun assertFiveStarRatingDisplayed() {
        for (i in 1 until 6) {
            Espresso.onView(ViewMatchers.withId(R.id.star + i))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    /**
     * Opens the app theme settings.
     */
    fun openAppThemeSettings() {
        appThemeTitle.scrollTo().click()
    }

    /**
     * Selects the specified app theme.
     *
     * @param appTheme The app theme to select.
     */
    fun selectAppTheme(appTheme: String) {
        onView(withText(appTheme) + withParent(R.id.select_dialog_listview)).click()
    }

    /**
     * Asserts the text color of the app theme title.
     *
     * @param expectedTextColor The expected text color of the app theme title.
     */
    fun assertAppThemeTitleTextColor(expectedTextColor: String) {
        appThemeTitle.check(TextViewColorAssertion(expectedTextColor))
    }

    /**
     * Asserts the text color of the app theme status.
     *
     * @param expectedTextColor The expected text color of the app theme status.
     */
    fun assertAppThemeStatusTextColor(expectedTextColor: String) {
        appThemeStatus.check(TextViewColorAssertion(expectedTextColor))
    }
}
