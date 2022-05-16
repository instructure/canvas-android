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
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

class SettingsPage : BasePage(R.id.settingsPage) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val profileSettingLabel by OnViewWithId(R.id.profileButton)
    private val pushNotificationsLabel by OnViewWithId(R.id.notificationPreferenesButton)
    private val rateAppLabel by OnViewWithId(R.id.rateButton)
    private val legalLabel by OnViewWithId(R.id.legalButton)
    private val featureFlagLabel by OnViewWithId(R.id.featureFlagButton)
    private val remoteConfigLabel by OnViewWithId(R.id.remoteConfigButton)

    fun openProfileSettingsPage() {
        profileSettingLabel.scrollTo().click()
    }

    fun openPushNotificationsPage() {
        pushNotificationsLabel.scrollTo().click()
    }

    fun openRateAppDialog() {
        rateAppLabel.scrollTo().click()
    }

    fun openLegalPage() {
        legalLabel.scrollTo().click()
    }

    fun openFeatureFlagsPage() {
        featureFlagLabel.scrollTo().click()
    }

    fun openRemoteConfigParamsPage() {
        remoteConfigLabel.scrollTo().click()
    }

    fun assertFiveStarRatingDisplayed() {
        for (i in 1 until 6) {
            Espresso.onView(ViewMatchers.withId(R.id.star + i))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}
