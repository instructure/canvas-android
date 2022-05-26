package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.GroupsApi
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
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student.name} to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(PREPARATION_TAG, "Seed an Inbox conversation via API..")
        val seedConversation = ConversationsApi.createConversation(
            token = student.token,
            recipients = listOf(teacher.id.toString())
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously seeded Inbox conversation is displayed.")
        inboxPage.refresh()
        inboxPage.assertHasConversation()

        Log.d(STEP_TAG,"Click on the conversation. Write a reply with the message: 'Hello there'.")
        inboxPage.clickConversation(seedConversation[0])
        inboxMessagePage.clickReply()
        addMessagePage.addReply("Hello there")

        Log.d(STEP_TAG,"Assert that the reply has successfully sent and it's displayed.")
        inboxMessagePage.assertHasReply()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxNewMessageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        Log.d(PREPARATION_TAG, "Create group membership for ${student1.name} to the group: ${group.name}.")
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open Inbox. Assert that Inbox is empty.")
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        Log.d(STEP_TAG,"Click on 'New Message' ('+') button.")
        inboxPage.clickAddMessageFAB()

        Log.d(STEP_TAG,"Select ${course.name} from course spinner.")
        addMessagePage.clickCourseSpinner()
        addMessagePage.selectCourseFromSpinner(course.name)

        Log.d(STEP_TAG,"Click on the '+' icon next to the recipients input field. Select the two students: ${student1.name} and ${student2.name}. Click on 'Done'.")
        addMessagePage.clickAddContacts()
        chooseRecipientsPage.clickStudentCategory()
        chooseRecipientsPage.clickStudent(student1)
        chooseRecipientsPage.clickStudent(student2)
        chooseRecipientsPage.clickDone()


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

        Log.d(STEP_TAG,"Click on $subject conversation and navigate back after it has opened. Assert that the conversation is still displayed on the Inbox Page after opening it.")
        inboxPage.clickConversation(subject)
        Espresso.pressBack()
        inboxPage.assertHasConversation()
    }
}