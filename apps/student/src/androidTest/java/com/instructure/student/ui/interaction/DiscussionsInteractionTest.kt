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

import android.os.SystemClock.sleep
import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addReplyToDiscussion
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.models.Tab
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Assert.assertNotNull
import org.junit.Test

// Note: Tests course discussions, not group discussions.
class DiscussionsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // Verify that a discussion header shows up properly after discussion creation
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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

    // Tests the creation of a discussion with an attachment.
    // It's actually impossible to attach anything to a discussion topic with our app,
    // so the attachment is done behind the scenes, after the fact.
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionCreate_withAttachment() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course = data.courses.values.first()

        val topicName = "Discussion created with attachment"
        val topicDescription = "created discussion topic"
        courseBrowserPage.selectDiscussions()
        discussionListPage.createDiscussionTopic(name = topicName, description = topicDescription)

        // Let's attach an html attachment after the fact
        val attachmentHtml =
                """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">Wait -- I just drank what? -- Socrates</p>
        </body>
        </html> """

        val topicHeader = data.courseDiscussionTopicHeaders[course.id]?.first()
        val attachment = createHtmlAttachment(data, attachmentHtml)
        topicHeader!!.attachments = mutableListOf(attachment)

        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(topicName)
        discussionListPage.selectTopic(topicName)
        discussionDetailsPage.assertTitleText(topicName)
        discussionDetailsPage.assertDescriptionText(topicDescription)
        discussionDetailsPage.assertMainAttachmentDisplayed()
        discussionDetailsPage.previewAndCheckMainAttachment(
                WebViewTextCheck(Locator.ID, "header1", "Famous Quote"),
                WebViewTextCheck(Locator.ID, "p1", "-- Socrates")
        )

    }

    // Test that you can't create a discussion when discussion creation is disabled
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionCreate_disabledWhenNotPermitted() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = false)

        courseBrowserPage.selectDiscussions()
        discussionListPage.assertDiscussionCreationDisabled()
    }


    // Tests that links to other Canvas content routes properly
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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
        sleep(500) // let's allow time for the webview to become populated/visible before we scroll to it
        discussionDetailsPage.scrollToRepliesWebview() // may be necessary on shorter screens / landscape
        // From what I can tell, our self-generated HTML has a 2500 ms wait before it
        // sends the "read" call for the unread messages on the page.  So we'll wait for
        // a few seconds.
        sleep(5000) // There have been instances where 3 seconds wasn't enough
        Espresso.pressBack() // Back to discussionListPage
        discussionListPage.pullToUpdate()
        discussionListPage.assertUnreadCount(topicHeader.title!!, 0)
    }

    // Tests that users can preview a discussion attachment.
    // Attachment is html, so that we can keep the viewing of it "in-house"
    // NOTE: Very similar to testDiscussionCreate_withAttachment
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussion_previewAttachment() {

        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course = data.courses.values.first()

        val attachmentHtml =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">No matter where you go, there you are -- Buckaroo Banzai</p>
        </body>
        </html> """

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course,
                user = data.users.values.first(),
                topicTitle = "Awesome topic",
                topicDescription = "With an attachment!"
        )
        val attachment = createHtmlAttachment(data,attachmentHtml)
        topicHeader.attachments = mutableListOf(attachment)

        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(topicHeader.title!!)
        discussionDetailsPage.assertTopicInfoShowing(topicHeader)
        discussionDetailsPage.assertMainAttachmentDisplayed()
        discussionDetailsPage.previewAndCheckMainAttachment(
                WebViewTextCheck(Locator.ID, "header1", "Famous Quote"),
                WebViewTextCheck(Locator.ID, "p1", "No matter where you go")
        )
    }


    // Tests that users can like entries and the correct like count is displayed, if the liking is enabled
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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
        sleep(1000) // Small wait to allow "like" to propagate
        discussionDetailsPage.assertLikeCount(discussionEntry, 1, refreshesAllowed = 2)
        discussionDetailsPage.clickLikeOnEntry(discussionEntry)
        sleep(1000) // Small wait to allow "unlike" to propagate
        discussionDetailsPage.assertLikeCount(discussionEntry, 0)
    }

    // Tests that discussion entry liking is not available when disabled
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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

    // Test that you can reply to a discussion (if enabled)
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionView_replies() {
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

    // Test that replies are not possible when they are not enabled
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
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
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionReply_base() {
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
        discussionDetailsPage.assertReplyDisplayed(discussionEntry, refreshesAllowed = 2)
    }

    // Tests replying with an attachment.
    // It is a whole other gear to manually specify an attachment the same way that a user would,
    // so we add the attachments programmatically.
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionReply_withAttachment() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user = data.users.values.first()

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user,
                topicTitle = "Hey!  A Discussion!",
                topicDescription = "Awesome!"
        )

        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(topicHeader.title!!)

        // Let's reply via the app
        val replyText = "I'm a reply"
        discussionDetailsPage.sendReply(replyText)

        // Now let's append the attachment after-the-fact, since it is very hard
        // to manually attach anything via Espresso, since it would require manipulating
        // system UIs.
        val attachmentHtml =
                """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">That's one small step for man, one giant leap for mankind -- Neil Armstrong</p>
        </body>
        </html> """

        val discussionEntry = findDiscussionEntry(data, topicHeader.title!!, replyText)
        val attachment = createHtmlAttachment(data, attachmentHtml)
        discussionEntry.attachments = mutableListOf(attachment)

        discussionDetailsPage.refresh() // To pick up updated reply
        discussionDetailsPage.assertReplyDisplayed(discussionEntry)
        discussionDetailsPage.assertReplyAttachment(discussionEntry)
        discussionDetailsPage.previewAndCheckReplyAttachment(discussionEntry,
                WebViewTextCheck(Locator.ID,"header1", "Famous Quote"),
                WebViewTextCheck(Locator.ID, "p1", "That's one small step"))
    }

    // Tests that we can make a threaded reply to a reply
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionReply_threaded() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user = data.users.values.first()

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user,
                topicTitle = "Wow!  A Discussion!",
                topicDescription = "Cool!"
        )

        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(topicHeader.title!!)

        // Let's reply via the app
        val replyText = "I'm a reply"
        discussionDetailsPage.sendReply(replyText)

        // Verify that our reply has made it to the screen
        val replyEntry = findDiscussionEntry(data, topicHeader.title!!, replyText)
        discussionDetailsPage.assertReplyDisplayed(replyEntry, refreshesAllowed = 2)

        // Now let's reply to the reply (i.e., threaded reply)
        val replyReplyText = "Threaded Reply"
        discussionDetailsPage.replyToReply(replyEntry,replyReplyText)

        // And verify that our reply-to-reply is showing
        val replyReplyEntry = findDiscussionEntry(data, topicHeader.title!!, replyReplyText)
        discussionDetailsPage.assertReplyDisplayed(replyReplyEntry, refreshesAllowed = 2)
    }

    // Tests that we can make a threaded reply with an attachment
    // It is a whole other gear to manually specify an attachment the same way that a user would,
    // so we add the attachments programmatically.
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.INTERACTION, false)
    fun testDiscussionReply_threadedWithAttachment() {
        val data = getToCourse(studentCount = 1, courseCount = 1, enableDiscussionTopicCreation = true)
        val course1 = data.courses.values.first()
        val user = data.users.values.first()

        val topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user,
                topicTitle = "Discussion threaded reply attachment",
                topicDescription = "Cool!"
        )

        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(topicHeader.title!!)

        // Let's reply via the app
        val replyText = "I'm a reply"
        discussionDetailsPage.sendReply(replyText)

        // Make sure that the reply is displayed, so that we can reply to it
        val replyEntry = findDiscussionEntry(data, topicHeader.title!!, replyText)
        discussionDetailsPage.assertReplyDisplayed(replyEntry, refreshesAllowed = 2)

        // Now let's reply to the reply (i.e., threaded reply)
        val replyReplyText = "Threaded Reply"
        discussionDetailsPage.replyToReply(replyEntry,replyReplyText)

        // And verify that our reply-to-reply is showing
        val replyReplyEntry = findDiscussionEntry(data, topicHeader.title!!, replyReplyText)

        // Lets attach an html attachment behind the scenes
        val attachmentHtml =
                """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">The only thing we have to fear is fear itself -- FDR</p>
        </body>
        </html> """

        val attachment = createHtmlAttachment(data,attachmentHtml)
        replyReplyEntry.attachments = mutableListOf(attachment)

        discussionDetailsPage.refresh() // To pick up updated discussion reply
        discussionDetailsPage.assertReplyDisplayed(replyReplyEntry)
        discussionDetailsPage.assertReplyAttachment(replyReplyEntry)
        discussionDetailsPage.previewAndCheckReplyAttachment(replyReplyEntry,
                WebViewTextCheck(Locator.ID,"header1", "Famous Quote"),
                WebViewTextCheck(Locator.ID, "p1", "The only thing we have to fear"))

    }

    //
    // Utilities
    //

    // Needed to grab the discussion entry associated with a manual discussion reply
    private fun findDiscussionEntry(data: MockCanvas, topicName: String, replyMessage: String) : DiscussionEntry {
        // Gotta grab our reply message...
        val myCourse = data.courses.values.first()
        val topicHeader = data.courseDiscussionTopicHeaders[myCourse.id]?.find { it.title.equals(topicName) }
        assertNotNull("Can't find topic header", topicHeader)
        val topic = data.discussionTopics[topicHeader!!.id]
        assertNotNull("Can't find topic", topic)
        var discussionEntry = topic!!.views.find { it.message.equals(replyMessage) }
        if(discussionEntry == null) {
            // It might be a threaded reply
            topic.views.forEach { view ->
                view.replies?.forEach { reply ->
                    if(reply.message.equals(replyMessage)) {
                        return reply
                    }
                }
            }
        }
        assertNotNull("Can't find discussionEntry", discussionEntry)

        return discussionEntry!!
    }

    // Mock a specified number of students and courses, and navigate to the first course
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

    companion object {
        // Creates an HTML attachment/file which can then be attached to a topic header or reply.
        fun createHtmlAttachment(data: MockCanvas, html: String): RemoteFile {
            val course1 = data.courses.values.first()
            val fileId = data.addFileToCourse(
                    courseId = course1.id,
                    displayName = "page.html",
                    contentType = "text/html",
                    fileContent = html
            )

            val attachment = RemoteFile(
                    id = fileId,
                    displayName = "page.html",
                    fileName = "page.html",
                    contentType = "text/html",
                    url = "https://mock-data.instructure.com/files/$fileId/preview",
                    size = html.length.toLong()
            )

            return attachment
        }

    }

}
