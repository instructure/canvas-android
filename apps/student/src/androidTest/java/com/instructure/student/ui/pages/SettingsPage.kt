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
package com.instructure.student.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.espresso.scrollTo
import com.instructure.pandautils.utils.AppTheme
import com.instructure.student.R

class SettingsPage : BasePage(R.id.settingsFragment) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileSettingLabel by OnViewWithId(R.id.profileSettings)
    private val accountPreferencesLabel by OnViewWithId(R.id.accountPreferences)
    private val pushNotificationsLabel by OnViewWithId(R.id.pushNotifications)

    // The pairObserverLabel may not be present if the corresponding remote-config flag is disabled.
    private val pairObserverLabel by OnViewWithId(R.id.pairObserver, autoAssert = false)
    private val aboutLabel by OnViewWithId(R.id.about)
    private val legalLabel by OnViewWithId(R.id.legal)
    private val remoteConfigLabel by OnViewWithId(R.id.remoteConfigParams)
    private val appThemeTitle by OnViewWithId(R.id.appThemeTitle)
    private val appThemeStatus by OnViewWithId(R.id.appThemeStatus)

    fun openAboutPage() {
        aboutLabel.click()
    }

    fun openLegalPage() {
        legalLabel.scrollTo().click()
    }

    fun openRemoteConfigParams() {
        remoteConfigLabel.scrollTo().click()
    }

    fun openPairObserverPage() {
        pairObserverLabel.scrollTo().click()
    }

    fun openProfileSettings() {
        profileSettingLabel.scrollTo().click()
    }

    fun openAppThemeSettings() {
        appThemeTitle.scrollTo().click()
    }

    fun selectAppTheme(appTheme: AppTheme)
    {
        when (appTheme) {
            AppTheme.LIGHT -> onView(withText(R.string.appThemeLight) + withParent(R.id.select_dialog_listview)).click()
            AppTheme.DARK -> onView(withText(R.string.appThemeDark) + withParent(R.id.select_dialog_listview)).click()
            AppTheme.SYSTEM -> onView(withText(R.string.appThemeSystem) + withParent(R.id.select_dialog_listview)).click()
        }
    }

    fun assertAppThemeTitleTextColor(expectedTextColor: String) {
        appThemeTitle.check(TextViewColorAssertion(expectedTextColor))
    }

    fun assertAppThemeStatusTextColor(expectedTextColor: String) {
        appThemeStatus.check(TextViewColorAssertion(expectedTextColor))
    }
}
