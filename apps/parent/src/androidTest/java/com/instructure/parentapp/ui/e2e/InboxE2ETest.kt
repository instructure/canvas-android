/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.ui.e2e

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxE2ETest: ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxSelectedButtonActionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(PREPARATION_TAG,"Seed an email from ${teacher.name}' to '${parent.name}'")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(parent.id.toString()))[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation. Assert that is has not been starred already.")
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.assertStarred(false)

        Log.d(STEP_TAG,"Toggle Starred to mark '${seededConversation.subject}' conversation as favourite. Assert that it has became starred.")
        inboxDetailsPage.pressStarButton(true)
        inboxDetailsPage.assertStarred(true)

        Log.d(STEP_TAG,"Navigate back to Inbox Page and  assert that the conversation itself is starred as well.")
        Espresso.pressBack() // To main inbox page
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation. Mark as Unread by clicking on the 'More Options' menu, 'Mark as Unread' menu point.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Mark as Unread")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Select 'Archived' conversation filter.")
        inboxPage.filterInbox("Archived")

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation. Assert that the selected number of conversations on the toolbar is 1." +
                "Unarchive it, and assert that it is not displayed in the 'ARCHIVED' scope any more.")
        inboxPage.selectConversation(seededConversation.subject)
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Click on the 'Mark as Unread' button and assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.clickMarkAsUnread()
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Select 'Unread' conversation filter.")
        inboxPage.filterInbox("Unread")

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation is displayed on the 'Inbox' tab.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation has been marked as unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.")
        inboxPage.openConversation(seededConversation.subject)
        inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        Espresso.pressBack()
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Select 'Archived' conversation filter.")
        inboxPage.filterInbox("Archived")
        refresh()

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Assert that '${seededConversation.subject}' conversation does not have the unread mark because an archived conversation cannot be unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation.")
        inboxPage.selectConversation(seededConversation.subject)

        Log.d(STEP_TAG, "Click on the 'Unarchive' button and assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.clickUnArchive()
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        sleep(2000)

        Log.d(STEP_TAG,"Navigate to 'INBOX' scope and assert that '${seededConversation.subject}' conversation is displayed.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation '${seededConversation.subject}' and unstar it." +
                "Assert that the selected number of conversations on the toolbar is 1 and the conversation is not starred.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.clickUnstar()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationNotStarred(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Select the conversation '${seededConversation.subject}' and archive it. Assert that it has not displayed in the 'INBOX' scope.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickArchive()

        sleep(2000)

        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and assert that the conversation is displayed there.")
        inboxPage.filterInbox("Archived")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope and assert that the conversation is displayed there, because a conversation cannot be archived and unread at the same time.")
        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope and assert that the conversation is NOT displayed there.")
        inboxPage.filterInbox("Starred")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Navigate to 'INBOX' scope and assert that '${seededConversation.subject}' conversation is NOT displayed because it is archived yet.")
        inboxPage.filterInbox("Inbox")
        sleep(2000)
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
        refresh()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and assert that the conversation is displayed there because it is not archived yet.")
        inboxPage.filterInbox("Inbox")
        inboxPage.assertConversationDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxMessageComposeReplyAndOptionMenuActionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 2, courses = 1)
        val parent = data.parentsList[0]
        val parent2 = data.parentsList[1]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(PREPARATION_TAG,"Seed an email from the teacher to '${parent.name}'.")
        val seededConversation = ConversationsApi.createConversationForCourse(teacher.token, course.id, listOf(parent.id.toString(), parent2.id.toString()))[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject = "Hey There"
        val newMessage = "Just checking in"
        Log.d(STEP_TAG,"Create a new message with subject: '$newMessageSubject', and message: '$newMessage'")
        inboxCoursePickerPage.selectCourseWithUser(course.name, student1.shortName)
        inboxComposeMessagePage.typeSubject(newMessageSubject)
        inboxComposeMessagePage.typeBody(newMessage)

        Log.d(STEP_TAG,"Click on 'Send' button.")
        inboxComposeMessagePage.pressSendButton()

        composeTestRule.waitForIdle()
        sleep(1000) // Allow time for messages to propagate

        Log.d(STEP_TAG,"Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject2 = "Test Message"
        val newMessage2 = "Testing body for second message"
        Log.d(STEP_TAG,"Create a new message with subject: '$newMessageSubject2', and message: '$newMessage2'")
        inboxCoursePickerPage.selectCourseWithUser(course.name, student2.shortName)
        inboxComposeMessagePage.typeSubject(newMessageSubject2)
        inboxComposeMessagePage.typeBody(newMessage2)

        Log.d(STEP_TAG,"Click on 'Send' button.")
        inboxComposeMessagePage.pressSendButton()

        sleep(2000) // Allow time for messages to propagate

        Log.d(STEP_TAG, "Navigate to 'SENT' scope")
        inboxPage.filterInbox("Sent")
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed(newMessageSubject2)

        Log.d(STEP_TAG, "Log out from ${parent.name} account.")
        Espresso.pressBack()
        dashboardPage.openNavigationDrawer()
        composeTestRule.waitForIdle()
        leftSideNavigationDrawerPage.clickLogout()
        composeTestRule.waitForIdle()
        leftSideNavigationDrawerPage.clickOk()
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Log in to ${parent2.name} account.")
        tokenLogin(parent2)
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Open Inbox")
        dashboardPage.openNavigationDrawer()
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG,"Select '${seededConversation.subject}' conversation.")
        inboxPage.openConversation(seededConversation.subject)
        val newReplyMessage = "This is a quite new reply message."
        Log.d(STEP_TAG,"Reply to ${seededConversation.subject} conversation. Assert that the reply is displayed.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply All")
        inboxComposeMessagePage.typeBody(newReplyMessage)
        inboxComposeMessagePage.pressSendButton()

        Log.d(STEP_TAG,"Delete '$newReplyMessage' reply and assert is has been deleted.")
        inboxDetailsPage.pressOverflowMenuItemForMessage(newReplyMessage, "Delete")
        inboxDetailsPage.pressAlertButton("Delete")
        inboxDetailsPage.assertMessageNotDisplayed(newReplyMessage)

        Log.d(STEP_TAG,"Delete the whole '$newMessageSubject' subject and assert that it has been removed from the conversation list on the Inbox Page.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.pressAlertButton("Delete")
        inboxPage.assertConversationNotDisplayed(newMessageSubject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxSwipeGesturesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]

        Log.d(STEP_TAG,"Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openNavigationDrawer()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(PREPARATION_TAG,"Seed an email from '${teacher.name}' to '${parent.name}'.")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(parent.id.toString()))[0]

        Log.d(STEP_TAG,"Refresh the page. Assert that there is a conversation and it is the previously seeded one.")
        refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right to make it read. Assert that the conversation became read.")
        inboxPage.selectConversation(seededConversation.subject)
        inboxPage.swipeConversationRight(seededConversation.subject)
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right again to make it unread. Assert that the conversation became unread.")
        inboxPage.swipeConversationRight(seededConversation.subject)
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'INBOX' scope because it has became archived.")
        inboxPage.swipeConversationLeft(seededConversation.subject)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope. Assert that the '${seededConversation.subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.swipeConversationLeft(seededConversation.subject)
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

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope. Assert that the conversation is displayed in the 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left and assert it is removed from the 'STARRED' scope because it has became unstarred.")
        inboxPage.swipeConversationLeft(seededConversation.subject)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope. Assert that the conversation is displayed in the 'Unread' scope.")
        inboxPage.filterInbox("Unread")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' conversation right and assert that it has disappeared from the 'UNREAD' scope.")
        inboxPage.swipeConversationRight(seededConversation.subject)
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)
    }
}