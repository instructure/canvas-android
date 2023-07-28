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

        dashboardPage.selectGroup(group)

        groupBrowserPage.assertTitleCorrect(group)
        groupBrowserPage.selectFiles()

        fileListPage.assertPageObjects()
        }
}