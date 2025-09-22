/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.FileFolderApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.uploadTextFile
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineFilesE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.FILES, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineFilesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val testCourseFolderName = "Goodya"
        Log.d(PREPARATION_TAG, "Create a course folder within the 'Files' tab with the name: '$testCourseFolderName'.")
        val courseRootFolder = FileFolderApi.getCourseRootFolder(course.id, teacher.token)
        val courseTestFolder = FileFolderApi.createCourseFolder(courseRootFolder.id, teacher.token, testCourseFolderName)

        Log.d(PREPARATION_TAG, "Create a (text) file within the root folder (so the 'Files' tab file list) of the '${course.name}' course.")
        val rootFolderTestTextFile = uploadTextFile(courseRootFolder.id, token = teacher.token, fileUploadType = FileUploadType.COURSE_FILE)

        Log.d(PREPARATION_TAG, "Create a (text) file within the '${courseTestFolder.name}' folder of the '${course.name}' course.")
        val courseTestFolderTextFile = uploadTextFile(courseTestFolder.id, token = teacher.token, fileUploadType = FileUploadType.COURSE_FILE)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course's checkbox state is 'Unchecked'.")
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)

        Log.d(STEP_TAG, "Expand the course. Select the 'Files' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.changeItemSelectionState("Files")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        uiDevice.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(uiDevice)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and click on 'Files' tab to navigate to the File List Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectFiles()

        Log.d(ASSERTION_TAG, "Assert that under the 'Files' tab there are 2 items, a folder and a file which has been seeded recently, and there is 1 item within the folder.")
        fileListPage.assertFileListCount(2)
        fileListPage.assertFolderSize(courseTestFolder.name, 1)

        Log.d(ASSERTION_TAG, "Assert that the folder's name is '${courseTestFolder.name}' and the file's name is '${rootFolderTestTextFile.fileName}'.")
        fileListPage.assertItemDisplayed(rootFolderTestTextFile.fileName)
        fileListPage.assertItemDisplayed(courseTestFolder.name)

        Log.d(STEP_TAG, "Open '${courseTestFolder.name}' folder.")
        fileListPage.selectItem(courseTestFolder.name)

        Log.d(ASSERTION_TAG, "Assert that the '${courseTestFolderTextFile.fileName}' file is displayed within it.")
        fileListPage.assertItemDisplayed(courseTestFolderTextFile.fileName)

        Log.d(STEP_TAG, "Navigate back to File List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${rootFolderTestTextFile.fileName}', the file's name to the search input field.")
        fileListPage.searchable.clickOnSearchButton()
        fileListPage.searchable.typeToSearchBar(rootFolderTestTextFile.fileName)

        Log.d(ASSERTION_TAG, "Assert that only 1 file matches for the search text, and it is '${rootFolderTestTextFile.fileName}', and no directories has been shown in the result.")
        fileListPage.assertSearchResultCount(1)
        fileListPage.assertSearchItemDisplayed(rootFolderTestTextFile.fileName)
        fileListPage.assertItemNotDisplayed(testCourseFolderName)
        Espresso.closeSoftKeyboard()
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}