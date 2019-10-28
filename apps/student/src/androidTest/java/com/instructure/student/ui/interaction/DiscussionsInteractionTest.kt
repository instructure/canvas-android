/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import android.os.SystemClock
import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addReplyToDiscussion
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.Tab
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Assert.assertNotNull
import org.junit.Test

class DiscussionsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // Verify that a discussion header shows up properly after discussion creation
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionCreate_base() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)

        val topicName = "Discussion create base"
        val topicDescription = "created discussion topic"
        courseBrowserPage.selectDiscussions()
        discussionListPage.createDiscussionTopic(name = topicName, description = topicDescription)
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(topicName)
        discussionListPage.selectTopic(topicName)
        discussionDetailsPage.assertTitleText(topicName)
        discussionDetailsPage.assertDescriptionText(topicDescription)
    }

    //    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
//    fun testDiscussionCreate_withAttachment() {
//
//    }
//
    // Test that you can't create a discussion when discussion creation is disabled
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionCreate_disabledWhenNotPermitted() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = false)

        courseBrowserPage.selectDiscussions()
        discussionListPage.assertDiscussionCreationDisabled()
    }


    // Tests that links to other Canvas content routes properly
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussion_linksRouteInApp() {
        val data = getToCourse(studentCount = 2, courseCount = 2, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val course2 = data.courses.values.last()
        val user1 = data.users.values.first()

        val course2Url = "https://mock-data.instructure.com/api/v1/courses/${course2.id}" // TODO: Less hard-coded?
        val course2LinkElementId = "courseLink"
        val course2Html = "<a id=\"$course2LinkElementId\" href=\"$course2Url\">course2</a>"
        val topicName = "Discussion with link in description"

        data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = topicName,
                topicDescription = course2Html
        )
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.selectTopic(topicName)
        discussionDetailsPage.clickLinkInDescription(course2LinkElementId) // Should navigate to course2

        courseBrowserPage.assertTitleCorrect(course2)
    }

    // Replies automatically get marked as read as the user scrolls through the list
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussion_postsGetMarkedAsRead() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course = data.courses.values.first()
        val user = data.users.values.first()
        val topicName = "Discussion with one reply"
        val topicDescription = "disc w/ reply"
        val replyMessage = "I'm unread (at first)"

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course,
                user = user,
                topicTitle = topicName,
                topicDescription = topicDescription
        )
        val discussionEntry = data.addReplyToDiscussion(
                topicHeader = topicHeader,
                user = user,
                replyMessage = replyMessage
        )

        // Bring up discussion page
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.assertUnreadCount(topicHeader.title!!, 1)
        discussionListPage.selectTopic(topicHeader.title!!)
        //discussionDetailsPage.swipeUpReplyButton()
        discussionDetailsPage.swipeReplyInfoView(discussionEntry)
        // From what I can tell, our self-generated HTML has a 2500 ms wait before it
        // send the "read" call for the unread messages on the page.  So we'll wait for
        // 3 seconds.
        sleep(3000)
        Espresso.pressBack() // Back to discussionListPage
        discussionListPage.pullToUpdate()
        discussionListPage.assertUnreadCount(topicHeader.title!!, 0)
    }

//    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
//    fun testDiscussion_previewAttachment() {
//
//    }
//

    // Tests that users can like entries and the correct like count is displayed, if the liking is enabled
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionLikePost_base() {

        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course = data.courses.values.first()
        val user = data.users.values.first()
        val topicName = "Discussion with likable posts"
        val topicDescription = "likable discussion"
        val replyMessage = "Like me!"

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course,
                user = user,
                topicTitle = topicName,
                topicDescription = topicDescription
        )
        val discussionEntry = data.addReplyToDiscussion(
                topicHeader = topicHeader,
                user = user,
                replyMessage = replyMessage
        )

        // Bring up discussion page
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(topicName)
        discussionListPage.selectTopic(topicName)

        // Check that favoriting works as expected
        discussionDetailsPage.assertFavoritingEnabled(discussionEntry)
        discussionDetailsPage.assertLikeCount(discussionEntry, 0)
        discussionDetailsPage.clickLikeOnEntry(discussionEntry)
        sleep(1000) // TODO: Something else
        discussionDetailsPage.assertLikeCount(discussionEntry, 1)
        discussionDetailsPage.clickLikeOnEntry(discussionEntry)
        sleep(1000)
        discussionDetailsPage.assertLikeCount(discussionEntry, 0)
    }

    // Tests that discussion entry liking is not available when disabled
    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionLikePost_disabledWhenNotPermitted() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        data.discussionRatingsEnabled = false

        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion with unlikable posts",
                topicDescription = "unlikable discussion",
                allowRating = false
        )
        val discussionEntry = data.addReplyToDiscussion(
                topicHeader = topicHeader,
                user = user1,
                replyMessage = "You can't touch this!"
        )
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(topicHeader.title!!)
        discussionListPage.selectTopic(topicHeader.title!!)

        discussionDetailsPage.assertFavoritingDisabled(discussionEntry)
    }

    // Test basic discussion view
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_base() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion view base",
                topicDescription = "A viewed discussion"
        )
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(topicHeader.title!!)
        discussionListPage.selectTopic(topicHeader.title!!)
        discussionDetailsPage.assertTopicInfoShowing(topicHeader)
    }

    // Test that you can reply to a discussion
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_replies() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion with replies enabled",
                topicDescription = "Replies enabled"
        )
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.selectTopic(topicHeader.title!!)
        discussionDetailsPage.assertTopicInfoShowing(topicHeader)
        discussionDetailsPage.assertRepliesEnabled()

        // Let's reply via the app
        val replyText = "I'm a reply"
        discussionDetailsPage.sendReply(replyText)
        val discussionEntry = findDiscussionEntry(data, topicHeader.title!!, replyText)
        discussionDetailsPage.assertReplyDisplayed(discussionEntry)
    }

    // Test that replies are not possible when they are not allowed
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionView_repliesHiddenWhenNotPermitted() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        data.discussionRepliesEnabled = false // Do we still need these?
        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion with replies disabled",
                topicDescription = "Replies disabled",
                allowReplies = false
        )
        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.selectTopic(topicHeader.title!!)
        discussionDetailsPage.assertRepliesDisabled()
    }

    // Test that a reply is displayed properly
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
    fun testDiscussionReply_base() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion with replies",
                topicDescription = "Reply-o-rama"
        )
        val discussionEntry = data.addReplyToDiscussion(
                topicHeader = topicHeader,
                user = user1,
                replyMessage = "Replied"
        )

        courseBrowserPage.selectDiscussions()
        discussionListPage.pullToUpdate()
        discussionListPage.selectTopic(topicHeader.title!!)
        discussionDetailsPage.assertRepliesDisplayed()

        discussionDetailsPage.assertReplyDisplayed(discussionEntry)
    }

//    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
//    fun testDiscussionReply_withAttachment() {
//
//    }
//
//    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
//    fun testDiscussionReply_threaded() {
//
//    }
//
//    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, true)
//    fun testDiscussionReply_threadedWithAttachment() {
//
//    }

    // Needed to grab the discussion entry associated with a manual discussion reply
    private fun findDiscussionEntry(data: MockCanvas, topicName: String, replyMessage: String) : DiscussionEntry {
        // Gotta grab our reply message...
        val myCourse = data.courses.values.first()
        val topicHeader = data.courseDiscussionTopicHeaders[myCourse.id]?.find { it.title.equals(topicName) }
        assertNotNull("Can't find topic header", topicHeader)
        val topic = data.discussionTopics[topicHeader!!.id]
        assertNotNull("Can't find topic", topic)
        val discussionEntry = topic!!.views.find { it.message.equals(replyMessage) }
        assertNotNull("Can't find discussionEntry", discussionEntry)

        return discussionEntry!!
    }

    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1,
            enableDiscussionTopicCreation: Boolean = true): MockCanvas {
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)

        if (enableDiscussionTopicCreation) {
            data.courses.values.forEach { course ->
                course.permissions = CanvasContextPermission(canCreateDiscussionTopic = true)
            }
        }
        val course1 = data.courses.values.first()
        val discussionsTab = Tab(position = 2, label = "Discussions", visibility = "public", tabId = Tab.DISCUSSIONS_ID)
        data.courseTabs[course1.id]!! += discussionsTab

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course1)

        return data
    }

}
