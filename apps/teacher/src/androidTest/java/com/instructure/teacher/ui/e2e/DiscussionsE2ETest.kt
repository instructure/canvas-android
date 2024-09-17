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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class DiscussionsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E)
    fun testDiscussionE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, discussions = 2)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val discussion = data.discussionsList[0]
        val discussion2 = data.discussionsList[1]

        val discussionEntryMessage = "Discussion entry test message"
        val testDiscussionTopicEntry = DiscussionTopicsApi.createEntryToDiscussionTopic(student.token, course.id, discussion.id, discussionEntryMessage)

        val testDiscussionEntryReplyMessage = "This is a reply for the entry for testing purposes!"
        DiscussionTopicsApi.createReplyToDiscussionTopicEntry(student.token, course.id, discussion.id, testDiscussionTopicEntry.id, testDiscussionEntryReplyMessage)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open '${course.name}' course.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.waitForRender()

        Log.d(STEP_TAG,"Open Discussions Page and assert has discussions: '${discussion.title}' and '${discussion2.title}'.")
        courseBrowserPage.openDiscussionsTab()
        discussionsListPage.assertHasDiscussion(discussion)
        discussionsListPage.assertHasDiscussion(discussion2)

        Log.d(STEP_TAG,"Click on '${discussion.title}' discussion.")
        discussionsListPage.clickDiscussion(discussion)

        Log.d(STEP_TAG, "Assert that the 'Reply' button is displayed on the discussion details (web view) page.")
        discussionDetailsPage.waitForReplyButtonDisplayed()
        discussionDetailsPage.assertReplyButtonDisplayed()

        Log.d(STEP_TAG, "Assert that the toolbar's title is the '${discussion.title}' discussion's title.")
        discussionDetailsPage.assertToolbarDiscussionTitle(discussion.title)

        Log.d(STEP_TAG, "Click on the more menu of the announcement and assert if the more menu items are all displayed.")
        discussionDetailsPage.clickOnDiscussionMoreMenu()
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Mark All as Read")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Mark All as Unread")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Edit")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Delete")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Close for Comments")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Send To...")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Copy To...")
        discussionDetailsPage.assertMoreMenuButtonDisplayed("Share to Commons")

        Log.d(STEP_TAG, "Assert that the '$discussionEntryMessage' discussion entry message is displayed.")
        discussionDetailsPage.assertDiscussionEntryMessageDisplayed(discussionEntryMessage)

        Log.d(STEP_TAG, "Assert that there is 1 reply and that is unread.")
        discussionDetailsPage.assertReplyCounter(1, 1)

        Log.d(STEP_TAG, "Expand the replies and wait for the reply to be displayed. Assert that it's displayed.")
        discussionDetailsPage.clickOnExpandRepliesButton()
        discussionDetailsPage.waitForReplyDisplayed(testDiscussionEntryReplyMessage)
        discussionDetailsPage.assertReplyDisplayed(testDiscussionEntryReplyMessage)

        Log.d(STEP_TAG, "Navigate back to Discussion List Page. Select 'Pin' overflow menu of '${discussion2.title}' discussion and assert that it has became Pinned.")
        Espresso.pressBack()
        discussionsListPage.clickDiscussionOverFlowMenu(discussion2.title)
        discussionsListPage.selectOverFlowMenu("Pin")
        discussionsListPage.assertGroupDisplayed("Pinned")
        discussionsListPage.assertDiscussionInGroup("Pinned", discussion2.title)
        discussionsListPage.assertDiscussionNotInGroup("Discussions", discussion2.title)

        Log.d(STEP_TAG, "Select 'Unpin' overflow menu of '${discussion2.title}' discussion and assert that it has became Unpinned, so it will be displayed (again) in the 'Discussions' group.")
        discussionsListPage.clickDiscussionOverFlowMenu(discussion2.title)
        discussionsListPage.selectOverFlowMenu("Unpin")
        discussionsListPage.assertDiscussionInGroup("Discussions", discussion2.title)
        discussionsListPage.assertDiscussionNotInGroup("Pinned", discussion2.title)

        Log.d(STEP_TAG, "Assert that both of the discussions, '${discussion.title}' and '${discussion2.title}' discussions are displayed.")
        discussionsListPage.assertHasDiscussion(discussion)
        discussionsListPage.assertHasDiscussion(discussion2)

        Log.d(STEP_TAG, "Select 'Closed for Comments' overflow menu of '${discussion.title}' discussion and assert that it has became 'Closed for Comments'.")
        discussionsListPage.clickDiscussionOverFlowMenu(discussion.title)
        discussionsListPage.selectOverFlowMenu("Close for Comments")
        discussionsListPage.assertGroupDisplayed("Closed for Comments")
        discussionsListPage.assertDiscussionInGroup("Closed for Comments", discussion.title)

        Log.d(STEP_TAG, "Assert that the 'Discussions' group will be still displayed despite it has no items in it. Assert that the '${discussion2.title}' discussion is not in the 'Discussions' group any more.")
        discussionsListPage.assertGroupDisplayed("Discussions")
        discussionsListPage.assertDiscussionNotInGroup("Discussions", discussion.title)

        Log.d(STEP_TAG, "Select 'Open for Comments' overflow menu of '${discussion.title}' discussion and assert that it will be (again) displayed under the 'Discussions' group.")
        discussionsListPage.clickDiscussionOverFlowMenu(discussion.title)
        discussionsListPage.selectOverFlowMenu("Open for Comments")
        discussionsListPage.assertDiscussionInGroup("Discussions", discussion.title)

        Log.d(STEP_TAG, "Assert that the '${discussion2.title}' discussion is not in the 'Closed for Comments' group any more.")
        discussionsListPage.assertDiscussionNotInGroup("Closed for Comments", discussion.title)

        Log.d(STEP_TAG,"Click on more menu of '${discussion.title}' discussion and delete it.")
        discussionsListPage.deleteDiscussionFromOverflowMenu(discussion.title)

        Log.d(STEP_TAG,"Assert that the previously deleted '${discussion.title}' discussion is not displayed, but the other, '${discussion2.title}' discussion is.")
        sleep(2000) //Allow the deletion to propagate
        discussionsListPage.assertDiscussionDoesNotExist(discussion.title)
        discussionsListPage.assertHasDiscussion(discussion2)

        Log.d(STEP_TAG,"Click on '+' icon on the UI to create a new discussion.")
        discussionsListPage.createNewDiscussion()

        val newDiscussionTitle = "Test Discussion Mobile UI"
        Log.d(STEP_TAG,"Set '$newDiscussionTitle' as the discussion's title and set some description as well.")
        editDiscussionsDetailsPage.editDiscussionTitle(newDiscussionTitle)
        editDiscussionsDetailsPage.editDiscussionDescription("Mobile UI Discussion description")

        Log.d(STEP_TAG,"Toggle 'Publish' checkbox and save the page.")
        editDiscussionsDetailsPage.togglePublished()
        editDiscussionsDetailsPage.clickSendNewDiscussion()

        Log.d(STEP_TAG,"Assert that '$newDiscussionTitle' discussion is displayed and published.")
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)
        discussionsListPage.clickDiscussion(newDiscussionTitle)

        Log.d(STEP_TAG, "Assert that the toolbar's title is the '$newDiscussionTitle' discussion's title.")
        discussionDetailsPage.assertToolbarDiscussionTitle(newDiscussionTitle)
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on the Search icon and type some search query string which matches only with the previously created discussion's title.")
        discussionsListPage.searchable.clickOnSearchButton()
        discussionsListPage.searchable.typeToSearchBar("Test Discussion")

        Log.d(STEP_TAG,"Assert that the '$newDiscussionTitle' discussion is displayed and it is the only one.")
        discussionsListPage.assertDiscussionCount(1)
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)
        discussionsListPage.searchable.clickOnClearSearchButton()

        Log.d(STEP_TAG, "Quit from Searching mechanism.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on more menu of '${discussion2.title}' discussion and delete it.")
        discussionsListPage.deleteDiscussionFromOverflowMenu(discussion2.title)

        Log.d(STEP_TAG,"Assert that the previously deleted '${discussion2.title}' discussion is not displayed.")
        sleep(2000) //Allow the deletion to propagate
        discussionsListPage.assertDiscussionDoesNotExist(discussion2.title)

        Log.d(STEP_TAG,"Collapse the discussion list and assert that the '$newDiscussionTitle' discussion can NOT be seen.")
        discussionsListPage.toggleCollapseExpandIcon()
        discussionsListPage.assertDiscussionCount(0) // header only
        discussionsListPage.assertDiscussionDoesNotExist(newDiscussionTitle)

        Log.d(STEP_TAG,"Expand the discussion list and assert that the '$newDiscussionTitle' discussion can be seen.")
        discussionsListPage.toggleCollapseExpandIcon()
        discussionsListPage.assertDiscussionCount(1)
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)
    }
}