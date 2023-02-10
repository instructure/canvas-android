package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
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

        Log.d(STEP_TAG,"Navigate back to Inbox Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the message is not unread anymore.")
        inboxPage.assertThereIsAnUnreadMessage(false)

        Log.d(STEP_TAG,"Add a new conversation message manually via UI. Click on 'New Message' ('+') button.")
        inboxPage.clickAddMessageFAB()

        Log.d(STEP_TAG,"Select ${course.name} from course spinner.Click on the '+' icon next to the recipients input field. Select the two students: ${student1.name} and ${student2.name}. Click on 'Done'.")
        addNewMessage(course,data.studentsList)

        val subject = "Hello there"
        Log.d(STEP_TAG,"Fill in the 'Subject' field with the value: $subject.")
        addMessagePage.addSubject(subject)

        Log.d(STEP_TAG,"Add some message text and click on 'Send' (aka. 'Arrow') button.")
        addMessagePage.addMessage("General Kenobi")
        addMessagePage.clickSendButton()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Sent' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Sent")

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

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'All' category from the spinner on Inbox Page.")
        inboxPage.filterInbox("Inbox")

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed.")
        inboxPage.refresh()
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Star the conversation and navigate back to Inbox Page.")
        inboxMessagePage.clickOnStarConversation()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the '${seedConversation[0]}' conversation has been starred.")
        inboxPage.assertConversationStarred(student1.name + ", " + student2.name)

        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: '$replyMessage'.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Archive the '${seedConversation[0]}' conversation and assert that it has disappeared from the list," +
                "because archived conversations does not displayed within the 'All' section.")
        inboxMessagePage.openOptionMenuFor("Archive")
        dashboardPage.assertPageObjects()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Archived' category from the spinner on Inbox Page." +
                "Assert that the previously archived conversation is displayed.")
        inboxPage.filterInbox("Archived")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Starred' category from the spinner on Inbox Page." +
                "Assert that the '${seedConversation[0]}' conversation is displayed because it's still starred.")
        inboxPage.filterInbox("Starred")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Remove star from the conversation and navigate back to Inbox Page.")
        inboxMessagePage.clickOnStarConversation()
        Espresso.pressBack()

        Log.d(STEP_TAG, "Assert that the '${seedConversation[0]}' conversation is disappeared because it's not starred yet.")
        dashboardPage.assertPageObjects()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Filter the Inbox by selecting 'Archived' category from the spinner on Inbox Page. Assert that the '${seedConversation[0]}' conversation is displayed.")
        inboxPage.filterInbox("Archived")
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation.")
        inboxPage.clickConversation(seedConversation[0])

        Log.d(STEP_TAG, "Delete the '${seedConversation[0]}' conversation and assert that it has disappeared from the list.")
        inboxMessagePage.deleteConversation()
        refresh() //This is needed because Archive list (and the other 'specific' ones) does not refreshing automatically like the 'All'.
        inboxPage.assertPageObjects()
        inboxPage.assertInboxEmpty()

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