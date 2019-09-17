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

import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class StreamUnitTest : Assert() {

    @Test
    fun personalStreamTest() {
        val personalStream: Array<StreamItem> = personalStreamJSON.parse()
        Assert.assertNotNull(personalStream)

        val streamItem = personalStream[0]
        Assert.assertNotNull(streamItem)
        Assert.assertTrue(streamItem.id == 98502910L)
        Assert.assertTrue(StreamItem.Type.isDiscussionTopic(streamItem))
        Assert.assertTrue(streamItem.courseId == 1393179L)
    }

    @Test
    fun testStreamItemCourse() {
        val streamItem: StreamItem = courseStreamItemJSON.parse()

        Assert.assertNotNull(streamItem)
        Assert.assertNotNull(streamItem.htmlUrl)
        Assert.assertNotNull(streamItem.contextType)
        Assert.assertNotNull(streamItem.type)
        Assert.assertTrue(streamItem.id > 0)
    }

    @Test
    fun testStreamItemCourseId() {
        val streamItem: StreamItem = courseShardStreamItemJSON.parse()

        Assert.assertNotNull(streamItem)
        Assert.assertEquals(836350000000001123L, streamItem.courseId)
    }

    @Test
    fun testStreamItemAssignmentId() {
        val streamItem: StreamItem = courseShardStreamItemJSON.parse()

        Assert.assertNotNull(streamItem)
        Assert.assertEquals(983440000000001212L, streamItem.assignmentId)
    }

    /**
     * personal stream
     * @GET("/users/self/activity_stream")
     * void getUserStream(Callback<StreamItem[]> callback);
     */
    @Language("JSON")
    private val personalStreamJSON = """
      [
        {
          "created_at": "2014-07-15T21:46:46Z",
          "updated_at": "2014-07-15T21:46:50Z",
          "id": 98502910,
          "title": "new discussion threaded 1",
          "message": "<p>new discussion threaded 1</p>",
          "type": "DiscussionTopic",
          "read_state": false,
          "context_type": "Course",
          "course_id": 1393179,
          "discussion_topic_id": 5919006,
          "html_url": "https://mobileqa.instructure.com/courses/1393179/discussion_topics/5919006",
          "total_root_discussion_entries": 0,
          "require_initial_post": false,
          "user_has_posted": null,
          "root_discussion_entries": []
        },
        {
          "created_at": "2014-07-15T19:09:45Z",
          "updated_at": "2014-07-15T19:12:00Z",
          "id": 98491440,
          "title": "yyyy",
          "message": "<p>yyyy</p>",
          "type": "DiscussionTopic",
          "read_state": false,
          "context_type": "Course",
          "course_id": 1393179,
          "discussion_topic_id": 5918712,
          "html_url": "https://mobileqa.instructure.com/courses/1393179/discussion_topics/5918712",
          "total_root_discussion_entries": 0,
          "require_initial_post": false,
          "user_has_posted": null,
          "root_discussion_entries": []
        },
        {
          "created_at": "2014-07-10T17:23:35Z",
          "updated_at": "2014-07-10T17:23:35Z",
          "id": 98218837,
          "title": "Assignment Created - The world cup write up, IOS Topdown 4 (Old Data)",
          "message": "          \nA new assignment has been created for your course, IOS Topdown 4 (Old Data)\n\nThe world cup write up\n\n  due: No Due Date\n\nClick here to view the assignment: \nhttps://mobileqa.instructure.com/courses/1098050/assignments/5152053\n\n\n\n\n\n\n          ________________________________________\n\n          You received this email because you are participating in one or more classes using Canvas.  To change or turn off email notifications, visit:\nhttps://mobileqa.instructure.com/profile/communication\n\n\n",
          "type": "Message",
          "read_state": true,
          "context_type": "Course",
          "course_id": 1098050,
          "message_id": null,
          "notification_category": "Due Date",
          "url": "https://mobileqa.instructure.com/courses/1098050/assignments/5152053",
          "html_url": "https://mobileqa.instructure.com/courses/1098050/assignments/5152053"
        },
        {
          "created_at": "2014-07-02T20:59:54Z",
          "updated_at": "2014-07-02T20:59:54Z",
          "id": 97859114,
          "title": null,
          "message": null,
          "type": "Conversation",
          "read_state": true,
          "context_type": "Course",
          "course_id": 1098050,
          "conversation_id": 3058273,
          "private": false,
          "participant_count": 2,
          "html_url": "https://mobileqa.instructure.com/conversations/3058273"
        },
        {
          "created_at": "2014-06-30T16:10:07Z",
          "updated_at": "2014-06-30T16:10:07Z",
          "id": 97736706,
          "title": "Assignment Created - Media Submission 2, IOS Topdown 4 (Old Data)",
          "message": "          \nA new assignment has been created for your course, IOS Topdown 4 (Old Data)\n\nMedia Submission 2\n\n  due: No Due Date\n\nClick here to view the assignment: \nhttps://mobileqa.instructure.com/courses/1098050/assignments/5111404\n\n\n\n\n\n\n          ________________________________________\n\n          You received this email because you are participating in one or more classes using Canvas.  To change or turn off email notifications, visit:\nhttps://mobileqa.instructure.com/profile/communication\n\n\n",
          "type": "Message",
          "read_state": true,
          "context_type": "Course",
          "course_id": 1098050,
          "message_id": null,
          "notification_category": "Due Date",
          "url": "https://mobileqa.instructure.com/courses/1098050/assignments/5111404",
          "html_url": "https://mobileqa.instructure.com/courses/1098050/assignments/5111404"
        },
        {
          "created_at": "2014-06-30T16:04:05Z",
          "updated_at": "2014-06-30T16:04:11Z",
          "id": 97736153,
          "title": "Media Submission 1",
          "message": "<p>testing media submission 1</p>",
          "type": "DiscussionTopic",
          "read_state": true,
          "context_type": "Course",
          "course_id": 1098050,
          "discussion_topic_id": 5844224,
          "html_url": "https://mobileqa.instructure.com/courses/1098050/discussion_topics/5844224",
          "total_root_discussion_entries": 0,
          "require_initial_post": false,
          "user_has_posted": null,
          "root_discussion_entries": []
        },
        {
          "created_at": "2014-06-23T19:19:26Z",
          "updated_at": "2014-06-23T19:19:26Z",
          "id": 97404523,
          "title": "  Test B, Graded, Group Assignment, Grades by group, Manually Created",
          "message": "<p> Test B, Graded, Group Assignment, Grades by group, Manually Created</p>",
          "type": "DiscussionTopic",
          "read_state": true,
          "context_type": "Group",
          "group_id": 157240,
          "discussion_topic_id": 4974242,
          "html_url": "https://mobileqa.instructure.com/groups/157240/discussion_topics/4974242",
          "total_root_discussion_entries": 4,
          "require_initial_post": null,
          "user_has_posted": null,
          "root_discussion_entries": [
            {
              "user": {
                "user_id": 3558540,
                "user_name": "S3First S3Last(5C)"
              },
              "message": "Sssss3"
            },
            {
              "user": {
                "user_id": 3564934,
                "user_name": "S5First S5Last(4X)"
              },
              "message": "Ssss5"
            },
            {
              "user": {
                "user_id": 3558540,
                "user_name": "S3First S3Last(5C)"
              },
              "message": "discuss"
            }
          ]
        }
      ]"""

    @Language("JSON")
    private var courseStreamItemJSON = """
      {
        "created_at": "2015-02-23T23:41:16Z",
        "updated_at": "2015-02-23T23:41:16Z",
        "id": 129486849,
        "title": "post a discussion from a ta perspective",
        "message": "hasjdf;lk alksjdfa;k sfal;jdflaksjdflas f;ljaslf kajsfl;ajsf",
        "type": "DiscussionTopic",
        "read_state": false,
        "context_type": "Course",
        "course_id": 836357,
        "discussion_topic_id": 9834412,
        "html_url": "https://mobiledev.instructure.com/courses/836357/discussion_topics/9834412",
        "total_root_discussion_entries": 0,
        "require_initial_post": null,
        "user_has_posted": null,
        "root_discussion_entries": []
      }"""

    @Language("JSON")
    private var courseShardStreamItemJSON = """
      {
        "created_at": "2015-02-23T23:41:16Z",
        "updated_at": "2015-02-23T23:41:16Z",
        "id": 129486849,
        "title": "Assignment Created",
        "message": "hasjdf;lk alksjdfa;k sfal;jdflaksjdflas f;ljaslf kajsfl;ajsf",
        "type": "Message",
        "read_state": false,
        "context_type": "Course",
        "course_id": -1,
        "html_url": "https://mobiledev.instructure.com/courses/83635~1123/assignments/98344~1212"
      }"""

}
