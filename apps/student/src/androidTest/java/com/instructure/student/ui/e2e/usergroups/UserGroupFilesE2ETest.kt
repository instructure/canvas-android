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
package com.instructure.student.ui.e2e.usergroups

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.SecondaryFeatureCategory
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class UserGroupFilesE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DASHBOARD, TestCategory.E2E, secondaryFeature = SecondaryFeatureCategory.GROUPS_FILES)
    fun testUserGroupFileControlFlow() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        setupFileOnDevice("samplepdf.pdf")

        Log.d(PREPARATION_TAG,"Seed some group info.")
        val groupCategory = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val groupCategory2 = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        val group2 = GroupsApi.createGroup(groupCategory2.id, teacher.token)

        Log.d(PREPARATION_TAG,"Create group membership for ${student.name} student.")
        GroupsApi.createGroupMembership(group.id, student.id, teacher.token)
        GroupsApi.createGroupMembership(group2.id, student.id, teacher.token)

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Assert that ${group.name} groups is displayed.")
        dashboardPage.assertDisplaysGroup(group, data.coursesList[0])
        dashboardPage.assertDisplaysGroup(group2, data.coursesList[0])

        Log.d(STEP_TAG, "Select '${group.name}' group and assert if the group title is correct on the Group Browser Page.")
        dashboardPage.selectGroup(group)
        groupBrowserPage.assertTitleCorrect(group)

        Log.d(STEP_TAG, "Select 'Files' tab within the Group Browser Page and assert that the File List Page is displayed.")
        groupBrowserPage.selectFiles()
        fileListPage.assertPageObjects()

        val testFolderName = "OneWordFolder"
        Log.d(STEP_TAG, "Click on Add ('+') button and then the 'Add Folder' icon, and create a new folder with the following name: '$testFolderName'.")
        fileListPage.clickAddButton()
        fileListPage.clickCreateNewFolderButton()
        fileListPage.createNewFolder(testFolderName)

        Log.d(STEP_TAG,"Assert that there is a folder called '$testFolderName' is displayed." +
                "Assert that the '$testFolderName' folder's size is 0, because we just created it.")
        fileListPage.assertItemDisplayed(testFolderName)
        fileListPage.assertFolderSize(testFolderName, 0)

        Log.d(STEP_TAG, "Select '$testFolderName' folder and upload a file named 'samplepdf.pdf' within it.")
        fileListPage.selectItem(testFolderName)
        fileListPage.clickAddButton()
        fileListPage.clickUploadFileButton()

        Intents.init()
        try {
            stubFilePickerIntent("samplepdf.pdf")
            fileUploadPage.chooseDevice()
        }
        finally {
            Intents.release()
        }
        fileUploadPage.clickUpload()

        Log.d(STEP_TAG, "Assert that the file upload was successful.")
        fileListPage.assertItemDisplayed("samplepdf.pdf")

        Log.d(STEP_TAG, "Navigate back to File List Page. Assert that the '$testFolderName' folder's size is 1, because we just uploaded a file in it.")
        Espresso.pressBack()
        fileListPage.assertFolderSize(testFolderName, 1)

        val testFolderName2 = "TwoWord Folder"
        Log.d(STEP_TAG, "Click on Add ('+') button and then the 'Add Folder' icon, and create a new folder with the following name: '$testFolderName2'.")
        fileListPage.clickAddButton()
        fileListPage.clickCreateNewFolderButton()
        fileListPage.createNewFolder(testFolderName2)

        Log.d(STEP_TAG,"Assert that there is a folder called '$testFolderName2' is displayed." +
                "Assert that the '$testFolderName2' folder's size is 0, because we just created it.")
        fileListPage.assertItemDisplayed(testFolderName2)
        fileListPage.assertFolderSize(testFolderName2, 0)

        Log.d(STEP_TAG, "Select '$testFolderName2' folder and assert that the empty view is displayed.")
        fileListPage.selectItem(testFolderName2)
        fileListPage.assertViewEmpty()
    }

}