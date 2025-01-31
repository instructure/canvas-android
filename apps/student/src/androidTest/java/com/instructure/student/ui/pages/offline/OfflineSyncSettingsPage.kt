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

package com.instructure.student.ui.pages.offline

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.pandautils.R

class OfflineSyncSettingsPage : BasePage(R.id.syncSettingsPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val autoSyncSwitch by OnViewWithId(R.id.autoSyncSwitch)
    private val furtherSettings by OnViewWithId(R.id.furtherSettings)
    private val syncFrequencyLabel by OnViewWithId(R.id.syncFrequencyLabel)
    private val wifiOnlySwitch by OnViewWithId(R.id.wifiOnlySwitch)

    fun clickAutoSyncSwitch() {
        autoSyncSwitch.click()
    }

    fun openSyncFrequencySettingsDialog() {
        syncFrequencyLabel.click()
    }

    fun clickSyncFrequencyDialogOption(stringResId: Int) {
        onView(withText(stringResId) + withParent(R.id.select_dialog_listview)).click()
    }

    fun clickWifiOnlySwitch() {
        wifiOnlySwitch.click()
    }

    fun clickTurnOff() {
        onViewWithText(R.string.syncSettings_wifiConfirmationPositiveButton).click()
    }

    fun assertTurnOffWifiOnlyDialogTexts() {
        waitForView(withId(R.id.alertTitle) + withText(R.string.syncSettings_wifiConfirmationTitle)).assertDisplayed()
        waitForView(withText(R.string.syncSettings_wifiConfirmationPositiveButton) + withAncestor(R.id.buttonPanel)).assertDisplayed()
        onView(withText(R.string.synySettings_wifiConfirmationMessage)).assertDisplayed()
    }

    fun assertFurtherSettingsIsDisplayed() {
        furtherSettings.assertDisplayed()
    }

    fun assertFurtherSettingsNotDisplayed() {
        furtherSettings.assertNotDisplayed()
    }

    fun assertSyncFrequencyLabelText(expected: Int) {
        syncFrequencyLabel.assertHasText(expected)
    }

    fun assertSyncFrequencyTitleText() {
        onView(withText(R.string.syncSettings_syncFrequencyTitle) + withParent(R.id.syncFrequencyContainer)).assertDisplayed()
    }

    fun assertWifiOnlySwitchIsChecked() {
        wifiOnlySwitch.check(matches(isChecked()))
    }

    fun assertWifiOnlySwitchIsNotChecked() {
        wifiOnlySwitch.check(matches(isNotChecked()))
    }

    fun assertAutoSyncSwitchIsChecked() {
        autoSyncSwitch.check(matches(isChecked()))
    }

    fun assertAutoSyncSwitchIsNotChecked() {
        autoSyncSwitch.check(matches(isNotChecked()))
    }

    fun assertDialogDisplayedWithTitle(title: String) {
        onViewWithText(title).assertDisplayed()
    }

    fun assertSyncSettingsToolbarTitle() {
        onView(withText(com.instructure.student.R.string.syncSettings_toolbarTitle) + withParent(withId(
            com.instructure.student.R.id.toolbar) + withAncestor(com.instructure.student.R.id.syncSettingsPage))).assertDisplayed()
    }

    fun assertSyncSettingsPageDescriptions() {
        onView(withText(R.string.syncSettings_autoContentSyncDescription)).assertDisplayed()
        onView(withText(R.string.syncSettings_syncFrequencyDescription)).assertDisplayed()
        onView(withText(R.string.syncSettings_wifiOnlyDescription)).assertDisplayed()
    }

}
