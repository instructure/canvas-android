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
package com.instructure.student.ui.e2e.classic.offline

import android.util.Log
import androidx.test.espresso.Espresso
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils.waitForNetworkToGoOffline
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class ManageOfflineContentE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.OFFLINE_CONTENT, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testManageOfflineContentE2ETest() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 2, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course1.name, "Manage Offline Content")

        Log.d(ASSERTION_TAG, "Assert that if there is nothing selected yet, the 'SELECT ALL' button text will be displayed on the top-left corner of the toolbar.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course for sync.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(ASSERTION_TAG, "Assert that if there is something selected yet, the 'DESELECT ALL' button text will be displayed on the top-left corner of the toolbar.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(ASSERTION_TAG, "Assert that the Storage info details are displayed properly.")
        manageOfflineContentPage.assertStorageInfoDetails()

        Log.d(ASSERTION_TAG, "Assert that the tool bar texts are displayed properly, so the subtitle is '${course1.name}', because we are on the Manage Offline Content page of '${course1.name}' course.")
        manageOfflineContentPage.assertToolbarTexts(course1.name)

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Expand '${course1.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course1.name)

        Log.d(STEP_TAG, "Deselect the 'Announcements' and 'Discussions' of the '${course1.name}' course.")
        manageOfflineContentPage.changeItemSelectionState("Announcements")
        manageOfflineContentPage.changeItemSelectionState("Discussions")

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Indeterminate'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_INDETERMINATE)

        Log.d(STEP_TAG, "Select the entire '${course1.name}' course (again) for sync.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(ASSERTION_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)

        Log.d(ASSERTION_TAG, "Assert that the 'DESELECT ALL' button is still displayed because there are still more than zero item checked.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(STEP_TAG, "Deselect '${course1.name}' course's checkbox.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(ASSERTION_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(ASSERTION_TAG, "Assert that the 'SELECT ALL' button is displayed because there is no item checked.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(STEP_TAG, "Click on 'SELECT ALL' button.")
        manageOfflineContentPage.clickOnSelectAllButton()

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(ASSERTION_TAG, "Assert that the previously unchecked 'Announcements' and 'Discussions' checkboxes are became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)

        Log.d(ASSERTION_TAG, "Assert that the 'DESELECT ALL' will be displayed after clicking the 'SELECT ALL' button.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = false)

        Log.d(STEP_TAG, "Click on 'DESELECT ALL' button.")
        manageOfflineContentPage.clickOnDeselectAllButton()

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' course's checkbox state that it became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(ASSERTION_TAG, "Assert that the previously checked 'Announcements' and 'Discussions' checkboxes are became 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(ASSERTION_TAG, "Assert that the 'SELECT ALL' will be displayed after clicking the 'DESELECT ALL' button.")
        manageOfflineContentPage.assertSelectButtonText(selectAll = true)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page. Open 'Global' Manage Offline Content page.")
        Espresso.pressBack()
        dashboardPage.openGlobalManageOfflineContentPage()

        Log.d(ASSERTION_TAG, "Assert that the Storage info details are displayed properly.")
        manageOfflineContentPage.assertStorageInfoDetails()

        Log.d(ASSERTION_TAG, "Assert that the tool bar texts are displayed properly, so the subtitle is 'All Courses', because we are on the 'Global' Manage Offline Content page.")
        manageOfflineContentPage.assertToolbarTexts("All Courses")

        Log.d(ASSERTION_TAG, "Assert that the '${course1.name}' and '${course2.name}' courses' checkboxes are 'Unchecked' yet.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem(course2.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand '${course1.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course1.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Announcements' and 'Discussions' items are 'unchecked' (and so all the other tabs of the course) because the course is NOT selected.")
        manageOfflineContentPage.assertCheckedStateOfItem("Announcements", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Collapse '${course1.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course1.name)

        manageOfflineContentPage.waitForItemDisappear("Announcements")
        manageOfflineContentPage.waitForItemDisappear("Discussions")

        Thread.sleep(1000) //need to wait 1 second here because sometimes expand/collapse happens too fast

        Log.d(STEP_TAG, "Expand '${course2.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course2.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Grades' and 'Discussions' items are 'Unchecked' (and so all the other tabs of the course) because the course has not selected.")
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Grades", MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Check '${course2.name}' course.")
        manageOfflineContentPage.changeItemSelectionState(course2.name)

        Log.d(ASSERTION_TAG, "Assert that the '${course2.name}' course's checkbox state and became 'Checked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course2.name, MaterialCheckBox.STATE_CHECKED)

        Log.d(ASSERTION_TAG, "Assert that the 'Grades' and 'Discussions' items are 'checked' (and so all the other tabs of the course) because the course has selected.")
        manageOfflineContentPage.assertCheckedStateOfItem("Discussions", MaterialCheckBox.STATE_CHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem("Grades", MaterialCheckBox.STATE_CHECKED)

        Log.d(STEP_TAG, "Collapse '${course2.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course2.name)

        Log.d(ASSERTION_TAG, "Assert that both of the seeded courses are displayed as a selectable item in the Manage Offline Content page.")
        manageOfflineContentPage.assertCourseCountWithMatcher(2)

        Log.d(STEP_TAG, "Click on the 'Sync' button and confirm sync.")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course2.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course2.name}' course and open 'Grades' menu to check if it's really synced and can be seen in offline mode.")
        dashboardPage.selectCourse(course2)
        courseBrowserPage.selectGrades()

        Log.d(ASSERTION_TAG,"Assert that the empty view is displayed on the 'Grades' page (just to check that it's available in offline mode.")
        gradesPage.assertEmptyStateIsDisplayed()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device via ADB, so it will come back online.")
        turnOnConnectionViaADB()
    }

}