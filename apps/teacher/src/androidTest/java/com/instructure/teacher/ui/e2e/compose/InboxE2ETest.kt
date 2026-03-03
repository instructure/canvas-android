/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.teacher.ui.e2e.compose

import android.os.Environment
import android.os.SystemClock.sleep
import android.util.Log
import androidx.media3.ui.R
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.getVideoPosition
import com.instructure.espresso.retry
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File

@HiltAndroidTest
class InboxE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

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
        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' student to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the dashboard displays the course: '${course.name}' ")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that Inbox is empty.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        val seedConversation = ConversationsApi.createConversationForCourse(
            token = student1.token,
            courseId = course.id,
            recipients = listOf(teacher.id.toString())
        )

        Log.d(STEP_TAG, "Refresh the page.")
        inboxPage.refreshInbox()

        Log.d(ASSERTION_TAG, "Assert that the previously seeded Inbox conversation is displayed. " +
                "Assert that the message is unread yet.")
        inboxPage.assertHasConversation()
        inboxPage.assertThereIsAnUnreadMessage(true)

        val replyMessage = "Hello there"
        Log.d(STEP_TAG, "Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.openConversation(seedConversation[0].subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply")
        inboxComposePage.typeBody(replyMessage)
        inboxComposePage.pressSendButton()

        Log.d(ASSERTION_TAG, "Assert that the reply has successfully sent and it's displayed.")
        inboxDetailsPage.assertMessageDisplayed(replyMessage)
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Navigate back to Inbox Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the message is not unread anymore.")
        inboxPage.assertThereIsAnUnreadMessage(false)

        Log.d(STEP_TAG, "Add a new conversation message manually via UI. Click on 'New Message' ('+') button.")
        inboxPage.pressNewMessageButton()

        Log.d(STEP_TAG, "Select '${course.name}' from course spinner. Click on the '+' icon next to the recipients input field. Select the two students: '${student1.name}' and '${student2.name}'. Click on 'Done'.")
        addNewMessage(course, data.studentsList)
        composeTestRule.waitForIdle()

        val subject = "Hello there"
        val body = "General Kenobi"
        Log.d(STEP_TAG, "Fill in the 'Subject' field with the value: '$subject'. Add some message text and click on 'Send' (aka. 'Arrow') button.")
        inboxComposePage.typeSubject(subject)
        inboxComposePage.typeBody(body)
        inboxComposePage.pressSendButton()
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Filter the Inbox by selecting 'Sent' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that the previously sent conversation is displayed.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Click on '$subject' conversation.")
        inboxPage.openConversation(subject)

        val replyMessageTwo = "Test Reply 2"
        Log.d(STEP_TAG, "Click on 'Reply' button. Write a reply with the message: '$replyMessageTwo'.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply")
        inboxComposePage.typeBody(replyMessageTwo)
        inboxComposePage.pressSendButton()
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the reply has successfully sent and it's displayed.")
        inboxDetailsPage.assertMessageDisplayed(replyMessageTwo)

        Log.d(STEP_TAG, "Navigate back after it has opened.")
        composeTestRule.waitForIdle()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the conversation is still displayed on the Inbox Page after opening it.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Filter the Inbox by selecting 'Inbox' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the previously seeded Inbox conversation is displayed.")
        inboxPage.refreshInbox()
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.openConversation(seedConversation[0].subject)

        Log.d(STEP_TAG, "Star the conversation and navigate back to Inbox Page.")
        inboxDetailsPage.pressStarButton(true)
        composeTestRule.waitForIdle()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0].subject}' conversation has been starred.")
        inboxPage.assertConversationStarred(seedConversation[0].subject)

        Log.d(STEP_TAG, "Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.openConversation(seedConversation[0].subject)

        Log.d(STEP_TAG, "Archive the '${seedConversation[0]}' conversation.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        composeTestRule.waitForIdle()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that it has disappeared from the list, because archived conversations does not displayed within the 'Inbox' section.")
        dashboardPage.assertPageObjects()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG, "Filter the Inbox by selecting 'Archived' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that the previously archived conversation is displayed.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Filter the Inbox by selecting 'Starred' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0]}' conversation is displayed because it's still starred.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Click on the conversation.")
        inboxPage.openConversation(seedConversation[0].subject)

        Log.d(STEP_TAG, "Remove star from the conversation and navigate back to Inbox Page.")
        inboxDetailsPage.pressStarButton(false)
        composeTestRule.waitForIdle()
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0]}' conversation is disappeared because it's not starred yet.")
        dashboardPage.assertPageObjects()
        inboxPage.assertInboxEmpty()
    }

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
        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' student to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the dashboard displays the course: '${course.name}'.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that Inbox is empty.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        val seedConversation = ConversationsApi.createConversation(token = student1.token, recipients = listOf(teacher.id.toString()))

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the conversation displayed as unread.")
        inboxPage.refreshInbox()
        inboxPage.assertThereIsAnUnreadMessage(true)

        Log.d(PREPARATION_TAG, "Seed another Inbox conversation via API.")
        val seedConversation2 = ConversationsApi.createConversation(token = student1.token, recipients = listOf(teacher.id.toString()), subject = "Second conversation", body = "Second body")

        Log.d(PREPARATION_TAG, "Seed a third Inbox conversation via API.")
        val seedConversation3 = ConversationsApi.createConversation(token = student2.token, recipients = listOf(teacher.id.toString()), subject = "Third conversation", body = "Third body")

        Log.d(STEP_TAG, "Refresh the page. Filter the Inbox by selecting 'Inbox' category from the spinner on Inbox Page.")
        inboxPage.refreshInbox()
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0]}' conversation is displayed. Assert that the conversation is unread yet.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation. Unarchive it.")
        inboxPage.selectConversation(seedConversation2[0].subject)
        inboxPage.clickArchive()

        Log.d(ASSERTION_TAG, "Assert that it is not displayed in the 'ARCHIVED' scope any more.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            inboxPage.filterInbox("Inbox")
            inboxPage.filterInbox("Archived")
            refresh()
        })
        {
            inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        }

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation.")
        inboxPage.selectConversation(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversation on the toolbar is 1 and '${seedConversation2[0].subject}' conversation is not displayed in the 'ARCHIVED' scope.")
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Click on the 'Mark as Unread' button.")
        inboxPage.clickMarkAsUnread()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            inboxPage.selectConversation(seedConversation2[0].subject)
            inboxPage.assertSelectedConversationNumber("1")
            inboxPage.clickMarkAsUnread()
            refresh()
        }) {
            Log.d(ASSERTION_TAG, "Assert that the empty view will be displayed and the '${seedConversation2[0].subject}' conversation is not because it should disappear from 'Archived' list.")
            inboxPage.assertInboxEmpty()
            inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        }

        Log.d(STEP_TAG, "Select 'Unread' conversation filter.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation is displayed on the 'Inbox' tab.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seedConversation2[0].subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation has removed from 'Inbox' tab.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select 'Archived' conversation filter.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation is displayed by the 'Archived' filter.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            inboxPage.filterInbox("Inbox")
            refresh()
            inboxPage.openConversation(seedConversation2[0].subject)
            inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
            inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
            Espresso.pressBack()
            inboxPage.filterInbox("Archived")
            refresh()
        }) {
            refresh()
            inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        }

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation does not have the unread mark because an archived conversation cannot be unread.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation.")
        inboxPage.selectConversation(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Click on the 'Unarchive' button.")
        inboxPage.clickUnArchive()

        Log.d(ASSERTION_TAG, "Assert that the empty view will be displayed and the '${seedConversation2[0].subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation is displayed.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations '${seedConversation[0].subject}' and '${seedConversation2[0].subject}' and star them.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickStar()

        Log.d(ASSERTION_TAG, "Assert that both of the has been starred and the selected number of conversations on the toolbar shows 2.")
        inboxPage.assertSelectedConversationNumber("2")
        inboxPage.assertConversationStarred(seedConversation2[0].subject)
        inboxPage.assertConversationStarred(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Mark them as read (since if at least there is one unread selected, we are showing the 'Mark as Read' icon).")
        inboxPage.clickMarkAsRead()

        Log.d(ASSERTION_TAG, "Assert that both of them are read.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.GONE)
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Mark them as unread.")
        inboxPage.clickMarkAsUnread()

        Log.d(ASSERTION_TAG, "Assert that both of them will became unread.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Archive both of them.")
        inboxPage.clickArchive()

        Log.d(ASSERTION_TAG, "Assert that non of them are displayed in the 'INBOX' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            inboxPage.filterInbox("Inbox")
            inboxPage.filterInbox("Archived")
            refresh()
            inboxPage.clickArchive()
        })
        {
            inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
            inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)
        }

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that both of the conversations are displayed there.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that none of the conversations are displayed there, because a conversation cannot be archived and unread at the same time.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that both of the conversations are displayed there.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations and unstar them.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickUnstar()

        Log.d(ASSERTION_TAG, "Assert that none of them are displayed in the 'STARRED' scope.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that both of the conversations are displayed there.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations and unarchive them.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickUnArchive()

        Log.d(ASSERTION_TAG, "Assert that none of them are displayed in the 'ARCHIVED' scope.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that both of the conversations are displayed there.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)
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
        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' student to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the dashboard displays the course: '${course.name}'.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that Inbox is empty.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        ConversationsApi.createConversation(token = student1.token, recipients = listOf(teacher.id.toString()))

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the previously seeded Inbox conversation is displayed. Assert that the message is unread yet.")
        inboxPage.refreshInbox()
        inboxPage.assertHasConversation()
        inboxPage.assertThereIsAnUnreadMessage(true)

        Log.d(PREPARATION_TAG, "Seed another Inbox conversation via API.")
        val seedConversation2 = ConversationsApi.createConversation(
            token = student1.token,
            recipients = listOf(teacher.id.toString()),
            subject = "Second conversation",
            body = "Second body"
        )

        Log.d(PREPARATION_TAG, "Seed a third Inbox conversation via API.")
        val seedConversation3 = ConversationsApi.createConversation(
            token = student2.token,
            recipients = listOf(teacher.id.toString()),
            subject = "Third conversation",
            body = "Third body"
        )

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation and swipe it right to make it read.")
        inboxPage.refreshInbox()
        inboxPage.selectConversation(seedConversation2[0].subject)
        inboxPage.swipeConversationRight(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation became read.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation and swipe it right again to make it unread.")
        inboxPage.swipeConversationRight(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation became unread.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left.")
        inboxPage.swipeConversationLeft(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'INBOX' scope because it has became archived.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left.")
        inboxPage.swipeConversationLeft(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'INBOX' scope.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations. Mark the unread.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickMarkAsRead()

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation3[0].subject}' conversation is read.")
        retry(times = 10, delay = 3000, block = {
            inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.GONE)
        })

        Log.d(STEP_TAG, "Star them.")
        inboxPage.clickStar()

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that both of the conversation are displayed in the 'STARRED' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
            inboxPage.assertConversationDisplayed(seedConversation3[0].subject)
        }

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left.")
        inboxPage.swipeConversationLeft(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'STARRED' scope because it has became unstarred.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation3[0].subject}' conversation right.")
        inboxPage.swipeConversationRight(seedConversation3[0].subject)

        Log.d(ASSERTION_TAG, "Assert that it has became unread.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that only the '${seedConversation3[0].subject}' conversation is displayed in the 'UNREAD' scope.")
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation3[0].subject}' conversation left.")
        inboxPage.swipeConversationLeft(seedConversation3[0].subject)

        Log.d(ASSERTION_TAG, "Assert it has been removed from the 'UNREAD' scope since it has became read.")
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and select '${seedConversation3[0].subject}' conversation.")
        inboxPage.filterInbox("Inbox")
        inboxPage.selectConversation(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Delete the '${seedConversation2[0].subject}' conversation.")
        inboxPage.clickDelete()
        inboxPage.confirmDelete()

        Log.d(ASSERTION_TAG, "Assert that it has been removed from the 'INBOX' scope.")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(STEP_TAG, "Click on the '${seedConversation3[0].subject}' conversation.")
        inboxPage.openConversation(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Delete the '${seedConversation3[0]}' conversation.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.pressAlertButton("Delete")

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed.")
        inboxPage.assertInboxEmpty()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxFilterCoursesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 2)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val course2 = data.coursesList[1]
        val student1 = data.studentsList[0]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for '${student1.name}' student to the group: '${group.name}'.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the dashboard displays the course: '${course.name}'.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that Inbox is empty.")
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API from Teacher to Student for '${course.name}' course only.")
        val seedConversation = ConversationsApi.createConversationForCourse(
            token = teacher.token,
            recipients = listOf(student1.id.toString(), teacher.id.toString()),
            courseId = course.id
        )

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API from Student to Teacher for '${course.name}' course only.")
        val seedConversation2 = ConversationsApi.createConversationForCourse(
            token = student1.token,
            recipients = listOf(student1.id.toString(), teacher.id.toString()),
            courseId = course.id
        )

        Log.d(STEP_TAG,"Refresh the page.")
        inboxPage.refreshInbox()

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)

        Log.d(STEP_TAG, "Filter to 'Sent' messages.")
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view," +
            "and the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to '${course.name}' course messages and filter to 'Inbox' messages.")
        inboxPage.selectInboxFilter(course.name)
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)

        Log.d(STEP_TAG, "Filter to 'Sent' messages.")
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view, " +
                "and the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to '${course.name}' course messages.")
        inboxPage.selectInboxFilter(course2.name)

        Log.d(ASSERTION_TAG, "Assert that there is no message in the '${course.name}' course's 'Inbox' filter view, because the seeded conversations does not belong to this course.")
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG, "Filter to 'Sent' messages among the '${course.name}' course's messages.")
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that there is no message in the '${course.name}' course's 'Sent' filter view, because the seeded conversations does not belong to this course.")
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG, "Clear course filter (so get back to 'All Courses' view.")
        inboxPage.clearCourseFilter()

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view, " +
                "and the '${seedConversation2[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to 'Inbox' messages.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view, " +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view. ")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)
    }

    private fun addNewMessage(course: CourseApiModel, userRecipientList: MutableList<CanvasUserApiModel>) {
        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(course.name)

        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Students")
        for(recipient in userRecipientList) {
            recipientPickerPage.pressLabel(recipient.shortName)
        }

        recipientPickerPage.pressDone()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxMessageReplyWithVideoAttachmentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        Log.d(PREPARATION_TAG, "Copy mp4 file to Downloads folder for attachment.")
        val videoFileName = "test_video.mp4"
        setupFileOnDevice(videoFileName)
        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()

        val conversationSubject = "Need Help with Assignment"
        val conversationBody = "Can you please send me a demo video?"
        Log.d(PREPARATION_TAG, "Create a conversation from '${student.name}' to '${teacher.name}'.")
        val seededConversation = ConversationsApi.createConversationForCourse(token = student.token, courseId = course.id, recipients = listOf(teacher.id.toString()), subject = conversationSubject, body = conversationBody)[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Open the conversation.")
        inboxPage.openConversation(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' and '${conversationBody}' are displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Click Reply button to respond to the conversation.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply")

        val replyMessage = "Sure! Here is the demo video."
        Log.d(STEP_TAG, "Type reply message: '$replyMessage'")
        inboxComposePage.typeBody(replyMessage)

        Log.d(ASSERTION_TAG, "Assert that send button is enabled after typing message.")
        inboxComposePage.assertIfSendButtonState(true)

        Log.d(STEP_TAG, "Click attachment button to open file picker dialog.")
        inboxComposePage.clickAttachmentButton()

        Log.d(PREPARATION_TAG, "Simulate file picker intent (again).")
        Intents.init()
        try {
            stubFilePickerIntent(videoFileName)
            fileChooserPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        Log.d(STEP_TAG, "Click OKAY button to confirm file selection.")
        fileChooserPage.clickOkay()

        Log.d(ASSERTION_TAG, "Assert that the video file is displayed as attached in the screen.")
        inboxComposePage.assertAttachmentDisplayed(videoFileName)

        Log.d(STEP_TAG, "Send the reply message with attachment.")
        sleep(2000) //Wait for attachment to finish uploading
        inboxComposePage.pressSendButton()

        Log.d(ASSERTION_TAG, "Assert that the reply message, attachment, and original message are displayed in the conversation.")
        inboxDetailsPage.assertMessageDisplayed(replyMessage)
        inboxDetailsPage.assertAttachmentDisplayed(videoFileName)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Click on the attachment to verify it can be opened.")
        inboxDetailsPage.clickAttachment(videoFileName)

        Log.d(ASSERTION_TAG, "Wait for video to load and assert that the media play button is visible.")
        inboxDetailsPage.assertPlayButtonDisplayed()

        Log.d(STEP_TAG, "Click the play button to start the video and on the screen to show media controls.")
        inboxDetailsPage.clickPlayButton()
        inboxDetailsPage.clickScreenCenterToShowControls(device)

        Log.d(ASSERTION_TAG, "Assert that the play/pause button is visible in the media controls.")
        inboxDetailsPage.assertPlayPauseButtonDisplayed()

        Log.d(STEP_TAG, "Click play/pause button to pause the video.")
        inboxDetailsPage.clickPlayPauseButton()

        Log.d(STEP_TAG, "Get the current video position.")
        val firstPositionText = getVideoPosition(R.id.exo_position)

        Log.d(STEP_TAG, "Click play/pause button to resume video playback, wait for video to play for 2 seconds then click play/pause button to pause again.")
        inboxDetailsPage.clickPlayPauseButton()
        sleep(2000)
        inboxDetailsPage.clickPlayPauseButton()

        Log.d(STEP_TAG, "Get the video position again.")
        val secondPositionText = getVideoPosition(R.id.exo_position)

        Log.d(ASSERTION_TAG, "Assert that the video position has changed, confirming video is playing.")
        assert(firstPositionText != secondPositionText) {
            "Video position did not change. First: $firstPositionText, Second: $secondPositionText"
        }

        Log.d(STEP_TAG, "Navigate back to the conversation details.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' is displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)

        Log.d(STEP_TAG, "Navigate back to inbox.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the conversation is still displayed in inbox.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxMessageForwardWithPdfAttachmentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(PREPARATION_TAG, "Copy PDF file to device Downloads folder for attachment.")
        val pdfFileName = "samplepdf.pdf"
        val context = InstrumentationRegistry.getInstrumentation().context
        val inputStream = context.assets.open(pdfFileName)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadsDir, pdfFileName)

        Log.d(PREPARATION_TAG, "Writing file to: ${pdfFile.absolutePath}")
        pdfFile.outputStream().use { inputStream.copyTo(it) }
        inputStream.close()

        val conversationSubject = "Project Documentation"
        val conversationBody = "Please review the attached document and share it with the team."
        Log.d(PREPARATION_TAG, "Create a conversation from '${student1.name}' to '${teacher.name}'.")
        val seededConversation = ConversationsApi.createConversationForCourse(token = student1.token, courseId = course.id, recipients = listOf(teacher.id.toString()), subject = conversationSubject, body = conversationBody)[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Open the conversation.")
        inboxPage.openConversation(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' and '${conversationBody}' are displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Click Forward button to forward the conversation to ${student2.name}")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Forward")

        val forwardMessage = "please check this document."
        Log.d(STEP_TAG, "Type forward message: '$forwardMessage'")
        inboxComposePage.typeBody(forwardMessage)

        Log.d(STEP_TAG, "Select recipient for forwarded message.")
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Students")
        recipientPickerPage.pressLabel(student2.shortName)
        recipientPickerPage.pressDone()

        Log.d(ASSERTION_TAG, "Assert that send button is enabled after selecting recipient.")
        inboxComposePage.assertIfSendButtonState(true)

        Log.d(STEP_TAG, "Click attachment button to open file picker dialog.")
        inboxComposePage.clickAttachmentButton()

        Log.d(STEP_TAG, "Click on 'Device' option in file picker dialog.")
        fileChooserPage.chooseDevice()

        Log.d(STEP_TAG, "Select the PDF file from Android file picker using UIAutomator.")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val pdfFileObject = device.findObject(UiSelector().textContains(pdfFileName))
        if (pdfFileObject.exists()) {
            Log.d(STEP_TAG, "Found PDF file with exact name, clicking...")
            pdfFileObject.click()
        } else {
            Log.d(STEP_TAG, "PDF file not immediately visible, trying to navigate to Downloads...")
            val showRootsButton = device.findObject(UiSelector().descriptionContains("Show roots"))
            if (showRootsButton.exists()) {
                showRootsButton.click()
            }

            val downloadsItem = device.findObject(UiSelector().textContains("Downloads"))
            if (downloadsItem.exists()) {
                downloadsItem.click()
            }

            val pdfFileObject2 = device.findObject(UiSelector().textContains(pdfFileName))
            if (pdfFileObject2.exists()) {
                pdfFileObject2.click()
            }
        }

        Log.d(STEP_TAG, "Click OKAY button to confirm file selection.")
        fileChooserPage.clickOkay()

        Log.d(ASSERTION_TAG, "Assert that the PDF file is displayed as attached in the screen.")
        inboxComposePage.assertAttachmentDisplayed(pdfFileName)

        Log.d(STEP_TAG, "Send the forwarded message with attachment.")
        sleep(2000) //Wait for attachment to finish uploading
        inboxComposePage.pressSendButton()

        Log.d(ASSERTION_TAG, "Assert that the forward message is displayed in the conversation.")
        inboxDetailsPage.assertMessageDisplayed(forwardMessage)

        Log.d(ASSERTION_TAG, "Assert that the PDF attachment is displayed in the message.")
        inboxDetailsPage.assertAttachmentDisplayed(pdfFileName)

        Log.d(ASSERTION_TAG, "Assert that the original message is still displayed.")
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Click on the PDF attachment to verify it can be opened.")
        inboxDetailsPage.clickAttachment(pdfFileName)

        Log.d(ASSERTION_TAG, "Assert that the PDF document view is displayed.")
        inboxDetailsPage.assertPdfDocumentViewDisplayed()

        Log.d(STEP_TAG, "Navigate back to conversation details and assert that the '${conversationSubject}' is displayed.")
        Espresso.pressBack()
        inboxDetailsPage.assertConversationSubject(conversationSubject)

        Log.d(STEP_TAG, "Navigate back to Inbox conversation list page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the conversation is still displayed in inbox.")
        inboxPage.assertConversationDisplayed(conversationSubject)

        Log.d(STEP_TAG, "Log out with '${teacher.name}' teacher.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${student2.name}', login id: '${student2.loginId}'.")
        tokenLogin(student2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that the forwarded conversation is displayed in ${student2.name}'s inbox.")
        inboxPage.assertConversationDisplayed(conversationSubject)

        Log.d(STEP_TAG, "Open the forwarded conversation.")
        inboxPage.openConversation(conversationSubject)

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' and '${conversationBody}' are displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(ASSERTION_TAG, "Assert that the forwarded message from ${teacher.name} is displayed.")
        inboxDetailsPage.assertMessageDisplayed(forwardMessage)

        Log.d(ASSERTION_TAG, "Assert that the PDF attachment is displayed to ${student2.name}.")
        inboxDetailsPage.assertAttachmentDisplayed(pdfFileName)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxMessageForwardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 2, courses = 1)
        val teacher = data.teachersList[0]
        val teacher2 = data.teachersList[1]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]

        val conversationSubject = "Important Announcement"
        val conversationBody = "Please review the course syllabus and share with your classmates."
        Log.d(PREPARATION_TAG, "Create a conversation from '${student1.name}' to '${teacher.name}'.")
        val seededConversation = ConversationsApi.createConversationForCourse(token = student1.token, courseId = course.id, recipients = listOf(teacher.id.toString()), subject = conversationSubject, body = conversationBody)[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Open the conversation.")
        inboxPage.openConversation(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' and '${conversationBody}' are displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Click Forward button to forward the conversation to ${teacher2.name}.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Forward")

        val forwardMessage = "Hey, check this out."
        Log.d(STEP_TAG, "Type forward message: '$forwardMessage'")
        inboxComposePage.typeBody(forwardMessage)

        Log.d(STEP_TAG, "Select recipient for forwarded message.")
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(teacher2.shortName)
        recipientPickerPage.pressDone()

        Log.d(ASSERTION_TAG, "Assert that send button is enabled after selecting recipient.")
        inboxComposePage.assertIfSendButtonState(true)

        Log.d(STEP_TAG, "Send the forwarded message.")
        inboxComposePage.pressSendButton()

        Log.d(ASSERTION_TAG, "Assert that the forward message is displayed in the conversation.")
        inboxDetailsPage.assertMessageDisplayed(forwardMessage)

        Log.d(ASSERTION_TAG, "Assert that the original message is still displayed.")
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(STEP_TAG, "Navigate back to Inbox conversation list page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the conversation is still displayed in inbox.")
        inboxPage.assertConversationDisplayed(conversationSubject)

        Log.d(STEP_TAG, "Log out with '${teacher.name}' teacher.")
        leftSideNavigationDrawerPage.logout()

        Log.d(STEP_TAG, "Login with user: '${teacher2.name}', login id: '${teacher2.loginId}'.")
        tokenLogin(teacher2)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open Inbox Page.")
        dashboardPage.openInbox()

        Log.d(ASSERTION_TAG, "Assert that the forwarded conversation is displayed in ${teacher2.name}'s inbox.")
        inboxPage.assertConversationDisplayed(conversationSubject)

        Log.d(STEP_TAG, "Open the forwarded conversation.")
        inboxPage.openConversation(conversationSubject)

        Log.d(ASSERTION_TAG, "Assert that the '${conversationSubject}' and '${conversationBody}' are displayed.")
        inboxDetailsPage.assertConversationSubject(conversationSubject)
        inboxDetailsPage.assertMessageDisplayed(conversationBody)

        Log.d(ASSERTION_TAG, "Assert that the forwarded message from ${teacher.name} is displayed.")
        inboxDetailsPage.assertMessageDisplayed(forwardMessage)

        Log.d(ASSERTION_TAG, "Assert that the original message is also displayed.")
        inboxDetailsPage.assertMessageDisplayed(conversationBody)
    }

}