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

import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class TodoUnitTest : Assert() {

    @Test
    fun testToDo() {
        val toDoList: Array<ToDo> = todoJSON.parse()

        Assert.assertNotNull(toDoList)

        for (toDo in toDoList) {
            Assert.assertNotNull(toDo)
            Assert.assertNotNull(toDo.type)
            Assert.assertNotNull(toDo.title)
            Assert.assertNotNull(toDo.htmlUrl)
            Assert.assertNotNull(toDo.ignore)
            Assert.assertNotNull(toDo.ignorePermanently)

            if (toDo.assignment != null) {
                val assignment = toDo.assignment!!
                Assert.assertTrue(assignment.id > 0)
            }
        }
    }

    @Language("JSON")
    private var todoJSON = """
      [
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2014-09-25T17:29:36Z",
            "description": "",
            "due_at": "2015-02-25T17:38:03Z",
            "grade_group_students_individually": true,
            "grading_standard_id": null,
            "grading_type": "percent",
            "group_category_id": 36074,
            "id": 5848306,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 140,
            "position": 14,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-09T17:36:55Z",
            "course_id": 833052,
            "name": "Group Assignment  | Graded Individually",
            "submission_types": [
              "online_upload"
            ],
            "has_submitted_submissions": true,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5848306",
            "has_overrides": false,
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5848306/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5848306/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5848306#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 534100,
            "automatic_peer_reviews": false,
            "created_at": "2014-08-12T22:40:35Z",
            "description": "<p>srtestse</p>",
            "due_at": "2015-02-28T17:09:05Z",
            "grade_group_students_individually": false,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 5375271,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 34,
            "position": 55,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-24T17:09:14Z",
            "course_id": 833052,
            "name": "testests",
            "submission_types": [
              "online_upload",
              "online_url",
              "online_text_entry",
              "media_recording"
            ],
            "has_submitted_submissions": true,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5375271",
            "has_overrides": false,
            "use_rubric_for_grading": true,
            "free_form_criterion_comments": false,
            "rubric": [
              {
                "id": "358778_9097",
                "points": 5,
                "description": "Description 1",
                "long_description": "Long Description for Description 1",
                "ratings": [
                  {
                    "id": "blank",
                    "points": 5,
                    "description": "Full Marks"
                  },
                  {
                    "id": "358778_3928",
                    "points": 3,
                    "description": "this my description"
                  },
                  {
                    "id": "blank_2",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "358778_4153",
                "points": 5,
                "description": "Description 2",
                "long_description": "Longer Description for Description 2",
                "ratings": [
                  {
                    "id": "358778_9598",
                    "points": 5,
                    "description": "Full Marks"
                  },
                  {
                    "id": "358778_4921",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "358778_8990",
                "points": 5,
                "description": "Description 3",
                "long_description": "Longer Description for Description 3",
                "ratings": [
                  {
                    "id": "358778_1227",
                    "points": 5,
                    "description": "Full Marks"
                  },
                  {
                    "id": "358778_2412",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "358778_6696",
                "points": 20,
                "description": "Description of criterion",
                "long_description": "",
                "ratings": [
                  {
                    "id": "358778_1639",
                    "points": 20,
                    "description": "Full Marks"
                  },
                  {
                    "id": "358778_9634",
                    "points": 19,
                    "description": "Rating Description"
                  },
                  {
                    "id": "358778_1304",
                    "points": 18,
                    "description": "Rating Description"
                  },
                  {
                    "id": "358778_326",
                    "points": 7,
                    "description": "hjkghkjgk"
                  }
                ]
              }
            ],
            "rubric_settings": {
              "id": 358778,
              "title": "Rubric Title (1)",
              "points_possible": 35,
              "free_form_criterion_comments": false
            },
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5375271/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5375271/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5375271#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2015-01-09T21:47:10Z",
            "description": "<p>ddd</p>",
            "due_at": "2015-02-28T17:15:30Z",
            "grade_group_students_individually": false,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 6624879,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 0,
            "position": 30,
            "post_to_sis": null,
            "unlock_at": "2015-01-31T07:00:00Z",
            "updated_at": "2015-02-24T17:15:36Z",
            "course_id": 833052,
            "name": "Locked",
            "submission_types": [
              "online_upload",
              "online_url",
              "online_text_entry",
              "media_recording"
            ],
            "has_submitted_submissions": false,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6624879",
            "has_overrides": true,
            "published": true,
            "unpublishable": true,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6624879/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6624879/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6624879#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 534101,
            "automatic_peer_reviews": false,
            "created_at": "2014-03-08T00:10:39Z",
            "description": "<p>Quiz</p>",
            "due_at": "2015-02-28T19:51:18Z",
            "grade_group_students_individually": null,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 4691021,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 2,
            "position": 14,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-23T19:51:37Z",
            "course_id": 833052,
            "name": "This is a Quiz",
            "submission_types": [
              "online_quiz"
            ],
            "has_submitted_submissions": true,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/4691021",
            "has_overrides": false,
            "quiz_id": 1642787,
            "hide_download_submissions_button": true,
            "anonymous_submissions": false,
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_4691021/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_4691021/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/4691021#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2015-02-09T21:14:09Z",
            "description": "<p><a id=\"media_comment_m-5rBaCRyiyvBByuS72NS5XtuXpdioHAku\" class=\" instructure_video_link instructure_file_link\" title=\"12_limits_continuity.mp4\" href=\"https://mobiledev.instructure.com/courses/833052/files/63383591/download?wrap=1\">12_limits_continuity.mp4</a></p>\n<p> </p>\n<p> </p>",
            "due_at": "2015-03-13T16:11:08Z",
            "grade_group_students_individually": null,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 6881563,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 1,
            "position": 32,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-24T17:09:44Z",
            "course_id": 833052,
            "name": "Limits",
            "submission_types": [
              "online_quiz"
            ],
            "has_submitted_submissions": true,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6881563",
            "has_overrides": false,
            "quiz_id": 2421689,
            "hide_download_submissions_button": true,
            "anonymous_submissions": false,
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6881563/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6881563/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6881563#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2015-02-13T21:46:45Z",
            "description": "<p><a href=\"http://www.youtube.com/watch?v=K4NRJoCNHIs\">http://www.youtube.com/watch?v=K4NRJoCNHIs</a></p>\r\n<p><a href=\"http://www.math.utah.edu/lectures/math2210/1PostNotes.pdf\">Pdf link </a></p>\r\n<p><a href=\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\">http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837</a></p>\r\n<p><a id=\"\" title=\"Link assignment\" href=\"https://mobiledev.instructure.com/courses/833052/assignments/6576095\">Link assignment</a></p>\r\n<p><a href=\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\"></a><a href=\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\"></a></p>\r\n<p><video preload=\"none\" class=\"instructure_inline_media_comment\" data-media_comment_id=\"m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW\" data-media_comment_type=\"video\" controls=\"controls\" poster=\"https://mobiledev.instructure.com/media_objects/m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW/thumbnail?height=448&amp;type=3&amp;width=550\" src=\"https://mobiledev.instructure.com/courses/833052/media_download?entryId=m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW&amp;media_type=video&amp;redirect=1\"></video></p>\r\n<p><iframe src=\"https://player.vimeo.com/video/78957784\" width=\"500\" height=\"281\"></iframe></p>\r\n<p> </p>\r\n<p> </p>\r\n<p><iframe src=\"https://www.youtube.com/embed/K4NRJoCNHIs\" width=\"560\" height=\"315\" allowfullscreen=\"\"></iframe></p>",
            "due_at": "2015-03-13T16:14:02Z",
            "grade_group_students_individually": false,
            "grading_standard_id": null,
            "grading_type": "letter_grade",
            "group_category_id": null,
            "id": 6925273,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 6,
            "position": 33,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-24T17:12:40Z",
            "course_id": 833052,
            "name": "eMbEdDeD vIdEoS",
            "submission_types": [
              "online_upload",
              "online_url",
              "online_text_entry",
              "media_recording"
            ],
            "has_submitted_submissions": false,
            "muted": false,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6925273",
            "has_overrides": true,
            "published": true,
            "unpublishable": true,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6925273/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6925273/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6925273#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2014-09-10T14:56:54Z",
            "description": "",
            "due_at": "2015-03-13T16:15:40Z",
            "grade_group_students_individually": false,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 5749710,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 40,
            "position": 18,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-02-24T17:14:17Z",
            "course_id": 833052,
            "name": "FreeForm Comment | Uneditted Rubric",
            "submission_types": [
              "online_upload",
              "online_url",
              "online_text_entry",
              "media_recording"
            ],
            "has_submitted_submissions": true,
            "muted": false,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5749710",
            "has_overrides": false,
            "use_rubric_for_grading": true,
            "free_form_criterion_comments": true,
            "rubric": [
              {
                "id": "386318_7798",
                "points": 5,
                "description": "Description of criterion",
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
                "id": "386318_4881",
                "points": 4,
                "description": "Description of criterion",
                "long_description": "",
                "ratings": [
                  {
                    "id": "386318_3500",
                    "points": 4,
                    "description": "Full Marks"
                  },
                  {
                    "id": "386318_2924",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "386318_5669",
                "points": 15,
                "description": "Description of criterion",
                "long_description": "",
                "ratings": [
                  {
                    "id": "386318_8106",
                    "points": 15,
                    "description": "Full Marks"
                  },
                  {
                    "id": "386318_4664",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "386318_3898",
                "points": 6,
                "description": "Description of criterion",
                "long_description": "",
                "ratings": [
                  {
                    "id": "386318_9273",
                    "points": 6,
                    "description": "Full Marks"
                  },
                  {
                    "id": "386318_5238",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              },
              {
                "id": "386318_4995",
                "points": 10,
                "description": "Description of criterion",
                "long_description": "",
                "ratings": [
                  {
                    "id": "386318_159",
                    "points": 10,
                    "description": "Full Marks"
                  },
                  {
                    "id": "386318_1765",
                    "points": 0,
                    "description": "No Marks"
                  }
                ]
              }
            ],
            "rubric_settings": {
              "id": 386318,
              "title": "Some Rubric (4)",
              "points_possible": 40,
              "free_form_criterion_comments": true
            },
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5749710/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5749710/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5749710#submit"
        },
        {
          "context_type": "Course",
          "course_id": 833052,
          "type": "submitting",
          "assignment": {
            "assignment_group_id": 1681571,
            "automatic_peer_reviews": false,
            "created_at": "2015-03-09T23:06:17Z",
            "description": "",
            "due_at": "2015-03-19T20:24:48Z",
            "grade_group_students_individually": false,
            "grading_standard_id": null,
            "grading_type": "points",
            "group_category_id": null,
            "id": 7050730,
            "lock_at": null,
            "peer_reviews": false,
            "points_possible": 50,
            "position": 1,
            "post_to_sis": null,
            "unlock_at": null,
            "updated_at": "2015-03-11T21:30:56Z",
            "course_id": 833052,
            "name": "Video djd Assignment",
            "submission_types": [
              "online_upload",
              "media_recording"
            ],
            "has_submitted_submissions": true,
            "muted": false,
            "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/7050730",
            "has_overrides": false,
            "published": true,
            "unpublishable": false,
            "locked_for_user": false
          },
          "ignore": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_7050730/submitting?permanent=0",
          "ignore_permanently": "https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_7050730/submitting?permanent=1",
          "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/7050730#submit"
        }
      ]"""
}
