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

package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.pandautils.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyncSettingsInteractionTest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION)
    fun testFurtherSettingsDisplayedByDefault() {
        goToSyncSettings()
        offlineSyncSettingsPage.assertFurtherSettingsIsDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION)
    fun testClickAutoSyncHidesFurtherSettings() {
        goToSyncSettings()
        offlineSyncSettingsPage.clickAutoSyncSwitch()
        offlineSyncSettingsPage.assertFurtherSettingsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION)
    fun testChangeFrequency() {
        goToSyncSettings()
        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.daily)
        offlineSyncSettingsPage.openSyncFrequencySettingsDialog()
        offlineSyncSettingsPage.clickSyncFrequencyDialogOption(R.string.weekly)
        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.weekly)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION)
    fun testChangeContentOverWifiOnly() {
        goToSyncSettings()
        offlineSyncSettingsPage.assertWifiOnlySwitchIsChecked()
        offlineSyncSettingsPage.clickWifiOnlySwitch()
        offlineSyncSettingsPage.assertTurnOffWifiOnlyDialogTexts()
        offlineSyncSettingsPage.clickTurnOff()
        offlineSyncSettingsPage.assertWifiOnlySwitchIsNotChecked()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION)
    fun testChangesSavedCorrectly() {
        val data = createMockCanvas()
        goToSyncSettings(data)

        offlineSyncSettingsPage.openSyncFrequencySettingsDialog()
        offlineSyncSettingsPage.clickSyncFrequencyDialogOption(R.string.weekly)
        offlineSyncSettingsPage.clickWifiOnlySwitch()
        offlineSyncSettingsPage.clickTurnOff()

        with(activityRule) {
            finishActivity()
            launchActivity(null)
        }

        goToSyncSettings(data)

        offlineSyncSettingsPage.assertSyncFrequencyLabelText(R.string.weekly)
        offlineSyncSettingsPage.assertWifiOnlySwitchIsNotChecked()
    }

    private fun createMockCanvas(): MockCanvas {
        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1)
        data.offlineModeEnabled = true
        return data
    }

    private fun goToSyncSettings(data: MockCanvas = createMockCanvas()) {
        val student = data.students.first()
        val token = data.tokenFor(student).orEmpty()
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Synchronization")
    }
}
