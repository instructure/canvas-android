package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class InboxE2ETest : TeacherTest() {
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
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} student to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        val seedConversation = ConversationsApi.createConversation(
            token = student1.token,
            recipients = listOf(teacher.id.toString())
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed. Assert that the message is unread yet.")
        inboxPage.refresh()
        inboxPage.assertHasConversation()
        inboxPage.assertThereIsAnUnreadMessage(true)

        val replyMessage = "Hello there"
        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.clickConversation(seedConversation[0])
        inboxMessagePage.clickReply()
        addMessagePage.addReply(replyMessage)

        Log.d(STEP_TAG,"Assert that the reply has successfully sent and it's displayed.")
        inboxMessagePage.assertHasReply()

        Log.d(STEP_TAG,"Navigate back to Inbox Page. Assert that the message is not unread anymore.")
        Espresso.pressBack()
        inboxPage.assertThereIsAnUnreadMessage(false)

        Log.d(STEP_TAG,"Add a new conversation message manually via UI. Click on 'New Message' ('+') button.")
        inboxPage.clickAddMessageFAB()

        Log.d(STEP_TAG,"Select ${course.name} from course spinner.Click on the '+' icon next to the recipients input field. Select the two students: ${student1.name} and ${student2.name}. Click on 'Done'.")
        addNewMessage(course,data.studentsList)

        val subject = "Hello there"
        Log.d(STEP_TAG,"Fill in the 'Subject' field with the value: $subject. Add some message text and click on 'Send' (aka. 'Arrow') button.")
        addMessagePage.composeMessageWithSubject(subject, "General Kenobi")
        addMessagePage.clickSendButton()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Sent' category from the spinner on Inbox Page.")
        inboxPage.filterMessageScope("Sent")

        Log.d(STEP_TAG,"Assert that the previously sent conversation is displayed.")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on $subject conversation.")
        inboxPage.clickConversation(subject)

        val replyMessageTwo = "Test Reply 2"
        Log.d(STEP_TAG,"Click on 'Reply' button. Write a reply with the message: '$replyMessageTwo'.")
        inboxMessagePage.clickReply()
        addMessagePage.addReply(replyMessageTwo)

        Log.d(STEP_TAG,"Assert that the reply has successfully sent and it's displayed.")
        inboxMessagePage.assertHasReply()

        Log.d(STEP_TAG,"Navigate back after it has opened. Assert that the conversation is still displayed on the Inbox Page after opening it.")
        Espresso.pressBack()
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Inbox' category from the spinner on Inbox Page.")
        inboxPage.filterMessageScope("Inbox")

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed.")
        inboxPage.refresh()
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Star the conversation and navigate back to Inbox Page.")
        inboxMessagePage.clickOnStarConversation()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the '${seedConversation[0].subject}' conversation has been starred.")
        inboxPage.assertConversationStarred(seedConversation[0].subject)

        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Archive the '${seedConversation[0]}' conversation and assert that it has disappeared from the list," +
                "because archived conversations does not displayed within the 'All' section.")
        inboxMessagePage.openOptionMenuFor("Archive")
        dashboardPage.assertPageObjects()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Archived' category from the spinner on Inbox Page." +
                "Assert that the previously archived conversation is displayed.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Starred' category from the spinner on Inbox Page." +
                "Assert that the '${seedConversation[0]}' conversation is displayed because it's still starred.")
        inboxPage.filterMessageScope("Starred")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Remove star from the conversation and navigate back to Inbox Page.")
        inboxMessagePage.clickOnStarConversation()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the '${seedConversation[0]}' conversation is disappeared because it's not starred yet.")
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
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} student to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        val seedConversation = ConversationsApi.createConversation(
            token = student1.token,
            recipients = listOf(teacher.id.toString())
        )

        Log.d(STEP_TAG, "Refresh the page. Assert that the conversation displayed as unread.")
        inboxPage.refresh()
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

        Log.d(STEP_TAG,"Refresh the page. Filter the Inbox by selecting 'Inbox' category from the spinner on Inbox Page. Assert that the '${seedConversation[0]}' conversation is displayed. Assert that the conversation is unread yet.")
        inboxPage.refresh()
        inboxPage.filterMessageScope("Inbox")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation. Unarchive it, and assert that it is not displayed in the 'ARCHIVED' scope any more.")
        inboxPage.selectConversation(seedConversation2[0])
        inboxPage.clickArchive()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select 'ARCHIVED' scope and assert that '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation and unarchive it." +
                "Assert that the selected number of conversation on the toolbar is 1 and '${seedConversation2[0].subject}' conversation is not displayed in the 'ARCHIVED' scope.")
        inboxPage.selectConversation(seedConversation2[0])
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.clickUnArchive()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG,"Navigate to 'INBOX' scope and assert that ${seedConversation2[0].subject} conversation is displayed.")
        inboxPage.filterMessageScope("Inbox")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations (${seedConversation[0].subject} and ${seedConversation2[0].subject} and star them." +
                "Assert that both of the has been starred and the selected number of conversations on the toolbar shows 2")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.assertSelectedConversationNumber("2")
        inboxPage.clickStar()
        inboxPage.assertConversationStarred(seedConversation2[0].subject)
        inboxPage.assertConversationStarred(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Mark them as read (since if at least there is one unread selected, we are showing the 'Mark as Read' icon). Assert that both of them are read.")
        inboxPage.clickMarkAsRead()
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.GONE)
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Mark them as unread. Assert that both of them will became unread.")
        inboxPage.clickMarkAsUnread()
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.VISIBLE)
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Archive both of them. Assert that non of them are displayed in the 'INBOX' scope.")
        inboxPage.clickArchive()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and assert that both of the conversations are displayed there.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope and assert that none of the conversations are displayed there, because a conversation cannot be archived and unread at the same time.")
        inboxPage.filterMessageScope("Unread")
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope and assert that both of the conversations are displayed there.")
        inboxPage.filterMessageScope("Starred")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations. Unstar them, and assert that none of them are displayed in the 'STARRED' scope.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickUnstar()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope and assert that both of the conversations are displayed there.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations. Unarchive them, and assert that none of them are displayed in the 'ARCHIVED' scope.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickUnArchive()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and assert that both of the conversations are displayed there.")
        inboxPage.filterMessageScope("Inbox")
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
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} student to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API.")
        val seedConversation = ConversationsApi.createConversation(
            token = student1.token,
            recipients = listOf(teacher.id.toString())
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed. Assert that the message is unread yet.")
        inboxPage.refresh()
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

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation and swipe it right to make it read. Assert that the conversation became read.")
        inboxPage.refresh()
        inboxPage.selectConversation(seedConversation2[0].subject)
        inboxPage.swipeConversationRight(seedConversation2[0])
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seedConversation2[0].subject}' conversation and swipe it right again to make it unread. Assert that the conversation became unread.")
        inboxPage.swipeConversationRight(seedConversation2[0])
        inboxPage.assertUnreadMarkerVisibility(seedConversation2[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left and assert it is removed from the 'INBOX' scope because it has became archived.")
        inboxPage.swipeConversationLeft(seedConversation2[0])
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope. Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left and assert it is removed from the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.swipeConversationLeft(seedConversation2[0])
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope. Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'INBOX' scope.")
        inboxPage.filterMessageScope("Inbox")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Select both of the conversations. Star them and mark the unread.")
        inboxPage.selectConversations(listOf(seedConversation2[0].subject, seedConversation3[0].subject))
        inboxPage.clickStar()
        inboxPage.clickMarkAsRead()

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope. Assert that both of the conversation are displayed in the 'STARRED' scope.")
        inboxPage.filterMessageScope("Starred")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation2[0].subject}' left and assert it is removed from the 'STARRED' scope because it has became unstarred.")
        inboxPage.swipeConversationLeft(seedConversation2[0])
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Assert that '${seedConversation3[0].subject}' conversation is read.")
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Swipe '${seedConversation3[0].subject}' conversation right and assert that it has became unread.")
        inboxPage.swipeConversationRight(seedConversation3[0].subject)
        inboxPage.assertUnreadMarkerVisibility(seedConversation3[0].subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope. Assert that only the '${seedConversation3[0].subject}' conversation is displayed in the 'UNREAD' scope.")
        inboxPage.filterMessageScope("Unread")
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Swipe '${seedConversation3[0].subject}' conversation left and assert it has been removed from the 'UNREAD' scope since it has became read.")
        inboxPage.swipeConversationLeft(seedConversation3[0])
        inboxPage.assertConversationNotDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope. Assert that the '${seedConversation2[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterMessageScope("Archived")
        inboxPage.assertConversationDisplayed(seedConversation3[0].subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope and select '${seedConversation3[0].subject}' conversation.")
        inboxPage.filterMessageScope("Inbox")
        inboxPage.selectConversation(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Delete the '${seedConversation2[0].subject}' conversation and assert that it has been removed from the 'INBOX' scope.")
        inboxPage.clickDelete()
        inboxPage.confirmDelete()
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope. Assert that the '${seedConversation3[0].subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.filterMessageScope("Archived")

        Log.d(STEP_TAG,"Click on the '${seedConversation3[0].subject}' conversation.")
        inboxPage.clickConversation(seedConversation3[0])

        Log.d(STEP_TAG, "Delete the '${seedConversation3[0]}' conversation and assert that it has disappeared from the list.")
        inboxMessagePage.deleteConversation()

        Log.d(STEP_TAG, "Assert that the empty view is displayed.")
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
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} student to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
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

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed. Assert that the message is unread yet.")
        inboxPage.refresh()

        Log.d(STEP_TAG, "Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)

        Log.d(STEP_TAG, "Filter to 'Sent' messages. Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view.")
        inboxPage.filterMessageScope("Sent")
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to '${course.name}' course messages and filter to 'Inbox' messages.")
        inboxPage.filterCourseScope(course.name)
        inboxPage.filterMessageScope("Inbox")

        Log.d(STEP_TAG, "Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)

        Log.d(STEP_TAG, "Filter to 'Sent' messages. Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation2[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view.")
        inboxPage.filterMessageScope("Sent")
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to '${course.name}' course messages. Assert that there is no message in the '${course.name}' course's 'Inbox' filter view," +
                "because the seeded conversations does not belong to this course.")
        inboxPage.filterCourseScope(course2.name)
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG, "Filter to 'Sent' messages among the '${course.name}' course's messages. Assert that there is no message in the '${course.name}' course's 'Sent' filter view," +
                "because the seeded conversations does not belong to this course.")
        inboxPage.filterMessageScope("Sent")
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG, "Clear course filter (so get back to 'All Courses' view." +
                "Assert that the '${seedConversation[0].subject}' conversation, which was sent by the Student to the Teacher is displayed in the 'Inbox' filter view, " +
                "and the '${seedConversation2[0].subject}' conversation, which was sent by the Teacher to the Student is not displayed in the 'Inbox' filter view.")
        inboxPage.clearCourseFilter()
        inboxPage.assertConversationDisplayed(seedConversation[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation2[0].subject)

        Log.d(STEP_TAG, "Filter to 'Inbox' messages. Assert that the '${seedConversation2[0].subject}' conversation, which was sent by the Teacher to the Student is displayed in the 'Inbox' filter view," +
                "and the '${seedConversation[0].subject}' conversation, which was sent by the Student to the Teacher is not displayed in the 'Inbox' filter view.")
        inboxPage.filterMessageScope("Inbox")
        inboxPage.assertConversationDisplayed(seedConversation2[0].subject)
        inboxPage.assertConversationNotDisplayed(seedConversation[0].subject)
    }

    private fun addNewMessage(course: CourseApiModel, userRecipientList: MutableList<CanvasUserApiModel>) {
        addMessagePage.clickCourseSpinner()
        addMessagePage.selectCourseFromSpinner(course.name)

        addMessagePage.clickAddContacts()
        chooseRecipientsPage.clickStudentCategory()
        for(recipient in userRecipientList) {
            chooseRecipientsPage.clickStudent(recipient)
        }

        chooseRecipientsPage.clickDone()
    }

}