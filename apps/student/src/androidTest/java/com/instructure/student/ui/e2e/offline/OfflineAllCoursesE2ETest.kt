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
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils.waitForNetworkToGoOffline
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineAllCoursesE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E, SecondaryFeatureCategory.ALL_COURSES)
    fun testOfflineAllCoursesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 3, announcements = 1)
        val student = data.studentsList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]
        val course3 = data.coursesList[2]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the 'All Courses' page and wait for it to be rendered.")
        dashboardPage.openAllCoursesPage()
        allCoursesPage.assertPageObjects()

        Log.d(STEP_TAG, "Favourite '${course1.name}' course and assert if it became favourited. Then navigate back to Dashboard page.")
        allCoursesPage.favoriteCourse(course1.name)
        allCoursesPage.assertCourseFavorited(course1)
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open global 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.openGlobalManageOfflineContentPage()
        manageOfflineContentPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the '${course1.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course1.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem(course2.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Select '${course1.name}' and '${course2.name}' courses' checkboxes and Sync them.")
        manageOfflineContentPage.changeItemSelectionState(course1.name)
        manageOfflineContentPage.changeItemSelectionState(course2.name)
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Assert that the offline sync icon is displayed on the synced  (and favorited) course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course1.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered, and assert that '${course1.name}' is the only course which is displayed on the offline mode Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertCourseNotDisplayed(course2)
        dashboardPage.assertCourseNotDisplayed(course3)

        Log.d(STEP_TAG, "Open the 'All Courses' page and wait for it to be rendered.")
        dashboardPage.openAllCoursesPage()
        allCoursesPage.assertPageObjects()

        Log.d(STEP_TAG, "Assert that the plus 'Note' box is displayed in which warns the user that favouring courses can only be done in online mode.")
        allCoursesPage.assertOfflineNoteDisplayed()

        Log.d(STEP_TAG, "Dismiss the offline 'Note' box and assert if it's disappear.")
        allCoursesPage.dismissOfflineNoteBox()
        allCoursesPage.assertOfflineNoteNotDisplayed()

        Log.d(STEP_TAG, "Assert that the select/unselect all button is not clickable because offline mode does not supports it.")
        allCoursesPage.assertSelectUnselectAllButtonNotClickable()

        Log.d(STEP_TAG, "Try to unfavorite '${course1.name}' course and assert it does not happened because favoring does not allowed in offline state.")
        allCoursesPage.unfavoriteCourse(course1.name)
        allCoursesPage.assertCourseFavorited(course1)

        Log.d(STEP_TAG, "Assert that '${course3.name}' course's details are faded (and they having 0.4 alpha value) and it's offline sync icon is not displayed since it's not synced.")
        allCoursesPage.assertCourseDetailsAlpha(course3.name, 0.4f)
        allCoursesPage.assertCourseOfflineSyncButton(course3.name, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Assert that '${course1.name}' course's favourite star is faded (and it's having 0.4 alpha value) because favoring is not possible in offline mode," +
                "the course title and open button are not faded (1.0 alpha value) and the offline sync icon is displayed since the course is synced.")
        allCoursesPage.assertCourseFavouriteStarAlpha(course1.name, 0.4f)
        allCoursesPage.assertCourseTitleAlpha(course1.name, 1.0f)
        allCoursesPage.assertCourseOpenButtonAlpha(course1.name, 1.0f)
        allCoursesPage.assertCourseOfflineSyncButton(course1.name, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Click on '${course1.name}' course and assert if it will navigate the user to the CourseBrowser Page.")
        allCoursesPage.openCourse(course1.name)
        courseBrowserPage.assertInitialBrowserTitle(course1)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}