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

import android.os.SystemClock.sleep
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
import org.junit.Test

@HiltAndroidTest
class InboxE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxE2E() {

        // Seed basic data
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        // Create a group and put both students in it
        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        // Seed an email from the teacher to the students
        val seededConversation = ConversationsApi.createConversation(
                token = teacher.token,
                recipients = listOf(student1.id.toString(), student2.id.toString())
        ).get(0)

        // Sign in with student1
        tokenLogin(student1)

        // Make sure that the seeded conversation shows up
        dashboardPage.waitForRender()
        dashboardPage.clickInboxTab()

        inboxPage.assertPageObjects()
        inboxPage.assertConversationDisplayed(seededConversation)

        // Compose and send an email to the other student
        inboxPage.pressNewMessageButton()

        newMessagePage.populateMessage(
                course,
                student2,
                "Hey There",
                "Just checking in"
        )
        newMessagePage.hitSend()

        // Compose and send an email to a group
        inboxPage.pressNewMessageButton()

        newMessagePage.populateGroupMessage(
                group,
                student2,
                "Group Message",
                "Testing Group ${group.name}"
        )
        newMessagePage.hitSend()

        sleep(3000) // Allow time for messages to propagate

        // Now sign out and sign back in as student2
        inboxPage.goToDashboard()

        dashboardPage.waitForRender()
        dashboardPage.signOut()

        tokenLogin(student2)

        // Navigate to the inbox and verify that the expected messages are there
        dashboardPage.waitForRender()
        dashboardPage.clickInboxTab()

        inboxPage.assertConversationDisplayed(seededConversation)
        inboxPage.assertConversationDisplayed("Hey There")
        inboxPage.assertConversationDisplayed("Group Message")
    }
}