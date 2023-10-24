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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.pandautils.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyncSettingsInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION, false)
    fun testFurtherSettingsDisplayedByDefault() {
        goToSyncSettings()
        syncSettingsPage.assertFurtherSettingsIsDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION, false)
    fun testClickAutoSyncHidesFurtherSettings() {
        goToSyncSettings()
        syncSettingsPage.clickAutoSyncSwitch()
        syncSettingsPage.assertFurtherSettingsNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION, false)
    fun testChangeFrequency() {
        goToSyncSettings()
        syncSettingsPage.assertFrequencyLabelText(R.string.daily)
        syncSettingsPage.clickFrequency()
        syncSettingsPage.clickDialogOption(R.string.weekly)
        syncSettingsPage.assertFrequencyLabelText(R.string.weekly)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION, false)
    fun testChangeContentOverWifiOnly() {
        goToSyncSettings()
        syncSettingsPage.assertWifiOnlySwitchIsChecked()
        syncSettingsPage.clickWifiOnlySwitch()
        syncSettingsPage.assertDialogDisplayedWithTitle(R.string.syncSettings_wifiConfirmationTitle)
        syncSettingsPage.clickTurnOff()
        syncSettingsPage.assertWifiOnlySwitchIsNotChecked()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SYNC_SETTINGS, TestCategory.INTERACTION, false)
    fun testChangesSavedCorrectly() {
        val data = createMockCanvas()
        goToSyncSettings(data)

        syncSettingsPage.clickFrequency()
        syncSettingsPage.clickDialogOption(R.string.weekly)
        syncSettingsPage.clickWifiOnlySwitch()
        syncSettingsPage.clickTurnOff()

        with(activityRule) {
            finishActivity()
            launchActivity(null)
        }

        goToSyncSettings(data)

        syncSettingsPage.assertFrequencyLabelText(R.string.weekly)
        syncSettingsPage.assertWifiOnlySwitchIsNotChecked()
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
        settingsPage.openOfflineContentPage()
    }
}
