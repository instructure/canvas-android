/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class DiscussionTopicHeaderUnitTest : Assert() {

    @Test
    fun testDiscussionTopicHeader() {
        val discussionTopicHeader: DiscussionTopicHeader = discussionTopicHeaderJSON.parse()

        Assert.assertNotNull(discussionTopicHeader)
        Assert.assertTrue(discussionTopicHeader.pinned)
        Assert.assertTrue(discussionTopicHeader.status == DiscussionTopicHeader.ReadState.UNREAD)
        Assert.assertTrue(discussionTopicHeader.id > 0)
        Assert.assertTrue(discussionTopicHeader.assignmentId > 0)
        Assert.assertNotNull(discussionTopicHeader.author)
        Assert.assertNotNull(discussionTopicHeader.htmlUrl)
        Assert.assertNotNull(discussionTopicHeader.lastReplyDate)
        Assert.assertNotNull(discussionTopicHeader.message)
        Assert.assertNotNull(discussionTopicHeader.message)
        Assert.assertNotNull(discussionTopicHeader.postedDate)
        Assert.assertNotNull(discussionTopicHeader.title)
        Assert.assertTrue(discussionTopicHeader.type == DiscussionTopicHeader.DiscussionType.SIDE_COMMENT)

        testDiscussionParticipant(discussionTopicHeader.author!!)
        testDiscussionAttachments(discussionTopicHeader.attachments)
        testDiscussionPermissions(discussionTopicHeader.permissions!!)
    }

    @Test
    fun testLockedDiscussionTopic() {
        val lockedDiscussion: DiscussionTopicHeader = lockedDiscussionTopicJSON.parse()

        val lockInfo = lockedDiscussion.lockInfo
        Assert.assertNotNull(lockInfo)

        // If the DiscussionTopic is Locked for the user, make sure the lock_info & explanation aren't empty/null
        if (lockedDiscussion.lockedForUser) {
            Assert.assertTrue(!lockedDiscussion.lockInfo!!.isEmpty)
            Assert.assertNotNull(lockedDiscussion.lockExplanation)
        }

        // The lock_info should have a context_module
        val lockedModule = lockInfo!!.contextModule
        Assert.assertNotNull(lockedModule)
        Assert.assertNotNull(lockedModule?.id)
        Assert.assertNotNull(lockedModule?.contextId)
        Assert.assertNotNull(lockedModule?.contextType)
        Assert.assertNotNull(lockedModule?.name)
        Assert.assertNotNull(lockedModule?.unlockAt)
        Assert.assertNotNull(lockedModule?.isRequireSequentialProgress)

        val completionRequirements = lockedModule?.completionRequirements
        Assert.assertNotNull(completionRequirements)
        Assert.assertEquals(3, completionRequirements?.size)
        for (requirement in completionRequirements!!) {
            Assert.assertNotNull(requirement.id)
            Assert.assertNotNull(requirement.type)
        }
    }

    private fun testDiscussionParticipant(participant: DiscussionParticipant) {
        Assert.assertTrue(participant.id > 0)
        Assert.assertNotNull(participant.avatarImageUrl)
        Assert.assertNotNull(participant.displayName)
        Assert.assertNotNull(participant.htmlUrl)
    }

    private fun testDiscussionAttachments(attachments: List<RemoteFile>) {
        Assert.assertNotNull(attachments)
        Assert.assertEquals(2, attachments.size)

        for (attachment in attachments) {
            Assert.assertNotNull(attachment.id)
            Assert.assertNotNull(attachment.locked)
            Assert.assertNotNull(attachment.lockedForUser)
            Assert.assertNotNull(attachment.hidden)
            Assert.assertNotNull(attachment.hiddenForUser)
            Assert.assertNotNull(attachment.size)
            Assert.assertNotNull(attachment.lockAt)
            Assert.assertNotNull(attachment.unlockAt)
            Assert.assertNotNull(attachment.updatedAt)
            Assert.assertNotNull(attachment.createdAt)
            Assert.assertNotNull(attachment.displayName)
            Assert.assertNotNull(attachment.fileName)
            Assert.assertNotNull(attachment.url)
            Assert.assertNotNull(attachment.contentType)
            Assert.assertNotNull(attachment.folderId)
            Assert.assertNotNull(attachment.thumbnailUrl)
        }
    }

    private fun testDiscussionPermissions(permission: DiscussionTopicPermission) {
        Assert.assertNotNull(permission)
        Assert.assertNotNull(permission.attach)
        Assert.assertNotNull(permission.update)
        Assert.assertNotNull(permission.delete)
    }

    @Language("JSON")
    private var discussionTopicHeaderJSON = """
      {
        "assignment_id": 3301597,
        "delayed_post_at": null,
        "id": 2111813,
        "last_reply_at": "2013-05-29T15:51:18Z",
        "lock_at": null,
        "podcast_has_student_posts": false,
        "posted_at": "2013-05-23T22:16:55Z",
        "root_topic_id": null,
        "title": "Which programming language is best?",
        "user_name": "Derrick Hathaway",
        "discussion_subentry_count": 2,
        "permissions": {
          "attach": true,
          "update": true,
          "delete": true
        },
        "message": "\u003Cp\u003EUse complete sentences and plenty of adverbs.\u003C/p\u003E",
        "discussion_type": "side_comment",
        "require_initial_post": null,
        "user_can_see_posts": true,
        "podcast_url": null,
        "pinned": true,
        "position": 236,
        "read_state": "unread",
        "unread_count": 0,
        "subscribed": false,
        "topic_children": [],
        "attachments": [
          {
            "id": 65668108,
            "folder_id": 4815146,
            "content-type": "image/png",
            "display_name": "banner.png",
            "filename": "banner.png",
            "url": "https://pandadev.instructure.com/files/65668108/download",
            "size": 16665,
            "created_at": "2015-03-13T15:58:56Z",
            "updated_at": "2015-03-13T15:58:56Z",
            "unlock_at": "2017-04-13T15:58:56Z",
            "locked": false,
            "hidden": false,
            "lock_at": "2015-04-13T15:58:56Z",
            "hidden_for_user": false,
            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/65668108/banner_thumb.png",
            "locked_for_user": false
          },
          {
            "id": 12345,
            "folder_id": 12343,
            "content-type": "image/png",
            "display_name": "banner.png",
            "filename": "fdsfa.png",
            "url": "https://pandadev.instructure.com/files/65668108/download",
            "size": 3432,
            "created_at": "2015-03-13T15:58:56Z",
            "updated_at": "2015-03-13T15:58:56Z",
            "unlock_at": "2017-04-13T15:58:56Z",
            "locked": false,
            "hidden": false,
            "lock_at": "2015-04-13T15:58:56Z",
            "hidden_for_user": false,
            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/65668108/banner_thumb.png",
            "locked_for_user": false
          }
        ],
        "published": true,
        "locked": false,
        "author": {
          "id": 170000003828513,
          "display_name": "Derrick Hathaway",
          "avatar_image_url": "https://secure.gravatar.com/avatar/1753d19b1ddf16cb0a31d983f97f4488?s=50\u0026d=https%3A%2F%2Fpandadev.instructure.com%2Fimages%2Fdotted_pic.png",
          "html_url": "https://pandadev.instructure.com/courses/24219/users/17~3828513"
        },
        "html_url": "https://pandadev.instructure.com/courses/24219/discussion_topics/2111813",
        "locked_for_user": false,
        "url": "https://pandadev.instructure.com/courses/24219/discussion_topics/2111813",
        "assignment": {
          "assignment_group_id": 6783,
          "automatic_peer_reviews": false,
          "description": "\u003Cp\u003EUse complete sentences and plenty of adverbs.\u003C/p\u003E",
          "due_at": "2013-05-28T05:59:00Z",
          "grade_group_students_individually": false,
          "grading_standard_id": null,
          "grading_type": "letter_grade",
          "group_category_id": null,
          "id": 3301597,
          "lock_at": null,
          "peer_reviews": false,
          "points_possible": 6,
          "position": 4,
          "unlock_at": null,
          "course_id": 24219,
          "name": "Which programming language is best?",
          "submission_types": [
            "discussion_topic"
          ],
          "muted": false,
          "html_url": "https://pandadev.instructure.com/courses/24219/assignments/3301597",
          "needs_grading_count": 1,
          "locked_for_user": false
        }
      }"""

    @Language("JSON")
    private val lockedDiscussionTopicJSON = """
      {
        "assignment_id": null,
        "delayed_post_at": null,
        "discussion_type": "side_comment",
        "id": 2557508,
        "last_reply_at": "2013-10-09T21:39:29Z",
        "lock_at": null,
        "podcast_has_student_posts": null,
        "position": 17,
        "posted_at": "2013-10-09T21:39:29Z",
        "root_topic_id": null,
        "title": "Locked by module",
        "user_name": "Brady fdsa",
        "discussion_subentry_count": 0,
        "permissions": {},
        "message": null,
        "require_initial_post": null,
        "user_can_see_posts": true,
        "podcast_url": null,
        "read_state": "read",
        "unread_count": 0,
        "subscribed": false,
        "topic_children": [],
        "attachments": [],
        "published": true,
        "can_unpublish": false,
        "locked": false,
        "can_lock": true,
        "author": {},
        "html_url": "https://pandadev.instructure.com/courses/833052/discussion_topics/2557508",
        "url": "https://pandadev.instructure.com/courses/833052/discussion_topics/2557508",
        "pinned": false,
        "group_category_id": null,
        "can_group": true,
        "locked_for_user": true,
        "lock_info": {
          "asset_string": "discussion_topic_2557508",
          "context_module": {
            "id": 1059721,
            "context_id": 833052,
            "context_type": "Course",
            "name": "Advanced",
            "position": 2,
            "prerequisites": [
              {
                "id": 1059720,
                "type": "context_module",
                "name": "Beginners"
              }
            ],
            "completion_requirements": [
              {
                "id": 10371213,
                "type": "must_contribute",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 10432029,
                "type": "must_contribute",
                "min_score": 0,
                "max_score": null
              },
              {
                "id": 10432069,
                "type": "min_score",
                "min_score": 1,
                "max_score": null
              }
            ],
            "created_at": "2013-07-24T20:08:18Z",
            "updated_at": "2014-01-28T04:09:00Z",
            "workflow_state": "active",
            "deleted_at": null,
            "unlock_at": "2013-07-31T06:00:00Z",
            "start_at": null,
            "end_at": null,
            "migration_id": null,
            "require_sequential_progress": false,
            "cloned_item_id": null,
            "completion_events": null
          }
        },
        "lock_explanation": "This content is part of the module <b>Advanced</b> and hasn&#x27;t been unlocked yet.<br/><a href='https://pandadev.instructure.com/courses/833052/modules#module_1059721'>Visit the course modules page for information on how to unlock this content.</a><a href='/courses/833052/modules/1059721/prerequisites/discussion_topic_2557508' style='display: none;' id='module_prerequisites_lookup_link'>&nbsp;</a>"
      }"""
}
