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

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.student.R

class SettingsPage : BasePage(R.id.settingsFragment) {
    private val profileSettingLabel by OnViewWithId(R.id.profileSettings)
    private val accountPreferencesLabel by OnViewWithId(R.id.accountPreferences)
    private val pushNotificationsLabel by OnViewWithId(R.id.pushNotifications)

    // The pairObserverLabel may not be present if the corresponding remote-config flag is disabled.
    private val pairObserverLabel by OnViewWithId(R.id.pairObserver, autoAssert = false)
    private val aboutLabel by OnViewWithId(R.id.about)
    private val legalLabel by OnViewWithId(R.id.legal)
    private val subscribeCalendarLabel by OnViewWithId(R.id.subscribeToCalendar, autoAssert = false)
    private val remoteConfigLabel by OnViewWithId(R.id.remoteConfigParams)
    private val appThemeTitle by OnViewWithId(R.id.appThemeTitle)
    private val appThemeStatus by OnViewWithId(R.id.appThemeStatus)
    private val offlineContent by OnViewWithId(R.id.offlineSyncSettingsContainer)

    fun openAboutPage() {
        aboutLabel.scrollTo().click()
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

    fun openSubscribeToCalendar() {
        subscribeCalendarLabel.scrollTo().click()
    }

    fun clickOnSubscribe() {
        onView(withText(R.string.subscribeButton)).click()
    }

    fun openAppThemeSettings() {
        appThemeTitle.scrollTo().click()
    }

    fun selectAppTheme(appTheme: String)
    {
        onView(withText(appTheme) + withParent(R.id.select_dialog_listview)).click()
    }

    fun assertAppThemeTitleTextColor(expectedTextColor: String) {
        appThemeTitle.check(TextViewColorAssertion(expectedTextColor))
    }

    fun assertAppThemeStatusTextColor(expectedTextColor: String) {
        appThemeStatus.check(TextViewColorAssertion(expectedTextColor))
    }

    //OfflineMethod
    fun openOfflineSyncSettingsPage() {
        offlineContent.scrollTo().click()
    }

    //OfflineMethod
    fun assertOfflineContentDisplayed() {
        offlineContent.scrollTo().assertDisplayed()
    }

    //OfflineMethod
    fun assertOfflineContentNotDisplayed() {
        offlineContent.assertNotDisplayed()
    }

    //OfflineMethod
    fun assertOfflineContentTitle() {
        onView(withId(R.id.offlineContentTitle) + withText(R.string.offlineContent)).assertDisplayed()
    }

    //OfflineMethod
    fun assertOfflineSyncSettingsStatus(expectedStatus: Int) {
        onView(withId(R.id.offlineSyncSettingsStatus) + withText(expectedStatus) + withParent(R.id.offlineSyncSettingsContainer) +
                hasSibling(withId(R.id.offlineSyncSettingsTitle) + withText(R.string.offlineSyncSettingsTitle))).assertDisplayed()
    }

}
