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
import android.util.Log
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

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        // Create a group and put both students in it
        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} and ${student2.name} students to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(PREPARATION_TAG,"Seed an email from the teacher to ${student1.name} and ${student2.name} students.")
        val seededConversation = ConversationsApi.createConversation(
                token = teacher.token,
                recipients = listOf(student1.id.toString(), student2.id.toString())
        ).get(0)

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId} , password: ${student1.password}")
        tokenLogin(student1)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open Inbox Page. Assert that the previously seeded conversation is displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertPageObjects() //TODO: Refactor to assert to the empty view just like in teacher would be better. AFTER THAT, seed the conversation.
        inboxPage.assertConversationDisplayed(seededConversation)

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject = "Hey There"
        val newMessage = "Just checking in"
        Log.d(STEP_TAG,"Create a new message with subject: $newMessageSubject, and message: $newMessage")
        newMessagePage.populateMessage(
                course,
                student2,
                newMessageSubject,
                newMessage
        )

        Log.d(STEP_TAG,"Click on 'Send' button.")
        newMessagePage.clickSend()

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newGroupMessageSubject = "Group Message"
        val newGroupMessage = "Testing Group ${group.name}"
        Log.d(STEP_TAG,"Create a new message with subject: $newGroupMessageSubject, and message: $newGroupMessage")
        newMessagePage.populateGroupMessage(
                group,
                student2,
                newGroupMessageSubject,
                newGroupMessage
        )

        Log.d(STEP_TAG,"Click on 'Send' button.")
        newMessagePage.clickSend()

        sleep(3000) // Allow time for messages to propagate

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        inboxPage.goToDashboard()
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Log out with ${student1.name} student.")
        dashboardPage.logOut()

        Log.d(STEP_TAG,"Login with user: ${student2.name}, login id: ${student2.loginId} , password: ${student2.password}")
        tokenLogin(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open Inbox Page. Assert that both, the previously seeded 'normal' conversation and the group conversation are displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(seededConversation)
        inboxPage.assertConversationDisplayed("Hey There")
        inboxPage.assertConversationDisplayed("Group Message")
    }
}