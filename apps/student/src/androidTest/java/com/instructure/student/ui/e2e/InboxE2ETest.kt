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
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.retry
import com.instructure.espresso.retryWithIncreasingDelay
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
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxSelectedButtonActionsE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} and ${student2.name} students to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId}.")
        tokenLogin(student1)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox Page. Assert that the previously seeded conversation is displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG,"Seed an email from the teacher to ${student1.name} and ${student2.name} students.")
        val seededConversation = createConversation(teacher, student1, student2)[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation)

        Log.d(STEP_TAG,"Select ${seededConversation.subject} conversation. Assert that is has not been starred already.")
        inboxPage.openConversation(seededConversation)
        inboxConversationPage.assertNotStarred()

        Log.d(STEP_TAG,"Toggle Starred to mark ${seededConversation.subject} conversation as favourite. Assert that it has became starred.")
        inboxConversationPage.toggleStarred()
        inboxConversationPage.assertStarred()

        Log.d(STEP_TAG,"Navigate back to Inbox Page and  assert that the conversation itself is starred as well.")
        Espresso.pressBack() // To main inbox page
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG,"Select ${seededConversation.subject} conversation. Mark as Unread by clicking on the 'More Options' menu, 'Mark as Unread' menu point.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)
        inboxPage.openConversation(seededConversation)
        inboxConversationPage.markUnread() //After select 'Mark as Unread', we will be navigated back to Inbox Page

        Log.d(STEP_TAG,"Assert that ${seededConversation.subject} conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG,"Select ${seededConversation.subject} conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seededConversation)
        inboxConversationPage.archive() //After select 'Archive', we will be navigated back to Inbox Page

        Log.d(STEP_TAG,"Assert that ${seededConversation.subject} conversation has removed from 'All' tab.") //TODO: Discuss this logic if it's ok if we don't show Archived messages on 'All' tab...
        inboxPage.assertConversationNotDisplayed(seededConversation)

        Log.d(STEP_TAG,"Select 'Archived' conversation filter.")
        inboxPage.filterInbox("Archived")

        Log.d(STEP_TAG,"Assert that ${seededConversation.subject} conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seededConversation)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation. Assert that the selected number of conversations on the toolbar is 1." +
                "Unarchive it, and assert that it is not displayed in the 'ARCHIVED' scope any more.")
        inboxPage.selectConversation(seededConversation)
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.clickUnArchive()

        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        sleep(2000)

        Log.d(STEP_TAG,"Navigate to 'INBOX' scope and assert that ${seededConversation.subject} conversation is displayed.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversations (${seededConversation.subject} and star it." +
                "Assert that the selected number of conversations on the toolbar is 1 and the conversation is starred.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.clickUnstar()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationNotStarred(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Select the conversations (${seededConversation.subject} and archive it. Assert that it has not displayed in the 'INBOX' scope.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickArchive()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        sleep(2000)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and assert that the conversation is displayed there.")
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope and assert that the conversation is displayed there, because a conversation cannot be archived and unread at the same time.")
        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope and assert that the conversation is NOT displayed there.")
        inboxPage.filterInbox("Starred")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Navigate to 'INBOX' scope and assert that ${seededConversation.subject} conversation is NOT displayed because it is archived yet.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and Select the conversation. Star it, and assert that it has displayed in the 'STARRED' scope.")
        inboxPage.filterInbox("Archived")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickStar()
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation. Unarchive it, and assert that it has not displayed in the 'ARCHIVED' scope.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickUnArchive()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope and assert that the conversations is displayed there.")
        inboxPage.filterInbox("Starred")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        sleep(2000)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and assert that the conversation is displayed there because it is not archived yet.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxMessageComposeReplyAndOptionMenuActionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} and ${student2.name} students to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId}.")
        tokenLogin(student1)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox Page. Assert that the previously seeded conversation is displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG,"Seed an email from the teacher to ${student1.name} and ${student2.name} students.")
        val seededConversation = createConversation(teacher, student1, student2)[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation)

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject = "Hey There"
        val newMessage = "Just checking in"
        Log.d(STEP_TAG,"Create a new message with subject: $newMessageSubject, and message: $newMessage")
        newMessagePage.populateMessage(course, student2, newMessageSubject, newMessage)

        Log.d(STEP_TAG,"Click on 'Send' button.")
        newMessagePage.clickSend()

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newGroupMessageSubject = "Group Message"
        val newGroupMessage = "Testing Group ${group.name}"
        Log.d(STEP_TAG,"Create a new message with subject: $newGroupMessageSubject, and message: $newGroupMessage")
        newMessagePage.populateGroupMessage(group, student2, newGroupMessageSubject, newGroupMessage)

        Log.d(STEP_TAG,"Click on 'Send' button.")
        newMessagePage.clickSend()

        sleep(2000) // Allow time for messages to propagate

        Log.d(STEP_TAG,"Navigate back to Dashboard Page.")
        inboxPage.goToDashboard()
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Log out with ${student1.name} student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG,"Login with user: ${student2.name}, login id: ${student2.loginId}.")
        tokenLogin(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open Inbox Page. Assert that both, the previously seeded 'normal' conversation and the group conversation are displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationDisplayed(seededConversation)
        inboxPage.assertConversationDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed("Group Message")

        Log.d(STEP_TAG,"Select $newGroupMessageSubject conversation.")
        inboxPage.openConversation(newMessageSubject)
        val newReplyMessage = "This is a quite new reply message."
        Log.d(STEP_TAG,"Reply to $newGroupMessageSubject conversation with '$newReplyMessage' message. Assert that the reply is displayed.")
        inboxConversationPage.replyToMessage(newReplyMessage)

        Log.d(STEP_TAG,"Delete $newReplyMessage reply and assert is has been deleted.")
        inboxConversationPage.deleteMessage(newReplyMessage)
        inboxConversationPage.assertMessageNotDisplayed(newReplyMessage)

        Log.d(STEP_TAG,"Delete the whole '$newGroupMessageSubject' subject and assert that it has been removed from the conversation list on the Inbox Page.")
        inboxConversationPage.deleteConversation() //After deletion we will be navigated back to Inbox Page
        inboxPage.assertConversationNotDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed(seededConversation)
        inboxPage.assertConversationDisplayed("Group Message")

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and seledct '$newGroupMessageSubject' conversation.")
        inboxPage.filterInbox("Inbox")
        inboxPage.selectConversation(newGroupMessageSubject)

        Log.d(STEP_TAG, "Delete the '$newGroupMessageSubject' conversation and assert that it has been removed from the 'INBOX' scope.")
        inboxPage.clickDelete()
        inboxPage.confirmDelete()
        inboxPage.assertConversationNotDisplayed(newGroupMessageSubject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxSwipeGesturesE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} and ${student2.name} students to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG,"Login with user: ${student1.name}, login id: ${student1.loginId}.")
        tokenLogin(student1)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox Page. Assert that the previously seeded conversation is displayed.")
        dashboardPage.clickInboxTab()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG,"Seed an email from the teacher to ${student1.name} and ${student2.name} students.")
        val seededConversation = createConversation(teacher, student1, student2)[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right to make it read. Assert that the conversation became read.")
        inboxPage.selectConversation(seededConversation.subject)
        inboxPage.swipeConversationRight(seededConversation)
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right again to make it unread. Assert that the conversation became unread.")
        inboxPage.swipeConversationRight(seededConversation)
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'INBOX' scope because it has became archived.")
        inboxPage.swipeConversationLeft(seededConversation)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope. Assert that the '${seededConversation.subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.swipeConversationLeft(seededConversation)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope. Assert that the '${seededConversation.subject}' conversation is displayed in the 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation. Star it and mark it unread. (Preparing for swipe gestures in 'STARRED' and 'UNREAD' scope.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.clickStar()
        inboxPage.assertConversationStarred(seededConversation.subject)
        inboxPage.clickMarkAsUnread()

        sleep(1000)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope. Assert that the conversation is displayed in the 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        retry(times = 10, delay = 3000) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'STARRED' scope because it has became unstarred.")
        inboxPage.swipeConversationLeft(seededConversation)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope. Assert that the conversation is displayed in the 'Unread' scope.")
        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' conversation right and assert that it has disappeared from the 'UNREAD' scope.")
        inboxPage.swipeConversationRight(seededConversation.subject)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E)
    fun testHelpMenuAskYourInstructorMessage() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Help Menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(STEP_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        val questionText = "Can you see message this Instructor?"
        val recipientList = student.shortName + ", " + teacher.shortName

        Log.d(STEP_TAG, "Send the '$questionText' question to the instructor (${teacher.shortName}) from the student (${student.shortName}).")
        helpPage.sendQuestionToInstructor(course, questionText)

        Log.d(STEP_TAG, "Dismiss 'Ask Your Instructor' dialog. Open Inbox Page. Navigate to 'SENT' scope and assert that the conversations is displayed there with the proper recipients.")
        Espresso.pressBack()
        dashboardPage.clickInboxTab()
        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationWithRecipientsDisplayed(recipientList)

        Log.d(STEP_TAG, "Open the conversation and assert that the message body is equal to which the student asked in the 'Ask Your Instructor' dialog: '$questionText'.")
        inboxPage.openConversationWithRecipients(recipientList)
        inboxConversationPage.assertMessageDisplayed(questionText)

        Log.d(STEP_TAG,"Log out with ${student.name} student.")
        Espresso.pressBack()
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG,"Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open Inbox Page. Assert that the asked question is displayed in the teacher's inbox with the proper recipients ($recipientList) and message ($questionText).")
        dashboardPage.clickInboxTab()
        inboxPage.assertConversationWithRecipientsDisplayed(recipientList)
        inboxPage.assertConversationDisplayed(questionText)

        Log.d(STEP_TAG, "Open the conversation and assert that there is no subject of the conversation and the message body is equal to which the student typed in the 'Ask Your Instructor' dialog: '$questionText'.")
        inboxPage.openConversationWithRecipients(recipientList)
        inboxConversationPage.assertMessageDisplayed(questionText)
        inboxConversationPage.assertNoSubjectDisplayed()
    }

    private fun createConversation(
        teacher: CanvasUserApiModel,
        student1: CanvasUserApiModel,
        student2: CanvasUserApiModel
    ) = ConversationsApi.createConversation(
        token = teacher.token,
        recipients = listOf(student1.id.toString(), student2.id.toString())
    )
}