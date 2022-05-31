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
package com.instructure.student.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import org.junit.Test

@HiltAndroidTest
class DashboardE2ETest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        // Seed data
        val data = seedData(students = 1, teachers = 1, courses = 2)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        // This will not consistently result in a badge on the email icon on the bottom of the screen.
        // So the check below is disabled. :-(
        // Seed a conversation, to give us an email in our inbox
        ConversationsApi.createConversation(
                token = teacher.token,
                recipients = listOf(student.id.toString())
        )

        // Seed some group info
        val groupCategory = GroupsApi.createCourseGroupCategory(data.coursesList[0].id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        val groupMembership = GroupsApi.createGroupMembership(group.id, student.id, teacher.token)

        // Sanity check
        assertEquals("course id for group", data.coursesList[0].id, group.courseId)

        // Sign in with lone student
        tokenLogin(student)

        // Verify that the page rendered and our courses are there
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()
        dashboardPage.assertDisplaysCourses()
        for(course in data.coursesList) {
            dashboardPage.assertDisplaysCourse(course)
        }

        // Verify that our group is displayed
        dashboardPage.assertDisplaysGroup(group, data.coursesList[0])

        // Verify that our email conversation is represented
        //dashboardPage.assertUnreadEmails(count = 1)  // Not reliable :-(
    }

}