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
import com.instructure.canvas.espresso.ReleaseExclude
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CalendarEventApi
import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.model.UpdateCourse
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Date

@HiltAndroidTest
class InboxE2ETest: ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    @ReleaseExclude
    fun testInboxSelectedButtonActionsE2E() = run {

        step("Seeding data.") {}
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]

        step("Login with user: '${parent.name}', login id: '${parent.loginId}'.") {
            tokenLogin(parent)
            dashboardPage.waitForRender()
        }

        step("Open the Left Side Navigation Drawer menu.") {
            dashboardPage.openLeftSideMenu()
        }

        step("Open 'Inbox' menu.") {
            leftSideNavigationDrawerPage.clickInbox()
        }

        step("Seed an email from '${teacher.name}' to '${parent.name}'") {}
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(parent.id.toString()))[0]

        step("Refresh the page.") {
            refresh()
        }

        step("Assert that there is a conversation and it is the previously seeded one.") {
            inboxPage.assertHasConversation()
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Select '${seededConversation.subject}' conversation.") {
            inboxPage.openConversation(seededConversation.subject)
        }

        step("Assert that is has not been starred already.") {
            inboxDetailsPage.assertStarred(false)
        }

        step("Toggle Starred to mark '${seededConversation.subject}' conversation as favourite.") {
            inboxDetailsPage.pressStarButton(true)
        }

        step("Assert that it has became starred.") {
            inboxDetailsPage.assertStarred(true)
        }

        step("Navigate back to Inbox Page.") {
            Espresso.pressBack() // To main Inbox page
        }

        step("Assert that the conversation itself is starred as well.") {
            inboxPage.assertConversationStarred(seededConversation.subject)
        }

        step("Assert that '${seededConversation.subject}' conversation does not have the unread mark") {
            inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)
        }

        step("Select '${seededConversation.subject}' conversation. Mark as Unread by clicking on the 'More Options' menu, 'Mark as Unread' menu point.") {
            inboxPage.openConversation(seededConversation.subject)
            inboxDetailsPage.pressOverflowMenuItemForConversation("Mark as Unread")
        }

        step("Navigate back to Inbox Page.") {
            Espresso.pressBack()
        }

        step("Assert that '${seededConversation.subject}' conversation has been marked as unread.") {
            inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)
        }

        step("Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.") {
            inboxPage.openConversation(seededConversation.subject)
            inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        }

       step("Navigate back to Inbox Page.") {
           Espresso.pressBack()
        }

        step("Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Select 'Archived' conversation filter.") {
            inboxPage.filterInbox("Archived")
        }

        step("Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Select '${seededConversation.subject}' conversation.") {
            inboxPage.selectConversation(seededConversation.subject)
        }

        step("Assert that the selected number of conversations on the toolbar is 1.") {
            inboxPage.assertSelectedConversationNumber("1")
        }

        step("Click on the 'Mark as Unread' button.") {
            inboxPage.clickMarkAsUnread()
        }

        step("Assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.") {
            inboxPage.assertInboxEmpty()
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Select 'Unread' conversation filter.") {
            inboxPage.filterInbox("Unread")
        }

        step("Assert that '${seededConversation.subject}' conversation is displayed on the 'Inbox' tab.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Assert that '${seededConversation.subject}' conversation has been marked as unread.") {
            inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)
        }

        step("Select '${seededConversation.subject}' conversation. Archive it by clicking on the 'More Options' menu, 'Archive' menu point.") {
            inboxPage.openConversation(seededConversation.subject)
            inboxDetailsPage.pressOverflowMenuItemForConversation("Archive")
        }

        step("Navigate back to Inbox Page.") {
            Espresso.pressBack()
            composeTestRule.waitForIdle()
        }

        step("Assert that '${seededConversation.subject}' conversation has removed from 'Inbox' tab.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Select 'Archived' conversation filter.") {
            inboxPage.filterInbox("Archived")
            refresh()
        }

        step("Assert that '${seededConversation.subject}' conversation is displayed by the 'Archived' filter.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Assert that '${seededConversation.subject}' conversation does not have the unread mark because an archived conversation cannot be unread.") {
            inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)
        }

        step("Select '${seededConversation.subject}' conversation.") {
            inboxPage.selectConversation(seededConversation.subject)
        }

        step("Click on the 'Unarchive' button.") {
            inboxPage.clickUnArchive()
        }

        step("Assert that the empty view will be displayed and the '${seededConversation.subject}' conversation is not because it should disappear from 'Archived' list.") {
            inboxPage.assertInboxEmpty()
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Navigate to 'INBOX' scope.") {
            inboxPage.filterInbox("Inbox")
        }

        step("Assert that '${seededConversation.subject}' conversation is displayed.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Select the conversation '${seededConversation.subject}' and unstar it.") {
            inboxPage.selectConversations(listOf(seededConversation.subject))
            inboxPage.clickUnstar()
        }

        step("Assert that the selected number of conversations on the toolbar is 1 and the conversation is not starred.") {
            inboxPage.assertSelectedConversationNumber("1")
            flakySafely(timeoutMs = 2000, intervalMs = 200) {
                inboxPage.assertConversationNotStarred(seededConversation.subject)
            }
        }

        step("Select the conversation '${seededConversation.subject}' and archive it.") {
            inboxPage.selectConversations(listOf(seededConversation.subject))
            inboxPage.clickArchive()
        }

        step("Assert that it has not displayed in the 'INBOX' scope.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Navigate to 'ARCHIVED' scope.") {
            inboxPage.filterInbox("Archived")
        }

        step("Assert that the conversation is displayed there.") {
            flakySafely(timeoutMs = 2000, intervalMs = 200) {
                inboxPage.assertConversationDisplayed(seededConversation.subject)
            }
        }

        step("Navigate to 'UNREAD' scope.") {
            inboxPage.filterInbox("Unread")
        }

        step("Assert that the conversation is not displayed there, because a conversation cannot be archived and unread at the same time.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Navigate to 'STARRED' scope.") {
            inboxPage.filterInbox("Starred")
        }

        step("Assert that the conversation is NOT displayed there.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Navigate to 'INBOX' scope.") {
            inboxPage.filterInbox("Inbox")
        }

        step("Assert that '${seededConversation.subject}' conversation is NOT displayed because it is archived yet.") {
            inboxPage.assertConversationNotDisplayed(seededConversation.subject)
        }

        step("Navigate to 'ARCHIVED' scope, Select the conversation and Star it.") {
            inboxPage.filterInbox("Archived")
            inboxPage.selectConversations(listOf(seededConversation.subject))
            inboxPage.clickStar()
        }

        step("Assert that it has displayed in the 'STARRED' scope.") {
            inboxPage.assertConversationStarred(seededConversation.subject)
        }

        step("Select the conversation and Unarchive it.") {
            inboxPage.selectConversations(listOf(seededConversation.subject))
            inboxPage.clickUnArchive()
        }

        step("Assert that it has not displayed in the 'ARCHIVED' scope.") {
            flakySafely(timeoutMs = 2000, intervalMs = 200) {
                inboxPage.assertConversationNotDisplayed(seededConversation.subject)
            }
        }

        step("Navigate to 'STARRED' scope.") {
            inboxPage.filterInbox("Starred")
            refresh()
        }

        step("Assert that the conversation is displayed there.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        step("Navigate to 'INBOX' scope.") {
            inboxPage.filterInbox("Inbox")
        }

        step("Assert that the conversation is displayed there because it is not archived yet.") {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }
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

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(PREPARATION_TAG, "Seed an email from the teacher to '${parent.name}'.")
        val seededConversation = ConversationsApi.createConversationForCourse(teacher.token, course.id, listOf(parent.id.toString(), parent2.id.toString()))[0]

        Log.d(STEP_TAG, "Refresh the page.")
        refresh()

        Log.d(ASSERTION_TAG, "Assert that there is a conversation and it is the previously seeded one.")
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject = "Hey There"
        val newMessage = "Just checking in"
        Log.d(STEP_TAG, "Create a new message with subject: '$newMessageSubject', and message: '$newMessage'")
        inboxCoursePickerPage.selectCourseWithUser(course.name, student1.shortName)
        inboxComposeMessagePage.typeSubject(newMessageSubject)
        inboxComposeMessagePage.typeBody(newMessage)

        Log.d(STEP_TAG, "Click on 'Send' button.")
        inboxComposeMessagePage.pressSendButton()

        composeTestRule.waitForIdle()
        sleep(1000) // Allow time for messages to propagate

        Log.d(STEP_TAG, "Click on 'New Message' button.")
        inboxPage.pressNewMessageButton()

        val newMessageSubject2 = "Test Message"
        val newMessage2 = "Testing body for second message"
        Log.d(STEP_TAG, "Create a new message with subject: '$newMessageSubject2', and message: '$newMessage2'")
        inboxCoursePickerPage.selectCourseWithUser(course.name, student2.shortName)
        inboxComposeMessagePage.typeSubject(newMessageSubject2)
        inboxComposeMessagePage.typeBody(newMessage2)

        Log.d(STEP_TAG, "Click on 'Send' button.")
        inboxComposeMessagePage.pressSendButton()

        sleep(2000) // Allow time for messages to propagate

        Log.d(STEP_TAG, "Navigate to 'SENT' scope")
        inboxPage.filterInbox("Sent")

        Log.d(ASSERTION_TAG, "Assert that the new messages are displayed.")
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(newMessageSubject)
        inboxPage.assertConversationDisplayed(newMessageSubject2)

        Log.d(STEP_TAG, "Log out from '${parent.name}' account.")
        Espresso.pressBack()
        dashboardPage.openLeftSideMenu()
        composeTestRule.waitForIdle()
        leftSideNavigationDrawerPage.clickLogout()
        composeTestRule.waitForIdle()
        leftSideNavigationDrawerPage.clickOk()
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Log in to '${parent2.name}' account.")
        tokenLogin(parent2)
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG, "Open Inbox")
        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation.")
        inboxPage.openConversation(seededConversation.subject)

        val newReplyMessage = "This is a quite new reply message."
        Log.d(STEP_TAG, "Reply to ${seededConversation.subject} conversation.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Reply All")
        inboxComposeMessagePage.typeBody(newReplyMessage)
        inboxComposeMessagePage.pressSendButton()

        Log.d(STEP_TAG, "Delete '$newReplyMessage' reply.")
        inboxDetailsPage.pressOverflowMenuItemForMessage(newReplyMessage, "Delete")
        inboxDetailsPage.pressAlertButton("Delete")

        Log.d(ASSERTION_TAG, "Assert that it has been deleted.")
        inboxDetailsPage.assertMessageNotDisplayed(newReplyMessage)

        Log.d(STEP_TAG, "Delete the whole '$newMessageSubject' subject.")
        inboxDetailsPage.pressOverflowMenuItemForConversation("Delete")
        inboxDetailsPage.pressAlertButton("Delete")

        Log.d(ASSERTION_TAG, "Assert that it has been removed from the conversation list on the Inbox Page.")
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

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the Left Side Navigation Drawer menu.")
        dashboardPage.openLeftSideMenu()

        Log.d(STEP_TAG, "Open 'Inbox' menu.")
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(PREPARATION_TAG, "Seed an email from '${teacher.name}' to '${parent.name}'.")
        val seededConversation = ConversationsApi.createConversation(teacher.token, listOf(parent.id.toString()))[0]

        Log.d(STEP_TAG, "Refresh the page.")
        refresh()

        Log.d(ASSERTION_TAG, "Assert that there is a conversation and it is the previously seeded one.")
        inboxPage.assertHasConversation()
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right to make it read.")
        inboxPage.selectConversation(seededConversation.subject)
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation became read.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.GONE)

        Log.d(STEP_TAG, "Select '${seededConversation.subject}' conversation and swipe it right again to make it unread.")
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that the conversation became unread.")
        inboxPage.assertUnreadMarkerVisibility(seededConversation.subject, ViewMatchers.Visibility.VISIBLE)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'INBOX' scope because it has became archived.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'ARCHIVED' scope.")
        inboxPage.filterInbox("Archived")

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is displayed in the 'ARCHIVED' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'ARCHIVED' scope because it has became unarchived.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'INBOX' scope.")
        inboxPage.filterInbox("Inbox")

        Log.d(ASSERTION_TAG, "Assert that the '${seededConversation.subject}' conversation is displayed in the 'INBOX' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Select the conversation. Star it and mark it unread. (Preparing for swipe gestures in 'STARRED' and 'UNREAD' scope.")
        inboxPage.selectConversations(listOf(seededConversation.subject))
        inboxPage.clickStar()
        inboxPage.clickMarkAsUnread()

        Log.d(ASSERTION_TAG, "Assert that the selected number of conversations on the toolbar is 1. and the conversation is starred.")
        inboxPage.assertSelectedConversationNumber("1")
        inboxPage.assertConversationStarred(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'STARRED' scope.")
        inboxPage.filterInbox("Starred")

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'STARRED' scope.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = { refresh() }) {
            inboxPage.assertConversationDisplayed(seededConversation.subject)
        }

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' left.")
        inboxPage.swipeConversationLeft(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert it is removed from the 'STARRED' scope because it has became unstarred.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Navigate to 'UNREAD' scope.")
        inboxPage.filterInbox("Unread")

        Log.d(ASSERTION_TAG, "Assert that the conversation is displayed in the 'Unread' scope.")
        inboxPage.assertConversationDisplayed(seededConversation.subject)

        Log.d(STEP_TAG, "Swipe '${seededConversation.subject}' conversation right.")
        inboxPage.swipeConversationRight(seededConversation.subject)

        Log.d(ASSERTION_TAG, "Assert that it has disappeared from the 'UNREAD' scope.")
        inboxPage.assertConversationNotDisplayed(seededConversation.subject)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.INBOX, TestCategory.E2E)
    fun testComposeMessageShortcutsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 2, syllabusBody = "Syllabus body")
        val course = data.coursesList[0]
        val courseWithFrontPage = data.coursesList[1]
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]

        Log.d(PREPARATION_TAG, "Creating a Front Page for '${courseWithFrontPage.name}' course.")
        PagesApi.createCoursePage(courseWithFrontPage.id, teacher.token, frontPage = true, editingRoles = "public")

        Log.d(PREPARATION_TAG, "Setting up '${course.name}' course to have summary.")
        CoursesApi.updateCourseSettings(course.id, mapOf("syllabus_course_summary" to true))

        Log.d(PREPARATION_TAG, "Setting the Front Page for '${courseWithFrontPage.name}' course.")
        CoursesApi.updateCourse(courseWithFrontPage.id, UpdateCourse(homePage = "wiki", syllabusBody = "Syllabus body"))

        Log.d(PREPARATION_TAG, "Seed an assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed a calendar event for '${course.name}' course.")
        val testEvent = CalendarEventApi.createCalendarEvent(
            teacher.token,
            CanvasContext.makeContextId(CanvasContext.Type.COURSE, course.id),
            "Test Event",
            Date().toApiString()
        )

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        retryWithIncreasingDelay(
            initialDelay = 2000,
            times = 5,
            maxDelay = 30000,
            catchBlock = {
                composeTestRule.waitForIdle()
                coursesPage.refresh()
            }
        ) {
            coursesPage.clickCourseItem(course.name)
        }

        Log.d(ASSERTION_TAG, "Assert that the details of the '${course.name}' course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG, "Click assignment '${testAssignment.name}'.")
        courseDetailsPage.clickAssignment(testAssignment.name)

        val expectedSubjectAssignment = "Regarding: ${student.name}, Assignment - ${testAssignment.name}"
        Log.d(STEP_TAG, "Click FAB.")
        assignmentDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectAssignment'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectAssignment)

        Log.d(STEP_TAG, "Navigate back to course details.")
        inboxComposeMessagePage.clickOnCloseButton()
        Espresso.pressBack()

        val expectedSubjectGrades = "Regarding: ${student.shortName}, Grades"
        Log.d(STEP_TAG, "Click FAB.")
        courseDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectGrades'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectGrades)

        Log.d(STEP_TAG, "Navigate back to course details and select 'SYLLABUS' tab.")
        inboxComposeMessagePage.clickOnCloseButton()
        courseDetailsPage.selectTab("SYLLABUS")

        val expectedSubjectSyllabus = "Regarding: ${student.shortName}, Syllabus"
        Log.d(STEP_TAG, "Click FAB.")
        courseDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectSyllabus'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectSyllabus)

        Log.d(STEP_TAG, "Navigate back to course details and select 'SUMMARY' tab.")
        inboxComposeMessagePage.clickOnCloseButton()
        courseDetailsPage.selectTab("SUMMARY")

        val expectedSubjectSummary = "Regarding: ${student.shortName}, Summary"
        Log.d(STEP_TAG, "Click FAB.")
        courseDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectSummary'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectSummary)

        Log.d(STEP_TAG, "Navigate back to course list.")
        inboxComposeMessagePage.clickOnCloseButton()
        Espresso.pressBack()

        Log.d(STEP_TAG, " Select a '${courseWithFrontPage.name}' course (with 'Front Page' as home page) and select 'FRONT PAGE' tab.")
        coursesPage.clickCourseItem(courseWithFrontPage.name)
        courseDetailsPage.selectTab("FRONT PAGE")

        val expectedSubjectFrontPage = "Regarding: ${student.shortName}, Front Page"
        Log.d(STEP_TAG, "Click FAB.")
        courseDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectFrontPage'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectFrontPage)

        Log.d(STEP_TAG, "Navigate to Calendar and click on '${testEvent.title}' event.")
        inboxComposeMessagePage.clickOnCloseButton()
        Espresso.pressBack()
        dashboardPage.clickCalendarBottomMenu()
        calendarScreenPage.clickOnItem(testEvent.title.orEmpty())

        val expectedSubjectEvent = "Regarding: ${student.name}, Event - ${testEvent.title}"
        Log.d(STEP_TAG, "Click FAB.")
        calendarEventDetailsPage.clickComposeMessageFAB()

        Log.d(ASSERTION_TAG, "Assert that the Subject text is '$expectedSubjectEvent'.")
        inboxComposeMessagePage.assertSubjectText(expectedSubjectEvent)

        Log.d(STEP_TAG, "Type a message body and press the 'Send' icon on the top-right corner to send the message.")
        inboxComposeMessagePage.typeBody("This is a test message.")
        inboxComposeMessagePage.pressSendButton()

        Log.d(STEP_TAG, "Navigate to Inbox.")
        Espresso.pressBack()
        dashboardPage.openLeftSideMenu()
        leftSideNavigationDrawerPage.clickInbox()

        Log.d(STEP_TAG, "Filter to 'Sent' messages.")
        inboxPage.filterInbox("Sent")

        val expectedMessage = "This is a test message.\n\nRegarding: ${student.name}, https://${CanvasNetworkAdapter.canvasDomain}/courses/${course.id}/calendar_events/${testEvent.id}"
        Log.d(ASSERTION_TAG, "Assert the previously sent message's body is '$expectedMessage'.")
        inboxPage.openConversation(expectedSubjectEvent)
        inboxDetailsPage.assertMessageDisplayed(expectedMessage)
    }
}
