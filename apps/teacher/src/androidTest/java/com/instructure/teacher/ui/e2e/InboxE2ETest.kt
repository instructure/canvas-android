package com.instructure.teacher.ui.e2e

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
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course = course)
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        val seedConversation = ConversationsApi.createConversation(
            token = student1.token,
            recipients = listOf(teacher.id.toString())
        )

        inboxPage.refresh()
        inboxPage.assertHasConversation()
        inboxPage.clickConversation(seedConversation[0])

        inboxMessagePage.clickReply()
        addMessagePage.addReply("Hello there")
        inboxMessagePage.assertHasReply()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testInboxNewMessageE2E() {
        val data = seedData(students = 2, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student1 = data.studentsList[0]
        val student2 = data.studentsList[1]

        val groupCategory = GroupsApi.createCourseGroupCategory(course.id, teacher.token)
        val group = GroupsApi.createGroup(groupCategory.id, teacher.token)
        GroupsApi.createGroupMembership(group.id, student1.id, teacher.token)

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course = course)
        dashboardPage.openInbox()
        inboxPage.assertInboxEmpty()

        inboxPage.clickAddMessageFAB()
        addMessagePage.clickCourseSpinner()
        addMessagePage.selectCourseFromSpinner(courseName = course.name)
        addMessagePage.clickAddContacts()
        chooseRecipientsPage.clickStudentCategory()
        chooseRecipientsPage.clickStudent(student = student1)
        chooseRecipientsPage.clickStudent(student = student2)
        chooseRecipientsPage.clickDone()
        addMessagePage.addSubject(subject = "Hello there")
        addMessagePage.addMessage("General Kenobi")
        addMessagePage.clickSendButton()
        inboxPage.filterInbox("Sent")
        inboxPage.assertHasConversation()
        inboxPage.clickConversation(conversationSubject = "Hello there")
        Espresso.pressBack()
        inboxPage.assertHasConversation()
    }
}