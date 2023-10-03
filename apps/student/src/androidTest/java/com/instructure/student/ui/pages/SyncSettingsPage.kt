/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.ui.pages

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.pandautils.R

class SyncSettingsPage : BasePage(R.id.syncSettingsPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val autoSyncSwitch by OnViewWithId(R.id.autoSyncSwitch)
    private val furtherSettings by OnViewWithId(R.id.furtherSettings)
    private val syncFrequencyLabel by OnViewWithId(R.id.syncFrequencyLabel)
    private val wifiOnlySwitch by OnViewWithId(R.id.wifiOnlySwitch)

    fun clickAutoSyncSwitch() {
        autoSyncSwitch.click()
    }

    fun clickFrequency() {
        syncFrequencyLabel.click()
    }

    fun clickDialogOption(stringResId: Int) {
        onViewWithText(stringResId).click()
    }

    fun clickWifiOnlySwitch() {
        wifiOnlySwitch.click()
    }

    fun clickTurnOff() {
        onViewWithText(R.string.syncSettings_wifiConfirmationPositiveButton).click()
    }

    fun assertFurtherSettingsIsDisplayed() {
        furtherSettings.assertDisplayed()
    }

    fun assertFurtherSettingsNotDisplayed() {
        furtherSettings.assertNotDisplayed()
    }

    fun assertFrequencyLabelText(expected: Int) {
        syncFrequencyLabel.assertHasText(expected)
    }

    fun assertWifiOnlySwitchIsChecked() {
        wifiOnlySwitch.check(matches(isChecked()))
    }

    fun assertWifiOnlySwitchIsNotChecked() {
        wifiOnlySwitch.check(matches(isNotChecked()))
    }

    fun assertDialogDisplayedWithTitle(title: Int) {
        onViewWithText(title).assertDisplayed()
    }
}
