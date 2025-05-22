package com.instructure.teacher.ui.e2e

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
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.retry
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

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
        retry(times = 10, delay = 3000, block = {
            refresh()
            inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        })

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation.")
        inboxPage.selectConversation(seedConversation2[0].subject)

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversation on the toolbar is 1 and '${seedConversation2[0].subject}' conversation is not displayed in the 'ARCHIVED' scope.")
        inboxPage.assertSelectedConversationNumber("1")

        Log.d(STEP_TAG, "Click on the 'Mark as Unread' button.")
        inboxPage.clickMarkAsUnread()

        Log.d(ASSERTION_TAG, "Assert that the empty view will be displayed and the '${seedConversation2[0].subject}' conversation is not because it should disappear from 'Archived' list.")
        inboxPage.assertInboxEmpty()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

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
        refresh()

        Log.d(ASSERTION_TAG, "Assert that '${seedConversation2[0].subject}' conversation is displayed by the 'Archived' filter.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

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
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

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

}