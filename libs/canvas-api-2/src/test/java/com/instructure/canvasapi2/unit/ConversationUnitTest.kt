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

import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.util.*

class ConversationUnitTest : Assert() {

    @Test
    fun testConversation() {

        // We have a list json and a detailed json
        val tempConversations: Array<Conversation> = conversationArrayJSON.parse()
        val detailedConversation: Conversation = detailedConversationJSON.parse()

        // Merge the two together
        val conversations = ArrayList(Arrays.asList<Conversation>(*tempConversations))
        conversations.add(detailedConversation)

        Assert.assertNotNull(conversations)
        Assert.assertEquals(6, conversations.size)


        for (conversation in conversations) {
            Assert.assertNotNull(conversation)

            Assert.assertNotNull(conversation.participants)

            for (basicUser in conversation.participants) {
                Assert.assertNotNull(basicUser)
                Assert.assertTrue(basicUser.id > 0)
                Assert.assertNotNull(basicUser.name)
            }

            Assert.assertTrue(conversation.id > 0)
            Assert.assertNotNull(conversation.audience)
            Assert.assertNotNull(conversation.avatarUrl)
            Assert.assertNotNull(conversation.lastMessagePreview)
            Assert.assertNotNull(conversation.lastMessageSent)
            Assert.assertTrue(conversation.messageCount > 0)

            if (conversation.messages != null) {
                for (message in conversation.messages) {
                    testMessage(message)
                }
            }

            Assert.assertFalse(conversation.workflowState == Conversation.WorkflowState.UNKNOWN)
        }
    }

    //Gets tested from testConversation()
    private fun testMessage(message: Message) {
        Assert.assertNotNull(message)

        if (message.attachments != null) {
            for (attachment in message.attachments) {
                Assert.assertNotNull(attachment)
                Assert.assertTrue(attachment.id > 0)
                Assert.assertNotNull(attachment.displayName)
                Assert.assertNotNull(attachment.filename)
                Assert.assertNotNull(attachment.contentType)
                Assert.assertNotNull(attachment.url)
            }
        }

        Assert.assertTrue(message.authorId > 0)
        Assert.assertNotNull(message.body)
        Assert.assertNotNull(message.createdAt)
        Assert.assertTrue(message.id > 0)

        if (message.forwardedMessages != null) {
            for (replyMessage in message.forwardedMessages) {
                testMessage(replyMessage)
            }
        }
    }

    @Language("JSON")
    private var conversationArrayJSON = """
      [
        {
          "id": 1818821,
          "subject": null,
          "workflow_state": "read",
          "last_message": "Duuuude.",
          "last_message_at": "2013-09-12T14:57:52-06:00",
          "last_authored_message": "Duuuude.",
          "last_authored_message_at": "2013-10-01T11:00:18-06:00",
          "message_count": 1,
          "subscribed": true,
          "private": true,
          "starred": false,
          "properties": [
            "last_author"
          ],
          "audience": [
            4301217
          ],
          "audience_contexts": {
            "courses": {
              "24219": [
                "StudentEnrollment"
              ]
            },
            "groups": {}
          },
          "avatar_url": "https://mobiledev.instructure.com/files/39591519/download?download_frd=1\u0026verifier=VbYIATBkVdKDRFuQ9Bj4ma2VH8MtJ9GDGP4awJle",
          "participants": [
            {
              "id": 3363291,
              "name": "Josher"
            },
            {
              "id": 4301217,
              "name": "nlambson+s@instructure.com"
            }
          ],
          "visible": true,
          "context_name": "Beginning iOS Development"
        },
        {
          "id": 1231484,
          "subject": null,
          "workflow_state": "read",
          "last_message": "vgdhfxfgv",
          "last_message_at": "2013-08-16T10:53:01-06:00",
          "last_authored_message": "hahaha",
          "last_authored_message_at": "2013-08-15T12:23:32-06:00",
          "message_count": 6,
          "subscribed": true,
          "private": true,
          "starred": false,
          "properties": [],
          "audience": [
            3572886
          ],
          "audience_contexts": {
            "courses": {
              "836357": [
                "StudentEnrollment"
              ],
              "833052": [
                "StudentEnrollment"
              ],
              "24219": [
                "StudentEnrollment"
              ]
            },
            "groups": {}
          },
          "avatar_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
          "participants": [
            {
              "id": 3572886,
              "name": "Harry Potter"
            },
            {
              "id": 3363291,
              "name": "Josher"
            }
          ],
          "visible": true,
          "context_name": "Beginning iOS Development"
        },
        {
          "id": 1426885,
          "subject": null,
          "workflow_state": "read",
          "last_message": "message as Joshua",
          "last_message_at": "2013-02-15T15:07:31-07:00",
          "last_authored_message": null,
          "last_authored_message_at": null,
          "message_count": 1,
          "subscribed": true,
          "private": true,
          "starred": false,
          "properties": [],
          "audience": [
            2334831
          ],
          "audience_contexts": {
            "courses": {
              "24219": [
                "StudentEnrollment"
              ],
              "36376": [
                "TeacherEnrollment"
              ]
            },
            "groups": {}
          },
          "avatar_url": "https://mobiledev.instructure.com/images/thumbnails/27255835/Efc7gYk0usxM7V3SFf7d8IpJlGcopgK5BXSL0hD0",
          "participants": [
            {
              "id": 2334831,
              "name": "Josh Teach Dutton"
            },
            {
              "id": 3363291,
              "name": "Josher"
            }
          ],
          "visible": true,
          "context_name": "Beginning iOS Development"
        },
        {
          "id": 1354837,
          "subject": null,
          "workflow_state": "read",
          "last_message": "Testing masquerading from Bradys student ",
          "last_message_at": "2013-02-06T13:33:13-07:00",
          "last_authored_message": "To all 3 bradys.",
          "last_authored_message_at": "2013-01-28T15:19:57-07:00",
          "message_count": 2,
          "subscribed": true,
          "private": false,
          "starred": false,
          "properties": [],
          "audience": [
            3360251,
            3356518,
            3360253
          ],
          "audience_contexts": {
            "courses": {
              "833052": []
            },
            "groups": {}
          },
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "participants": [
            {
              "id": 3360251,
              "name": "Brady L"
            },
            {
              "id": 3363291,
              "name": "Josher"
            },
            {
              "id": 3356518,
              "name": "Brady Larson"
            },
            {
              "id": 3360253,
              "name": "brady.larson.development@gmail.com"
            }
          ],
          "visible": true,
          "context_name": "Android Development"
        },
        {
          "id": 1396121,
          "subject": null,
          "workflow_state": "read",
          "last_message": "Test masquerade as Brady ",
          "last_message_at": "2013-02-06T09:56:32-07:00",
          "last_authored_message": null,
          "last_authored_message_at": null,
          "message_count": 1,
          "subscribed": true,
          "private": false,
          "starred": false,
          "properties": [],
          "audience": [
            3360251,
            3356518,
            242527
          ],
          "audience_contexts": {
            "courses": {
              "833052": []
            },
            "groups": {}
          },
          "avatar_url": "https://mobiledev.instructure.com/images/messages/avatar-group-50.png",
          "participants": [
            {
              "id": 3360251,
              "name": "Brady L"
            },
            {
              "id": 3356518,
              "name": "Brady Larson"
            },
            {
              "id": 3363291,
              "name": "Josher"
            },
            {
              "id": 242527,
              "name": "Mark Suman"
            }
          ],
          "visible": true,
          "context_name": "Android Development"
        }
      ]"""

    @Language("JSON")
    private var detailedConversationJSON = """
      {
        "id": 1231484,
        "subject": null,
        "workflow_state": "read",
        "last_message": "vgdhfxfgv",
        "last_message_at": "2013-08-16T16:53:01Z",
        "last_authored_message": "hahaha",
        "last_authored_message_at": "2013-08-15T18:23:32Z",
        "message_count": 6,
        "subscribed": true,
        "private": true,
        "starred": false,
        "properties": [],
        "messages": [
          {
            "author_id": 3572886,
            "body": "vgdhfxfgv",
            "created_at": "2013-08-16T16:53:01Z",
            "generated": false,
            "id": 12598638,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          },
          {
            "author_id": 3363291,
            "body": "hahaha",
            "created_at": "2013-08-15T18:23:32Z",
            "generated": false,
            "id": 12591793,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          },
          {
            "author_id": 3363291,
            "body": "ello potter",
            "created_at": "2013-08-15T18:23:23Z",
            "generated": false,
            "id": 12591792,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          },
          {
            "author_id": 3363291,
            "body": "ello potter",
            "created_at": "2013-08-15T18:21:02Z",
            "generated": false,
            "id": 12591771,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          },
          {
            "author_id": 3363291,
            "body": "ello potter",
            "created_at": "2013-08-15T18:20:58Z",
            "generated": false,
            "id": 12591769,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          },
          {
            "author_id": 3363291,
            "body": "ello potter",
            "created_at": "2013-08-15T18:20:51Z",
            "generated": false,
            "id": 12591767,
            "forwarded_messages": [],
            "attachments": [],
            "media_comment": null,
            "participating_user_ids": [
              3572886,
              3363291
            ]
          }
        ],
        "submissions": [
          {
            "assignment_id": 3568225,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19744263,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/836357/assignments/3568225/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "what?",
                "created_at": "2013-08-06T19:27:30Z",
                "id": 4495398,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/836357/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534104,
              "automatic_peer_reviews": false,
              "description": null,
              "due_at": "2013-07-24T05:59:59Z",
              "grade_group_students_individually": null,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3568225,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 15,
              "position": 19,
              "unlock_at": null,
              "course_id": 836357,
              "name": "Superhero Quiz",
              "submission_types": [
                "online_quiz"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/836357/assignments/3568225",
              "needs_grading_count": 0,
              "quiz_id": 1298486,
              "anonymous_submissions": false,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 405404,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19733183,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/24219/assignments/405404/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "comment",
                "created_at": "2013-08-05T22:47:55Z",
                "id": 4491901,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/24219/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 5724,
              "automatic_peer_reviews": false,
              "description": "",
              "due_at": "2011-12-01T07:00:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 405404,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 90,
              "position": 17,
              "unlock_at": null,
              "course_id": 24219,
              "name": "The Long Rubric",
              "submission_types": [
                "online_upload",
                "online_text_entry",
                "online_url",
                "media_recording"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/24219/assignments/405404",
              "needs_grading_count": 2,
              "use_rubric_for_grading": false,
              "free_form_criterion_comments": false,
              "rubric": [
                {
                  "id": "14816_4761",
                  "points": 5,
                  "description": "This is a really long criterion description. It's so long it will probably be truncated. What are we going to do about it?",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_61",
                      "points": 5,
                      "description": "Something ridiculous you nor your mother can ever hope of accomplishing"
                    },
                    {
                      "id": "14816_9706",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_8636",
                  "points": 5,
                  "description": "Point 2",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_3099",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_9826",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_2093",
                  "points": 5,
                  "description": "Awesome",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_9589",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_7260",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_6089",
                  "points": 5,
                  "description": "Cows",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_4956",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_7882",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_4891",
                  "points": 5,
                  "description": "Pigs",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_6941",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_2252",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_8525",
                  "points": 5,
                  "description": "What's next on this food chain?",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_1051",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_8985",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_2841",
                  "points": 5,
                  "description": "Corn husks",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_6",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_2747",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_3451",
                  "points": 5,
                  "description": "Sunlight",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_9067",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_9334",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_9023",
                  "points": 5,
                  "description": "Oxygen",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_2945",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_9873",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_7267",
                  "points": 5,
                  "description": "Water",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_8686",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_5610",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_4894",
                  "points": 5,
                  "description": "Soil nutrients",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_4121",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_6746",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_8936",
                  "points": 5,
                  "description": "Fertilizer from the Cows",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "blank",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "blank_2",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                },
                {
                  "id": "14816_4132",
                  "points": 5,
                  "description": "It's the Circle of Life",
                  "long_description": "",
                  "ratings": [
                    {
                      "id": "14816_315",
                      "points": 5,
                      "description": "Full Marks"
                    },
                    {
                      "id": "14816_2855",
                      "points": 0,
                      "description": "No Marks"
                    }
                  ]
                }
              ],
              "rubric_settings": {
                "points_possible": 65,
                "free_form_criterion_comments": false
              },
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3562637,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19732990,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/24219/assignments/3562637/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "just a comment",
                "created_at": "2013-08-05T22:27:27Z",
                "id": 4491792,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/24219/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 1030331,
              "automatic_peer_reviews": false,
              "description": "",
              "due_at": null,
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3562637,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 0,
              "position": 5,
              "unlock_at": null,
              "course_id": 24219,
              "name": "Assignment for 27th - Upcoming Event",
              "submission_types": [
                "online_text_entry"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/24219/assignments/3562637",
              "needs_grading_count": 0,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 2978687,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19732860,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/24219/assignments/2978687/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "I'm only commenting, this is not a submission",
                "created_at": "2013-08-05T22:14:39Z",
                "id": 4491749,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/24219/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 1030331,
              "automatic_peer_reviews": false,
              "description": "\u003Ctable border=\"0\"\u003E\u003Ctbody\u003E\n\u003Ctr\u003E\n\u003Ctd\u003ECat\u003C/td\u003E\r\n\u003Ctd\u003E\r\n\u003Cp\u003EMember of the feline family. Four legs, tail, etc.\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003C/tr\u003E\n\u003Ctr\u003E\n\u003Ctd\u003EDog\u003C/td\u003E\r\n\u003Ctd\u003E\r\n\u003Cp\u003EMember of the canine family. Four legs, tail, etc.\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003C/tr\u003E\n\u003Ctr\u003E\n\u003Ctd\u003E\r\n\u003Cp\u003EDolphin\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003Ctd\u003E\r\n\u003Cp\u003ELooks like a fish, but don't be deceived... Mammal.\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003C/tr\u003E\n\u003Ctr\u003E\n\u003Ctd\u003ETurkey\u003C/td\u003E\r\n\u003Ctd\u003E\r\n\u003Cp\u003EEssential part of a club sandwich. Comes in two varieties: wild and Jennie-O.\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003C/tr\u003E\n\u003Ctr\u003E\n\u003Ctd\u003EHuge Manatee\u003C/td\u003E\r\n\u003Ctd\u003E\r\n\u003Cp\u003ECow of the sea.\u003C/p\u003E\r\n\u003C/td\u003E\r\n\u003C/tr\u003E\n\u003C/tbody\u003E\u003C/table\u003E",
              "due_at": null,
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 2978687,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 5,
              "position": 3,
              "unlock_at": null,
              "course_id": 24219,
              "name": "Animals",
              "submission_types": [
                "online_text_entry"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/24219/assignments/2978687",
              "needs_grading_count": 2,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3507913,
            "attempt": 1,
            "body": "Ya I really want to turn this in again",
            "grade": null,
            "grade_matches_current_submission": true,
            "graded_at": null,
            "grader_id": null,
            "id": 19149101,
            "score": null,
            "submission_type": "online_text_entry",
            "submitted_at": "2013-07-16T20:15:49Z",
            "url": null,
            "user_id": 3572886,
            "workflow_state": "submitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/3507913/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "This is a media comment",
                "created_at": "2013-07-15T23:03:20Z",
                "id": 4330040,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "media_comment": {
                  "content-type": "audio/mp4",
                  "display_name": null,
                  "media_id": "0_7vtr4a82",
                  "media_type": "audio",
                  "url": "https://mobiledev.instructure.com/users/3363291/media_download?entryId=0_7vtr4a82\u0026redirect=1\u0026type=mp4"
                },
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              },
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "This is a media comment",
                "created_at": "2013-07-16T20:15:12Z",
                "id": 4338386,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "media_comment": {
                  "content-type": "video/mp4",
                  "display_name": null,
                  "media_id": "0_03bem65b",
                  "media_type": "video",
                  "url": "https://mobiledev.instructure.com/users/3363291/media_download?entryId=0_03bem65b\u0026redirect=1\u0026type=mp4"
                },
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              },
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "only commenting",
                "created_at": "2013-08-05T22:13:18Z",
                "id": 4491741,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534101,
              "automatic_peer_reviews": false,
              "description": "\u003Cp\u003ECheck this out: if you guess what's happening on Friday, I'll give you extra credit!\u003C/p\u003E",
              "due_at": "2013-08-01T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3507913,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 100,
              "position": 3,
              "unlock_at": null,
              "course_id": 833052,
              "name": "Extra Credit",
              "submission_types": [
                "online_text_entry"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/3507913",
              "needs_grading_count": 1,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3563900,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19682081,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/24219/assignments/3563900/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "comment before submission",
                "created_at": "2013-08-02T15:42:44Z",
                "id": 4477823,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/24219/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 5723,
              "automatic_peer_reviews": false,
              "description": "",
              "due_at": "2013-08-02T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3563900,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 0,
              "position": 11,
              "unlock_at": null,
              "course_id": 24219,
              "name": "Assignment due 25th",
              "submission_types": [
                "online_text_entry",
                "online_url"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/24219/assignments/3563900",
              "needs_grading_count": 1,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3488393,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19682069,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/836357/assignments/3488393/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "comment",
                "created_at": "2013-08-02T15:41:47Z",
                "id": 4477815,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/836357/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534104,
              "automatic_peer_reviews": false,
              "description": "\u003Cp\u003E\u003Ca id=\"\" class=\" instructure_image_thumbnail instructure_file_link\" title=\"card.png\" href=\"https://mobiledev.instructure.com/courses/836357/files/29647541/download?verifier=8ZnHW6vNjMNtx54uawEpysf4MMxPAZefJyWV4ibj\"\u003Ecard.png\u003C/a\u003E\u003C/p\u003E",
              "due_at": "2013-07-25T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3488393,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 15,
              "position": 15,
              "unlock_at": null,
              "course_id": 836357,
              "name": "File Link",
              "submission_types": [
                "none"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/836357/assignments/3488393",
              "needs_grading_count": 0,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3562410,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19636322,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/24219/assignments/3562410/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "only a comment",
                "created_at": "2013-07-31T20:02:20Z",
                "id": 4465502,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/24219/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 5723,
              "automatic_peer_reviews": false,
              "description": "\u003Cp\u003EBring your A-game cause objc runtime stuff is trixy.\u003C/p\u003E",
              "due_at": "2013-08-03T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3562410,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 32,
              "position": 5,
              "unlock_at": null,
              "course_id": 24219,
              "name": "objc/runtime.h",
              "submission_types": [
                "online_text_entry",
                "online_url",
                "media_recording",
                "online_upload"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/24219/assignments/3562410",
              "needs_grading_count": 1,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3510750,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19177973,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/3510750/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "Y",
                "created_at": "2013-07-16T17:56:54Z",
                "id": 4337107,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              },
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "P",
                "created_at": "2013-07-16T18:40:31Z",
                "id": 4337469,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534100,
              "automatic_peer_reviews": false,
              "description": "",
              "due_at": "2013-07-20T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3510750,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 17,
              "position": 32,
              "unlock_at": null,
              "course_id": 833052,
              "name": "Android Essay",
              "submission_types": [
                "online_text_entry"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/3510750",
              "needs_grading_count": 0,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 2241845,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19150487,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241845/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "This is a media comment",
                "created_at": "2013-07-15T22:49:45Z",
                "id": 4329936,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "media_comment": {
                  "content-type": "audio/mp4",
                  "display_name": null,
                  "media_id": "0_izwen3h7",
                  "media_type": "audio",
                  "url": "https://mobiledev.instructure.com/users/3363291/media_download?entryId=0_izwen3h7\u0026redirect=1\u0026type=mp4"
                },
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534101,
              "automatic_peer_reviews": false,
              "description": "\u003Cp\u003EAnswer all these questions.\u003Ca id=\"\" title=\"Quiz List\" href=\"https://mobiledev.instructure.com/courses/833052/quizzes\" data-api-endpoint=\"https://mobiledev.instructure.com/api/v1/courses/833052/quizzes\" data-api-returntype=\"[Quiz]\"\u003EQuiz List\u003C/a\u003E\u003C/p\u003E",
              "due_at": "2012-11-01T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 2241845,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 0,
              "position": 1,
              "unlock_at": null,
              "course_id": 833052,
              "name": "Extra Credit 1",
              "submission_types": [
                "online_quiz"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241845",
              "needs_grading_count": 0,
              "quiz_id": 757313,
              "anonymous_submissions": false,
              "locked_for_user": false
            }
          },
          {
            "assignment_id": 3484476,
            "attempt": null,
            "body": null,
            "grade": null,
            "grade_matches_current_submission": null,
            "graded_at": null,
            "grader_id": null,
            "id": 19146335,
            "score": null,
            "submission_type": null,
            "submitted_at": null,
            "url": null,
            "user_id": 3572886,
            "workflow_state": "unsubmitted",
            "late": false,
            "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/3484476/submissions/3572886?preview=1",
            "submission_comments": [
              {
                "author_id": 3572886,
                "author_name": "Harry Potter",
                "comment": "T",
                "created_at": "2013-07-15T21:16:18Z",
                "id": 4328969,
                "avatar_path": "/images/users/3572886-4fdd2ff573",
                "author": {
                  "id": 3572886,
                  "display_name": "Harry Potter",
                  "avatar_image_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
                  "html_url": "https://mobiledev.instructure.com/courses/833052/users/3572886"
                }
              }
            ],
            "assignment": {
              "assignment_group_id": 534100,
              "automatic_peer_reviews": false,
              "description": "\u003Cp\u003E\u003Ca id=\"\" class=\" instructure_scribd_file instructure_file_link\" title=\"Preview.pdf\" href=\"https://mobiledev.instructure.com/courses/833052/files/39496468/download?verifier=UelGSrRrOis4KdsiwUMf64ftCBL2cu1SpLzxrnPb\"\u003EPreview.pdf\u003C/a\u003E\u003C/p\u003E\r\n\u003Cp\u003E\u00a0\u003C/p\u003E\r\n\u003Cp\u003E\u003Ca id=\"\" href=\"http://www.youtube.com/watch?v=55D-ybnYQSs\"\u003ELink\u003C/a\u003E\u003C/p\u003E\r\n\u003Cp\u003E\u00a0\u003C/p\u003E\r\n\u003Cp\u003E\u00a0\u003C/p\u003E\r\n\u003Cp class=\"p1\"\u003E\u003Ciframe src=\"https://www.youtube.com/embed/55D-ybnYQSs\" width=\"560\" height=\"315\"\u003E\u003C/iframe\u003E\u003C/p\u003E",
              "due_at": "2013-07-25T05:59:00Z",
              "grade_group_students_individually": false,
              "grading_standard_id": null,
              "grading_type": "points",
              "group_category_id": null,
              "id": 3484476,
              "lock_at": null,
              "peer_reviews": false,
              "points_possible": 10,
              "position": 31,
              "unlock_at": null,
              "course_id": 833052,
              "name": "File Link",
              "submission_types": [
                "none"
              ],
              "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/3484476",
              "needs_grading_count": 0,
              "locked_for_user": false
            }
          }
        ],
        "audience": [
          3572886
        ],
        "audience_contexts": {
          "courses": {
            "836357": [
              "StudentEnrollment"
            ],
            "833052": [
              "StudentEnrollment"
            ],
            "24219": [
              "StudentEnrollment"
            ]
          },
          "groups": {}
        },
        "avatar_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq",
        "participants": [
          {
            "id": 3572886,
            "name": "Harry Potter",
            "common_courses": {
              "836357": [
                "StudentEnrollment"
              ],
              "24219": [
                "StudentEnrollment"
              ],
              "833052": [
                "StudentEnrollment"
              ],
              "0": [
                "StudentEnrollment",
                "StudentEnrollment"
              ]
            },
            "common_groups": {},
            "avatar_url": "https://mobiledev.instructure.com/images/thumbnails/26917859/klDcnhrClLcP7BbA2VAIlniCnINQbMdc8mQOLNfq"
          },
          {
            "id": 3363291,
            "name": "Josher",
            "common_courses": {},
            "common_groups": {},
            "avatar_url": "https://mobiledev.instructure.com/images/thumbnails/25871866/VCYN4XMwkJjyXJQy2tyKXibUdPnIT4aAmZCPstGP"
          }
        ],
        "visible": true,
        "context_name": "Beginning iOS Development"
      }"""

}
