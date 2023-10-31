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
package com.instructure.student.ui.e2e.offline

import android.util.Log
import androidx.test.espresso.Espresso
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class ManageOfflineContentE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.OFFLINE_CONTENT, TestCategory.E2E)
    fun testManageOfflineContentE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]
        val testAnnouncement = data.announcementsList[0]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course1.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Assert that if there is nothing selected yet, the 'SELECT ALL' button text will be displayed on the top-left corner of the toolbar.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(STEP_TAG, "Assert that if there is something selected yet, the 'DESELECT ALL' button text will be displayed on the top-left corner of the toolbar.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(STEP_TAG, "Click on the 'Sync' button.")
        manageOfflineContentPage.clickOnSyncButton()

        Log.d(STEP_TAG, "Wait for the 'Download Started' dashboard notification to be displayed, and the to disappear.")
        dashboardPage.waitForRender()
        dashboardPage.waitForSyncProgressDownloadStartedNotification()
        dashboardPage.waitForSyncProgressDownloadStartedNotificationToDisappear()

        Log.d(STEP_TAG, "Wait for the 'Syncing Offline Content' dashboard notification to be displayed, and the to disappear. (It should be displayed after the 'Download Started' notification immediately.)")
        dashboardPage.waitForSyncProgressStartingNotification()
        dashboardPage.waitForSyncProgressStartingNotificationToDisappear()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course1.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Assert that the Storage info details are displayed properly.")
        manageOfflineContentPage.assertStorageInfoDetails()

        Log.d(STEP_TAG, "Assert that the tool bar texts are displayed properly, so the subtitle is '${course1.name}', because we are on the Manage Offline Content page of '${course1.name}' course.")
        manageOfflineContentPage.assertToolbarTexts(course1.name)

        Log.d(STEP_TAG, "Deselect the 'Announcements' and 'Discussions' of the '${course1.name}' course.")
        manageOfflineContentPage.changeItemSelectionState("Announcements")
        manageOfflineContentPage.changeItemSelectionState("Discussions")

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Indeterminate'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_INDETERMINATE)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course (again) for sync.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Assert that the 'DESELECT ALL' button is still displayed because there are still more than zero item checked.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(STEP_TAG, "Deselect '${course1.name}' course's checkbox.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Assert that the 'SELECT ALL' button is displayed because there is no item checked.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(STEP_TAG, "Click on 'SELECT ALL' button.")
        manageOfflineContentPage.clickOnSelectAllButton()

        manageOfflineContentPage.expandCollapseItem(course1.name)
        manageOfflineContentPage.expandCollapseItem(course1.name)

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Assert that the 'DESELECT ALL' will be displayed after clicking the 'SELECT ALL' button.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(STEP_TAG, "Click on 'DESELECT ALL' button.")
        manageOfflineContentPage.clickOnDeselectAllButton()

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state that it became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Assert that the previously checked 'Announcements' and 'Discussions' checkboxes are became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Assert that the 'SELECT ALL' will be displayed after clicking the 'SELECT ALL' button.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page and confirm 'Discard Changes' dialog. Open 'Global' Manage Offline Content page.")
        Espresso.pressBack()
        manageOfflineContentPage.confirmDiscardChanges()
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(STEP_TAG, "Assert that the Storage info details are displayed properly.")
        manageOfflineContentPage.assertStorageInfoDetails()

        Log.d(STEP_TAG, "Assert that the tool bar texts are displayed properly, so the subtitle is 'All Courses', because we are on the 'Global' Manage Offline Content page.")
        manageOfflineContentPage.assertToolbarTexts("All Courses")

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state is still 'Checked' even on the 'Global' Manage Offline Content page. Assert that '${course2.name}' course's checkbox is 'Unchecked' yet.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem(course2.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand '${course1.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course1.name)

        Log.d(STEP_TAG, "Assert that the 'Announcements' and 'Discussions' items are 'checked' (and so all the other tabs of the course) because the course has been selected.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Collapse '${course1.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course1.name)

        Log.d(STEP_TAG, "Expand '${course2.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course2.name)

        Log.d(STEP_TAG, "Assert that the 'Announcements' and 'Discussions' items are 'Unchecked' (and so all the other tabs of the course) because the course has not selected.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Check '${course2.name}' course.")
        manageOfflineContentPage.changeItemSelectionState(course2.name)

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Assert that the '${course2.name}' course's checkbox state and became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Assert that both of the seeded courses are displayed as a selectable item in the Manage Offline Content page.")
        manageOfflineContentPage.assertCourseCount(2)

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        OfflineTestUtils.turnOffConnectionViaADB()
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course1.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course1)
        courseBrowserPage.selectAnnouncements()

        Log.d(STEP_TAG,"Assert that the '${testAnnouncement.title}' titled announcement is displayed, so the user is able to see it in offline mode because it was synced.")
        announcementListPage.assertTopicDisplayed(testAnnouncement.title)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device via ADB, so it will come back online.")
        OfflineTestUtils.turnOnConnectionViaADB()
    }

}