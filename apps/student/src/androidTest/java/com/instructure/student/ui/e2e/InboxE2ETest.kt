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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxE2ETest: StudentComposeTest() {
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

        Log.d(PREPARATION_TAG, "Create a course group category and a group based on that category.")
        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' and '${student2.name}' students to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        tokenLogin(student1)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since we did not make any conversation yet.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an email from the teacher to '${student1.name}' and '${student2.name}' students.")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(student1.id.toString(), student2.id.toString()))[0]

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation.")
        inboxPage.openConversation(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that is has not been starred already.")
        inboxDetailsPage.assertStarred(false)

        Log.d(STEP_TAG, "Toggle Starred to mark '${seededConversation.subject}' conversation as favourite.")
        inboxDetailsPage.pressStarButton(true)

        Log.d(ASSERTION_TAG, "Assert that the conversation is starred.")
        inboxDetailsPage.assertStarred(true)

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack() // To main inbox page

        Log.d(ASSERTION_TAG, "Assert that the conversation itself is starred as well.")
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation. Mark as Unread by clicking on the 'More Options' menu, 'Mark as Unread' menu point.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Mark as Unread")

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select 'Archived' conversation filter.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation.")
        inboxPage.selectConversation(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversations on the toolbar is 1.")
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Click on the 'Mark as Unread' button.")
        inboxPage.clickMarkAsUnread()

        Log.d(ASSERTION_TAG, "Assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select 'Unread' conversation filter.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is displayed on the 'Inbox' tab.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Assert that '${seededConversation.subject}' conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack()
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select 'Archived' conversation filter and refresh it.")
        inboxPage.filterInbox("Archived")
        refresh()

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation does not have the unread mark because an archived conversation cannot be unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation.")
        inboxPage.selectConversation(seededConversation.subject)

        Log.d(STEP_TAG, "Click on the 'Unarchive' button.")
        inboxPage.clickUnArchive()

        Log.d(ASSERTION_TAG, "Assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        sleep(2000)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is displayed.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation '${seededConversation.subject}'.")
        inboxPage.selectConversations(listOf(seededConversation.subject))

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversations on the toolbar is 1.")
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Unstar the conversation.")
        inboxPage.clickUnstar()

        Log.d(ASSERTION_TAG, "Assert that the conversation is not starred.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationNotStarred(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Select the conversation '${seededConversation.subject}' and archive it.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickArchive()

        Log.d(ASSERTION_TAG, "Assert that it has not displayed in the 'INBOX' scope.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that the conversation is NOT displayed there, because a conversation cannot be archived and unread at the same time.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that the conversation is NOT displayed there.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")
        sleep(2000)

        Log.d(ASSERTION_TAG, "Assert that '${seededConversation.subject}' conversation is NOT displayed because it is archived yet.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and Select the conversation. Star it.")
        inboxPage.filterInbox("Archived")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickStar()

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'STARRED' scope.")
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation. Unarchive it.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickUnArchive()

        Log.d(ASSERTION_TAG, "Assert that the conversation is not displayed in the 'ARCHIVED' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'STARRED' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'INBOX' scope because it is not archived yet..")
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

        Log.d(PREPARATION_TAG, "Create a course group category and a group based on that category.")
        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' and '${student2.name}' students to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        tokenLogin(student1)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since we did not make any conversation yet.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an email from the teacher to '${student1.name}' and '${student2.name}' students.")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(student1.id.toString(), student2.id.toString()))[0]

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject = "Hey There"
        val newMessage = "Just checking in"
        Log.d(STEP_TAG, "Create a new message with subject: '$newMessageSubject', and message: '$newMessage'")
        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(course.name)
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Students")
        recipientPickerPage.pressLabel(student2.shortName)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject(newMessageSubject)
        inboxComposePage.typeBody(newMessage)

        Log.d(STEP_TAG, "Click on 'Send' button.")
        inboxComposePage.pressSendButton()

        composeTestRule.waitForIdle()
        sleep(1000) // Allow time for messages to propagate

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newGroupMessageSubject = "Group Message"
        val newGroupMessage = "Testing Group ${group.name}"
        Log.d(STEP_TAG, "Create a new message with subject: '$newGroupMessageSubject', and message: '$newGroupMessage'")
        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(course.name)
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Students")
        recipientPickerPage.pressLabel(student2.shortName)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject(newGroupMessageSubject)
        inboxComposePage.typeBody(newGroupMessage)
        inboxComposePage.pressIndividualSendSwitch()

        Log.d(STEP_TAG, "Click on 'Send' button.")
        inboxComposePage.pressSendButton()

        sleep(2000) // Allow time for messages to propagate

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        dashboardPage.goToDashboard()
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Log out with '${student1.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${student2.name}', login id: '${student2.loginId}'.")
        tokenLogin(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(ASSERTION_TAG, "Assert that both, the previously seeded 'normal' conversation and the group conversation are displayed.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)
        inboxPage.assertConversationDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed("Group Message")

        Log.d(STEP_TAG, "Select '$newGroupMessageSubject' conversation.")
        inboxPage.openConversation(newMessageSubject)

        val newReplyMessage = "This is a quite new reply message."
        Log.d(STEP_TAG, "Reply to '$newGroupMessageSubject' conversation with '$newReplyMessage' message.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply")
        inboxComposePage.typeBody(newReplyMessage)
        inboxComposePage.pressSendButton()

        Log.d(STEP_TAG, "Delete '$newReplyMessage' reply.")
        inboxDetailsPage.pressOverflowMenuItemForMessage(newReplyMessage, "Delete")
        inboxDetailsPage.pressAlertButton("Delete")

        Log.d(ASSERTION_TAG, "Assert that the '$newReplyMessage' message is not displayed because it has been deleted.")
        inboxDetailsPage.assertMessageNotDisplayed(newReplyMessage)

        Log.d(STEP_TAG, "Delete the whole '$newGroupMessageSubject' subject.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.pressAlertButton("Delete")

        Log.d(ASSERTION_TAG, "Assert that it has been removed from the conversation list on the Inbox Page.")
        inboxPage.assertConversationNotDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed(seededConversation.subject)
        inboxPage.assertConversationDisplayed("Group Message")

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and select '$newGroupMessageSubject' conversation.")
        inboxPage.filterInbox("Inbox")
        inboxPage.selectConversation(newGroupMessageSubject)

        Log.d(STEP_TAG, "Delete the '$newGroupMessageSubject' conversation and assert that it has been removed from the 'INBOX' scope.")
        inboxPage.clickDelete()
        inboxPage.confirmDelete()

        Log.d(ASSERTION_TAG, "Assert that the '$newGroupMessageSubject' conversation is not displayed because it has been deleted.")
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

        Log.d(PREPARATION_TAG, "Create a course group category and a group based on that category.")
        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)

        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' and '${student2.name}' students to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student2.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${student1.name}', login id: '${student1.loginId}'.")
        tokenLogin(student1)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed since we did not make any conversation yet.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an email from the teacher to '${student1.name}' and '${student2.name}' students.")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(student1.id.toString(), student2.id.toString()))[0]

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right to make it read.")
        inboxPage.selectConversation(seededConversation.subject)
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation is not displayed in the 'INBOX' scope because it has became read.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right again to make it unread.")
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation became unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is not displayed in the 'INBOX' scope because it has became archived.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is not displayed in the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is displayed in the 'INBOX' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation.")
        inboxPage.selectConversations(listOf(seededConversation.subject))

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversations on the toolbar is 1.")
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Star the conversation.")
        inboxPage.clickStar()

        Log.d(ASSERTION_TAG, "Assert that the conversation is starred.")
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG, "Mark the conversation as Unread.")
        inboxPage.clickMarkAsUnread()

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'STARRED' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is not displayed in the 'STARRED' scope because it has became unstarred.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is displayed in the 'UNREAD' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' conversation right.")
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is not displayed in the 'UNREAD' scope because it has became read.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E)
    @Stub
    fun testHelpMenuAskYourInstructorMessage() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Help Menu.")
        leftSideNavigationDrawerPage.clickHelpMenu()

        Log.d(ASSERTION_TAG, "Assert Help Menu Dialog is displayed.")
        helpPage.assertHelpMenuDisplayed()

        val questionText = "Can you see message this Instructor?"
        val recipientList = student.shortName + ", " + teacher.shortName
        Log.d(STEP_TAG, "Send the '$questionText' question to the instructor ('${teacher.shortName}') from the student ('${student.shortName}').")
        helpPage.sendQuestionToInstructor(course, questionText)

        Log.d(STEP_TAG, "Navigate back to Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open Inbox Page. Navigate to 'SENT' scope.")
        dashboardPage.clickInboxTab()
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that the conversations is displayed there with the proper recipients.")
        inboxPage.assertConversationWithRecipientsDisplayed(recipientList)

        Log.d(STEP_TAG, "Open the conversation.")
        inboxPage.openConversationWithRecipients(recipientList)

        Log.d(ASSERTION_TAG, "Assert that the message body is equal to which the student asked in the 'Ask Your Instructor' dialog: '$questionText'.")
        inboxDetailsPage.assertMessageDisplayed(questionText)

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Log out with '${student.name}' student.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.clickInboxTab()

        Log.d(ASSERTION_TAG, "Assert that the asked question is displayed in the teacher's inbox with the proper recipients ('$recipientList'), subject and message ('$questionText').")
        inboxPage.assertConversationWithRecipientsDisplayed(recipientList)
        inboxPage.assertConversationSubject("(No Subject)")
        inboxPage.assertConversationDisplayed(questionText)

        Log.d(STEP_TAG, "Open the conversation.")
        inboxPage.openConversationWithRecipients(recipientList)

        Log.d(ASSERTION_TAG, "Assert that there is no subject of the conversation and the message body is equal to which the student typed in the 'Ask Your Instructor' dialog: '$questionText'.")
        inboxDetailsPage.assertMessageDisplayed(questionText)
        inboxDetailsPage.assertConversationSubject("")
    }

}