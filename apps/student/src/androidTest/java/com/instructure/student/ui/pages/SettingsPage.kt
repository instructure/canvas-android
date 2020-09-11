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
import com.instructure.espresso.assertHasContentDescription
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.student.R

class SettingsPage : BasePage(R.id.settingsFragment) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileSettingLabel by OnViewWithId(R.id.profileSettings)
    private val accountPreferencesLabel by OnViewWithId(R.id.accountPreferences)
    private val pushNotificationsLabel by OnViewWithId(R.id.pushNotifications)
    // The pairObserverLabel may not be present if the corresponding remote-config flag is disabled.
    private val pairObserverLabel by OnViewWithId(R.id.pairObserver,autoAssert=false)
    private val aboutLabel by OnViewWithId(R.id.about)
    private val legalLabel by OnViewWithId(R.id.legal)
    private val helpLabel by OnViewWithId(R.id.help)
    private val remoteConfigLabel by OnViewWithId(R.id.remoteConfigParams)

    fun launchAboutPage() {
        aboutLabel.click()
    }

    fun launchLegalPage() {
        legalLabel.scrollTo().click()
    }

    fun launchHelpPage() {
        helpLabel.scrollTo().click()
    }

    fun launchRemoteConfigParams() {
        remoteConfigLabel.scrollTo().click()
    }

    fun launchPairObserverPage() {
        pairObserverLabel.scrollTo().click()
    }

    fun launchProfileSettings() {
        profileSettingLabel.scrollTo().click()
    }
}
