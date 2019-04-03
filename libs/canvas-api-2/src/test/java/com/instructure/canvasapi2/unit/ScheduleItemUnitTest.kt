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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.parse
import com.instructure.canvasapi2.utils.toScheduleItem
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class ScheduleItemUnitTest : Assert() {

    @Test
    fun testScheduleItemCalendar() {
        val scheduleItems: Array<ScheduleItem> = scheduleItemCalendarJSON.parse()

        Assert.assertNotNull(scheduleItems)

        for (scheduleItem in scheduleItems) {
            Assert.assertNotNull(scheduleItem)
            Assert.assertNotNull(scheduleItem.startAt)
            Assert.assertNotNull(scheduleItem.title)
            Assert.assertNotNull(scheduleItem.contextType)
            Assert.assertNotNull(scheduleItem.htmlUrl)
            Assert.assertTrue(scheduleItem.id > 0)
            Assert.assertTrue(scheduleItem.contextId > 0)
        }
    }

    @Test
    fun testScheduleItemAssignment() {
        val assignments: Array<Assignment> = scheduleItemAssignmentJSON.parse()

        Assert.assertNotNull(assignments)

        for (assignment in assignments) {
            val scheduleItem = assignment.toScheduleItem()
            Assert.assertNotNull(scheduleItem)
            Assert.assertTrue(scheduleItem.id > 0)
            Assert.assertNotNull(scheduleItem.title)
            Assert.assertNotNull(scheduleItem.itemType)
        }
    }

    //region scheduleItemCalendar

    @Language("JSON")
    private val scheduleItemCalendarJSON = """
        [
            {
                "all_day": true,
                "all_day_date": "2012-10-17",
                "created_at": "2012-10-06T01:09:52Z",
                "end_at": "2012-10-17T06:00:00Z",
                "id": 673956,
                "location_address": null,
                "location_name": null,
                "start_at": "2012-10-17T06:00:00Z",
                "title": "No Class",
                "updated_at": "2012-10-06T01:09:52Z",
                "workflow_state": "active",
                "description": null,
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/673956",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=673956&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": true,
                "all_day_date": "2013-05-01",
                "created_at": "2013-04-30T19:19:18Z",
                "end_at": "2013-05-01T06:00:00Z",
                "id": 921108,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-05-01T06:00:00Z",
                "title": "Link to another event",
                "updated_at": "2013-04-30T19:20:09Z",
                "workflow_state": "active",
                "description": "<p><a href=\"https://mobiledev.instructure.com/courses/833052/calendar_events/921098\">https://mobiledev.instructure.com/courses/833052/calendar_events/921098</a></p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/921108",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=921108&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2013-04-30T19:10:08Z",
                "end_at": "2013-05-03T00:00:00Z",
                "id": 921098,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-05-02T23:00:00Z",
                "title": "Come party with Joshua",
                "updated_at": "2013-04-30T19:12:27Z",
                "workflow_state": "active",
                "description": "<p>We're going to party like it's 1989! Of course, I was only 9 years old then, but that doesn't mean I didn't know how to party.</p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/921098",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=921098&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": true,
                "all_day_date": "2013-10-29",
                "created_at": "2013-10-28T22:41:46Z",
                "end_at": "2013-10-29T06:00:00Z",
                "id": 1252004,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-10-29T06:00:00Z",
                "title": "Tuesday!",
                "updated_at": "2013-10-28T22:41:46Z",
                "workflow_state": "active",
                "description": null,
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1252004",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1252004&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2013-10-04T17:39:19Z",
                "end_at": "2013-11-13T21:00:00Z",
                "id": 1215732,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-11-13T19:00:00Z",
                "title": "Kit Kat",
                "updated_at": "2013-11-12T23:41:13Z",
                "workflow_state": "active",
                "description": "<p>Break me off a piece of that Kit Kat bar.</p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1215732",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1215732&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2013-12-13T22:21:48Z",
                "end_at": "2013-12-14T03:00:00Z",
                "id": 1708201,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-12-14T02:00:00Z",
                "title": "Party Tonight",
                "updated_at": "2013-12-13T22:21:48Z",
                "workflow_state": "active",
                "description": "<p>Come all and have fun</p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1708201",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1708201&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2013-12-13T22:22:31Z",
                "end_at": "2013-12-14T03:00:00Z",
                "id": 1708202,
                "location_address": null,
                "location_name": null,
                "start_at": "2013-12-14T02:00:00Z",
                "title": "Party Tonight",
                "updated_at": "2013-12-13T22:22:31Z",
                "workflow_state": "active",
                "description": "<p>Come all and have fun</p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1708202",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1708202&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2014-01-15T20:21:21Z",
                "end_at": "2014-01-17T22:00:00Z",
                "id": 1761739,
                "location_address": null,
                "location_name": null,
                "start_at": "2014-01-17T21:00:00Z",
                "title": "Class time",
                "updated_at": "2014-01-15T20:21:21Z",
                "workflow_state": "active",
                "description": null,
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1761739",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1761739&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": false,
                "all_day_date": null,
                "created_at": "2014-02-06T21:14:30Z",
                "end_at": "2014-02-07T23:00:00Z",
                "id": 1790273,
                "location_address": null,
                "location_name": null,
                "start_at": "2014-02-07T21:00:00Z",
                "title": "Test my calendar",
                "updated_at": "2014-02-06T21:14:30Z",
                "workflow_state": "active",
                "description": null,
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/1790273",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=1790273&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            },
            {
                "all_day": true,
                "all_day_date": "2015-02-19",
                "created_at": "2015-02-10T21:03:17Z",
                "end_at": "2015-02-19T07:00:00Z",
                "id": 2235263,
                "location_address": "",
                "location_name": "",
                "start_at": "2015-02-19T07:00:00Z",
                "title": "This is an event",
                "updated_at": "2015-02-10T21:03:52Z",
                "workflow_state": "active",
                "description": "<p><a id=\"\" title=\"Discussion Index\" href=\"https://mobiledev.instructure.com/courses/833052/discussion_topics\" data-api-endpoint=\"https://mobiledev.instructure.com/api/v1/courses/833052/discussion_topics\" data-api-returntype=\"[Discussion]\">Discussion 1</a></p>",
                "context_code": "course_833052",
                "child_events_count": 0,
                "parent_event_id": null,
                "hidden": false,
                "child_events": [],
                "url": "https://mobiledev.instructure.com/api/v1/calendar_events/2235263",
                "html_url": "https://mobiledev.instructure.com/calendar?event_id=2235263&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d"
            }
        ]
        """

    @Language("JSON")
    private val scheduleItemAssignmentJSON = """
        [
            {
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
                "all_dates": [
                    {
                        "due_at": "2015-03-19T20:24:48Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 7050730,
                    "attempt": null,
                    "body": null,
                    "grade": null,
                    "grade_matches_current_submission": null,
                    "graded_at": null,
                    "grader_id": null,
                    "id": 52520237,
                    "score": null,
                    "submission_type": null,
                    "submitted_at": null,
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "unsubmitted",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/7050730/submissions/3360251?preview=1"
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-07-23T16:21:38Z",
                "description": "<p>Submit assignments with multiple attachments</p>",
                "due_at": "2014-10-29T21:11:05Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "percent",
                "group_category_id": null,
                "id": 5206403,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 200,
                "position": 2,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-10T20:27:39Z",
                "course_id": 833052,
                "name": "Multiple Attachments",
                "submission_types": [
                    "online_upload",
                    "online_url",
                    "online_text_entry",
                    "media_recording"
                ],
                "has_submitted_submissions": true,
                "muted": true,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5206403",
                "has_overrides": false,
                "use_rubric_for_grading": false,
                "free_form_criterion_comments": false,
                "rubric": [
                    {
                        "id": "343141_819",
                        "points": 8,
                        "description": "Essay was in PDF format",
                        "long_description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus condimentum placerat dignissim. Donec tellus nunc, tincidunt sed nisl sed, suscipit venenatis mi. Sed suscipit urna quis felis feugiat elementum. Aenean feugiat molestie augue et sagittis. Vivamus tincidunt et nibh eu fermentum.",
                        "ratings": [
                            {
                                "id": "blank",
                                "points": 8,
                                "description": "Full Marks"
                            },
                            {
                                "id": "blank_2",
                                "points": 4,
                                "description": "This essay was in PDF but the PDF was really just an image and so it doesn't count  new line"
                            },
                            {
                                "id": "343141_2870",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "343141_6862",
                        "points": 12,
                        "description": "Good Spelling",
                        "long_description": "Aliquam faucibus augue nec justo malesuada, at cursus libero egestas. Nullam iaculis libero volutpat orci fringilla aliquet. Proin pulvinar pulvinar urna a aliquet. Quisque congue ligula felis, eget venenatis nunc posuere vitae. Donec commodo, velit nec mollis facilisis, metus sapien tempor urna, at posuere lorem velit quis mi. Praesent tristique magna a vestibulum adipiscing. Aliquam eu felis eros.",
                        "ratings": [
                            {
                                "id": "343141_4445",
                                "points": 12,
                                "description": "Full Marks"
                            },
                            {
                                "id": "343141_2255",
                                "points": 6,
                                "description": "Okay"
                            },
                            {
                                "id": "343141_1930",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "343141_811",
                        "points": 10,
                        "description": "Correct Grammar",
                        "long_description": "Etiam nec ornare magna. Morbi hendrerit, nisi quis sodales semper, enim neque fringilla elit, faucibus fermentum enim magna vel ante. Sed convallis diam libero. Mauris molestie aliquet convallis. Sed feugiat magna a eros aliquet molestie.",
                        "ratings": [
                            {
                                "id": "343141_8367",
                                "points": 10,
                                "description": "Full Marks"
                            },
                            {
                                "id": "343141_3758",
                                "points": 5,
                                "description": "Mostly Correct"
                            },
                            {
                                "id": "343141_9282",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "343141_9479",
                        "points": 10,
                        "description": "Description of criterion",
                        "long_description": "Suspendisse sed nisl quis nunc ornare fermentum. Quisque facilisis ante ligula, at volutpat quam consectetur vel. Integer dignissim dui turpis, nec lobortis tortor condimentum eget. Proin non sagittis urna. Ut pharetra vitae arcu ut pretium. Nulla vitae scelerisque arcu, quis faucibus eros. Cras ultrices iaculis dui, tempor gravida tortor congue et. Donec varius auctor tellus, nec sagittis arcu dignissim vitae.",
                        "ratings": [
                            {
                                "id": "343141_9707",
                                "points": 10,
                                "description": "Full Marks"
                            },
                            {
                                "id": "343141_6511",
                                "points": 5,
                                "description": "Rating Description"
                            },
                            {
                                "id": "343141_8007",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    }
                ],
                "rubric_settings": {
                    "id": 343141,
                    "title": "Some Rubric (1)",
                    "points_possible": 40,
                    "free_form_criterion_comments": false
                },
                "all_dates": [
                    {
                        "due_at": "2014-10-29T21:11:05Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 5206403,
                    "attempt": 14,
                    "body": null,
                    "grade_matches_current_submission": true,
                    "graded_at": "2015-03-09T21:56:12Z",
                    "grader_id": 5020852,
                    "id": 37518778,
                    "submission_type": "online_upload",
                    "submitted_at": "2015-02-06T18:25:06Z",
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "graded",
                    "late": true,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/5206403/submissions/3360251?preview=1",
                    "attachments": [
                        {
                            "id": 64091912,
                            "folder_id": 2891717,
                            "content-type": "image/jpeg",
                            "display_name": "139353068671_thumb.jpg",
                            "filename": "139353068671_thumb.jpg",
                            "url": "https://mobiledev.instructure.com/files/64091912/download?download_frd=1&verifier=tMUYFt3dRFmjYswSK1cef6EdLNET8jyzdc29jrdE",
                            "size": 7961,
                            "created_at": "2015-02-06T18:25:04Z",
                            "updated_at": "2015-02-06T18:25:05Z",
                            "unlock_at": null,
                            "locked": false,
                            "hidden": false,
                            "lock_at": null,
                            "hidden_for_user": false,
                            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/64091912/139353068671_thumb_thumb.jpg?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139511&Signature=aOGcUEarHCnoTTv2oDifOVZ%2F0yk%3D",
                            "locked_for_user": false,
                            "preview_url": null
                        }
                    ]
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2012-10-06T00:23:46Z",
                "description": "<p>Answer all these questions.<a id=\"\" title=\"Quiz List\" href=\"https://mobiledev.instructure.com/courses/833052/quizzes\">Quiz List</a></p>",
                "due_at": "2012-11-01T05:59:00Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "points",
                "group_category_id": null,
                "id": 2241845,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 0,
                "position": 3,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-09T22:28:43Z",
                "course_id": 833052,
                "name": "Extra Credit Quiz",
                "submission_types": [
                    "online_quiz"
                ],
                "has_submitted_submissions": true,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241845",
                "has_overrides": false,
                "quiz_id": 757313,
                "hide_download_submissions_button": true,
                "anonymous_submissions": false,
                "all_dates": [
                    {
                        "due_at": "2012-11-01T05:59:00Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 2241845,
                    "attempt": 1,
                    "body": "user: 3360251, quiz: 757313, score: 100000, time: 2015-01-02 19:40:08 +0000",
                    "grade": "100000",
                    "grade_matches_current_submission": true,
                    "graded_at": "2015-01-02T19:40:08Z",
                    "grader_id": 5020852,
                    "id": 11193438,
                    "score": 100000,
                    "submission_type": "online_quiz",
                    "submitted_at": "2012-11-08T22:04:57Z",
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "graded",
                    "late": true,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241845/submissions/3360251?preview=1"
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-07-23T21:29:31Z",
                "description": "<p><a id=\"\" title=\"Empty Page\" href=\"https://mobiledev.instructure.com/courses/833052/pages/empty-page\">Empty Page</a><a id=\"\" title=\"Elevensies\" href=\"https://mobiledev.instructure.com/courses/833052/pages/elevensies\">Elevensies</a>Submit an online URL</p>",
                "due_at": "2015-06-06T05:59:59Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "points",
                "group_category_id": null,
                "id": 5208375,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 2,
                "position": 4,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-11T21:17:29Z",
                "course_id": 833052,
                "name": "Online URL Assignment",
                "submission_types": [
                    "on_paper"
                ],
                "has_submitted_submissions": true,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5208375",
                "has_overrides": true,
                "use_rubric_for_grading": true,
                "free_form_criterion_comments": false,
                "rubric": [
                    {
                        "id": "343388_2621",
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
                    }
                ],
                "rubric_settings": {
                    "id": 343388,
                    "title": "Submitted",
                    "points_possible": 5,
                    "free_form_criterion_comments": false
                },
                "all_dates": [
                    {
                        "due_at": "2015-06-06T05:59:59Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 5208375,
                    "attempt": 1,
                    "body": null,
                    "grade": "5",
                    "grade_matches_current_submission": false,
                    "graded_at": "2015-01-12T22:22:11Z",
                    "grader_id": 5020852,
                    "id": 43438485,
                    "score": 5,
                    "submission_type": "online_url",
                    "submitted_at": "2014-11-24T17:46:35Z",
                    "url": "http://www.cnn.com",
                    "user_id": 3360251,
                    "workflow_state": "submitted",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/5208375/submissions/3360251?preview=1",
                    "attachments": [
                        {
                            "id": 60660403,
                            "folder_id": null,
                            "content-type": "image/png",
                            "display_name": "websnappr20141124-15069-6q2fmi.png",
                            "filename": "websnappr20141124-15069-6q2fmi.png",
                            "url": "https://mobiledev.instructure.com/files/60660403/download?download_frd=1&verifier=jpiM5zBgXZ3FEIOUUplMVT2GPfdChDdNsveGUqI2",
                            "size": 2406239,
                            "created_at": "2014-11-24T17:46:43Z",
                            "updated_at": "2014-11-24T17:46:43Z",
                            "unlock_at": null,
                            "locked": false,
                            "hidden": false,
                            "lock_at": null,
                            "hidden_for_user": false,
                            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/60660403/websnappr20141124-15069-6q2fmi_thumb.png?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139512&Signature=7oWv1Wx25ly2AhD7EivOqLgZ%2FD8%3D",
                            "locked_for_user": false,
                            "preview_url": null
                        }
                    ]
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2012-10-06T01:01:27Z",
                "description": "<ol><li>List a favorite app</li><li>Tell what you think the creator did well</li><li>Tell what they could improve.</li></ol>",
                "due_at": "2015-06-27T05:59:00Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "points",
                "group_category_id": null,
                "id": 2241860,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 5,
                "position": 5,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-13T23:49:17Z",
                "course_id": 833052,
                "name": "App Discussion",
                "submission_types": [
                    "discussion_topic"
                ],
                "has_submitted_submissions": true,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241860",
                "has_overrides": false,
                "discussion_topic": {
                    "assignment_id": 2241860,
                    "delayed_post_at": null,
                    "discussion_type": "side_comment",
                    "id": 1370025,
                    "last_reply_at": "2015-03-13T22:21:12Z",
                    "lock_at": null,
                    "podcast_has_student_posts": null,
                    "position": 2,
                    "posted_at": "2012-10-06T01:01:27Z",
                    "root_topic_id": null,
                    "title": "App Discussion",
                    "user_name": null,
                    "discussion_subentry_count": 8,
                    "permissions": {
                        "attach": false,
                        "update": false,
                        "delete": false
                    },
                    "message": "<ol><li>List a favorite app</li><li>Tell what you think the creator did well</li><li>Tell what they could improve.</li></ol>",
                    "require_initial_post": null,
                    "user_can_see_posts": true,
                    "podcast_url": null,
                    "read_state": "read",
                    "unread_count": 1,
                    "subscribed": true,
                    "topic_children": [],
                    "attachments": [],
                    "published": true,
                    "can_unpublish": false,
                    "locked": false,
                    "can_lock": false,
                    "author": {},
                    "html_url": "https://mobiledev.instructure.com/courses/833052/discussion_topics/1370025",
                    "url": "https://mobiledev.instructure.com/courses/833052/discussion_topics/1370025",
                    "pinned": false,
                    "group_category_id": null,
                    "can_group": false,
                    "locked_for_user": false
                },
                "all_dates": [
                    {
                        "due_at": "2015-06-27T05:59:00Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 2241860,
                    "attempt": 1,
                    "body": null,
                    "grade": "7",
                    "grade_matches_current_submission": true,
                    "graded_at": "2014-11-12T20:58:56Z",
                    "grader_id": 3356518,
                    "id": 10186105,
                    "score": 7,
                    "submission_type": "discussion_topic",
                    "submitted_at": "2012-10-09T01:51:59Z",
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "graded",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241860/submissions/3360251?preview=1",
                    "discussion_entries": [
                        {
                            "created_at": "2012-10-09T01:51:59Z",
                            "id": 3052775,
                            "parent_id": null,
                            "updated_at": "2012-10-09T01:51:59Z",
                            "user_id": 3360251,
                            "user_name": "bla@gmail.com",
                            "message": "<p>I like shazam. One big button to do what you want. I wish it started up faster though, sometimes the song is over by the time I find the app and it's finished starting.</p>",
                            "read_state": "read",
                            "forced_read_state": false
                        },
                        {
                            "created_at": "2014-07-08T21:07:42Z",
                            "id": 10485920,
                            "parent_id": null,
                            "updated_at": "2014-07-08T21:07:42Z",
                            "user_id": 3360251,
                            "user_name": "bla@gmail.com",
                            "message": "<p>What about the Canvas for Android app?</p>",
                            "read_state": "read",
                            "forced_read_state": false
                        },
                        {
                            "created_at": "2014-07-15T20:54:35Z",
                            "id": 10581899,
                            "parent_id": null,
                            "updated_at": "2014-07-15T20:54:35Z",
                            "user_id": 3360251,
                            "user_name": "bla@gmail.com",
                            "message": "<p>And the polling app!</p><p><a href=\"https://help.instructure.com/entries/46160714-Polls-for-Canvas-iOS-Android-1-0-Release-Notes\">Here are the release notes</a></p><p><a title=\"Dozen\" href=\"https://mobiledev.instructure.com/courses/833052/pages/dozen\" data-api-endpoint=\"https://mobiledev.instructure.com/api/v1/courses/833052/pages/dozen\" data-api-returntype=\"Page\">And here's a link to a page!</a></p>",
                            "read_state": "read",
                            "forced_read_state": false
                        },
                        {
                            "created_at": "2014-07-15T21:52:25Z",
                            "id": 10582439,
                            "parent_id": null,
                            "updated_at": "2014-07-15T21:52:25Z",
                            "user_id": 3360251,
                            "user_name": "bla@gmail.com",
                            "message": "<p><a class=\" instructure_image_thumbnail instructure_file_link\" title=\"code-broken.jpg\" href=\"https://mobiledev.instructure.com/courses/833052/files/39506637/download?wrap=1\" data-api-endpoint=\"https://mobiledev.instructure.com/api/v1/files/39506637\" data-api-returntype=\"File\">A file link!</a></p>",
                            "read_state": "read",
                            "forced_read_state": false
                        }
                    ]
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-11-05T18:31:13Z",
                "description": "",
                "due_at": "2015-01-24T06:59:59Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "gpa_scale",
                "group_category_id": null,
                "id": 6193386,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 25,
                "position": 6,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-09T22:43:44Z",
                "course_id": 833052,
                "name": "Multiple Due Dates Assignment",
                "submission_types": [
                    "online_upload",
                    "online_url",
                    "online_text_entry"
                ],
                "has_submitted_submissions": true,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6193386",
                "has_overrides": true,
                "all_dates": [
                    {
                        "due_at": "2015-01-24T06:59:59Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 6193386,
                    "attempt": null,
                    "body": null,
                    "grade": null,
                    "grade_matches_current_submission": null,
                    "graded_at": null,
                    "grader_id": null,
                    "id": 52307539,
                    "score": null,
                    "submission_type": null,
                    "submitted_at": null,
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "unsubmitted",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/6193386/submissions/3360251?preview=1"
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-11-25T18:26:11Z",
                "description": "",
                "due_at": null,
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "gpa_scale",
                "group_category_id": null,
                "id": 6332133,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 25,
                "position": 7,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2014-11-25T18:26:50Z",
                "course_id": 833052,
                "name": "GPA Scale | Use Rubric : true",
                "submission_types": [
                    "online_url"
                ],
                "has_submitted_submissions": false,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6332133",
                "has_overrides": false,
                "use_rubric_for_grading": true,
                "free_form_criterion_comments": false,
                "rubric": [
                    {
                        "id": "422949_7331",
                        "points": 5,
                        "description": "fdsfs1232dsfsfsa",
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
                        "id": "422949_6340",
                        "points": 5,
                        "description": "f2ffds dfadgw3r",
                        "long_description": "",
                        "ratings": [
                            {
                                "id": "422949_9109",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "422949_4067",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "422949_1420",
                        "points": 5,
                        "description": "cve2rfd233",
                        "long_description": "",
                        "ratings": [
                            {
                                "id": "422949_6888",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "422949_9960",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "422949_7490",
                        "points": 5,
                        "description": "vr32rg",
                        "long_description": "",
                        "ratings": [
                            {
                                "id": "422949_4714",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "422949_3054",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    },
                    {
                        "id": "422949_4123",
                        "points": 5,
                        "description": "gfdsxxxxbfx",
                        "long_description": "",
                        "ratings": [
                            {
                                "id": "422949_2293",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "422949_5490",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    }
                ],
                "rubric_settings": {
                    "id": 422949,
                    "title": "Some Rubric (10)",
                    "points_possible": 25,
                    "free_form_criterion_comments": false
                },
                "all_dates": [
                    {
                        "due_at": null,
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": true,
                "submission": {
                    "assignment_id": 6332133,
                    "attempt": null,
                    "body": null,
                    "grade": null,
                    "grade_matches_current_submission": true,
                    "graded_at": "2015-01-08T20:33:42Z",
                    "grader_id": 5020852,
                    "id": 47972648,
                    "score": null,
                    "submission_type": null,
                    "submitted_at": null,
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "graded",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/6332133/submissions/3360251?preview=1"
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-07-30T20:21:13Z",
                "description": "<p>Same students, different submissions.</p>",
                "due_at": "2015-05-18T18:04:48Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "gpa_scale",
                "group_category_id": null,
                "id": 5261722,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 40,
                "position": 8,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-03-09T22:12:17Z",
                "course_id": 833052,
                "name": "SubmissionTypes Assignment",
                "submission_types": [
                    "online_url",
                    "online_text_entry",
                    "media_recording"
                ],
                "has_submitted_submissions": true,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/5261722",
                "has_overrides": false,
                "all_dates": [
                    {
                        "due_at": "2015-05-18T18:04:48Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 5261722,
                    "attempt": 15,
                    "body": null,
                    "grade": "B",
                    "grade_matches_current_submission": false,
                    "graded_at": "2015-01-09T21:45:13Z",
                    "grader_id": 5020852,
                    "id": 41802655,
                    "score": 34,
                    "submission_type": "media_recording",
                    "submitted_at": "2014-12-02T18:55:55Z",
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "submitted",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/5261722/submissions/3360251?preview=1",
                    "media_comment": {
                        "content-type": "video/mp4",
                        "display_name": null,
                        "media_id": "m-54DhcWnQcNc8m5tCLocxZT39jRNHskvX",
                        "media_type": "video",
                        "url": "https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-54DhcWnQcNc8m5tCLocxZT39jRNHskvX&redirect=1&type=mp4"
                    }
                },
                "locked_for_user": false
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2014-04-22T22:18:19Z",
                "due_at": "2014-11-17T06:59:00Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "points",
                "group_category_id": null,
                "id": 4840751,
                "lock_at": "2015-03-14T05:59:59Z",
                "peer_reviews": false,
                "points_possible": 43,
                "position": 9,
                "post_to_sis": null,
                "unlock_at": "2014-04-21T06:00:00Z",
                "updated_at": "2015-03-12T19:48:40Z",
                "lock_info": {
                    "asset_string": "assignment_4840751",
                    "lock_at": "2015-03-14T05:59:59Z"
                },
                "course_id": 833052,
                "name": "Upload any file",
                "submission_types": [
                    "online_upload",
                    "online_url",
                    "online_text_entry",
                    "media_recording"
                ],
                "has_submitted_submissions": true,
                "description": null,
                "muted": true,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/4840751",
                "has_overrides": false,
                "use_rubric_for_grading": true,
                "free_form_criterion_comments": false,
                "rubric": [
                    {
                        "id": "324234_6446",
                        "points": 10,
                        "description": "Well Written Essay",
                        "long_description": "This is a long description, that is very long. Because this is a long description.",
                        "ratings": [
                            {
                                "id": "blank",
                                "points": 10,
                                "description": "Beautiful Essay"
                            },
                            {
                                "id": "324234_7629",
                                "points": 5,
                                "description": "Average Essay"
                            },
                            {
                                "id": "blank_2",
                                "points": 0,
                                "description": "Terrible Essay"
                            }
                        ]
                    },
                    {
                        "id": "324234_4825",
                        "points": 5,
                        "description": "No Mistakes",
                        "long_description": "This is a long description, that is very long. Because this is a long description.",
                        "ratings": [
                            {
                                "id": "324234_9581",
                                "points": 5,
                                "description": "1 mistake or less"
                            },
                            {
                                "id": "324234_2998",
                                "points": 4,
                                "description": "2 mistakes"
                            },
                            {
                                "id": "324234_3511",
                                "points": 3,
                                "description": "3 mistakes"
                            },
                            {
                                "id": "324234_4657",
                                "points": 0,
                                "description": "More than 3 mistakes"
                            }
                        ]
                    },
                    {
                        "id": "324234_5484",
                        "points": 5,
                        "description": "On Time",
                        "long_description": "This is a long description, that is very long. Because this is a long description.",
                        "ratings": [
                            {
                                "id": "324234_7407",
                                "points": 5,
                                "description": "On Time"
                            },
                            {
                                "id": "324234_5711",
                                "points": 0,
                                "description": "Late"
                            }
                        ]
                    },
                    {
                        "id": "324234_8781",
                        "points": 15,
                        "description": "Essay Format",
                        "long_description": "This is a long description, that is very long. Because this is a long description.",
                        "ratings": [
                            {
                                "id": "324234_3550",
                                "points": 15,
                                "description": "Well Formatted"
                            },
                            {
                                "id": "324234_2136",
                                "points": 8,
                                "description": "Some mistakes in essay format"
                            },
                            {
                                "id": "324234_6516",
                                "points": 0,
                                "description": "Lots of mistakes"
                            }
                        ]
                    },
                    {
                        "id": "324234_2324",
                        "points": 5,
                        "description": "Is a nice person",
                        "long_description": "This is a long description, that is very long. Because this is a long description.",
                        "ratings": [
                            {
                                "id": "324234_8895",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "324234_5663",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    }
                ],
                "rubric_settings": {
                    "id": 324234,
                    "title": "File Rubric",
                    "points_possible": 40,
                    "free_form_criterion_comments": false
                },
                "all_dates": [
                    {
                        "due_at": "2014-11-17T06:59:00Z",
                        "unlock_at": "2014-04-21T06:00:00Z",
                        "lock_at": "2015-03-14T05:59:59Z",
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": false,
                "submission": {
                    "assignment_id": 4840751,
                    "attempt": 2,
                    "body": null,
                    "grade_matches_current_submission": false,
                    "graded_at": "2014-09-03T19:16:48Z",
                    "grader_id": 5020852,
                    "id": 37518822,
                    "submission_type": "online_upload",
                    "submitted_at": "2014-11-25T18:45:44Z",
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "submitted",
                    "late": true,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/4840751/submissions/3360251?preview=1",
                    "attachments": [
                        {
                            "id": 60760541,
                            "folder_id": 2891717,
                            "content-type": "image/jpeg",
                            "display_name": "1416940985760-2.jpg",
                            "filename": "1416940985760.jpg",
                            "url": "https://mobiledev.instructure.com/files/60760541/download?download_frd=1&verifier=2vVkyEPvXJpiG87BkpYWtLqv7G30HSJZdhKbSaQh",
                            "size": 7352,
                            "created_at": "2014-11-25T18:45:42Z",
                            "updated_at": "2014-11-25T18:45:43Z",
                            "unlock_at": null,
                            "locked": false,
                            "hidden": false,
                            "lock_at": null,
                            "hidden_for_user": false,
                            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/60760541/1416940985760_thumb.jpg?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139513&Signature=5CNmo1I0SYxLbFUFwHaD%2BBp954s%3D",
                            "locked_for_user": false,
                            "preview_url": null
                        }
                    ]
                },
                "locked_for_user": true,
                "lock_explanation": "This assignment was locked Mar 13 at 11:59pm."
            },
            {
                "assignment_group_id": 1681571,
                "automatic_peer_reviews": false,
                "created_at": "2015-01-02T22:12:27Z",
                "description": "",
                "due_at": "2015-02-27T19:02:15Z",
                "grade_group_students_individually": false,
                "grading_standard_id": null,
                "grading_type": "not_graded",
                "group_category_id": null,
                "id": 6544286,
                "lock_at": null,
                "peer_reviews": false,
                "points_possible": 0,
                "position": 10,
                "post_to_sis": null,
                "unlock_at": null,
                "updated_at": "2015-02-06T19:01:12Z",
                "course_id": 833052,
                "name": "No Grade",
                "submission_types": [
                    "none"
                ],
                "has_submitted_submissions": false,
                "muted": false,
                "html_url": "https://mobiledev.instructure.com/courses/833052/assignments/6544286",
                "has_overrides": false,
                "use_rubric_for_grading": true,
                "free_form_criterion_comments": false,
                "rubric": [
                    {
                        "id": "447144_7222",
                        "points": 5,
                        "description": "Description 1",
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
                        "id": "447144_872",
                        "points": 5,
                        "description": "Description of criterion",
                        "long_description": "",
                        "ratings": [
                            {
                                "id": "447144_6267",
                                "points": 5,
                                "description": "Full Marks"
                            },
                            {
                                "id": "447144_6772",
                                "points": 0,
                                "description": "No Marks"
                            }
                        ]
                    }
                ],
                "rubric_settings": {
                    "id": 447144,
                    "title": "Confuzzled",
                    "points_possible": 10,
                    "free_form_criterion_comments": false
                },
                "all_dates": [
                    {
                        "due_at": "2015-02-27T19:02:15Z",
                        "unlock_at": null,
                        "lock_at": null,
                        "base": true
                    }
                ],
                "published": true,
                "unpublishable": true,
                "submission": {
                    "assignment_id": 6544286,
                    "attempt": null,
                    "body": null,
                    "grade": "10",
                    "grade_matches_current_submission": true,
                    "graded_at": "2015-02-06T19:01:12Z",
                    "grader_id": 5020852,
                    "id": 47938642,
                    "score": 10,
                    "submission_type": null,
                    "submitted_at": null,
                    "url": null,
                    "user_id": 3360251,
                    "workflow_state": "graded",
                    "late": false,
                    "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/6544286/submissions/3360251?preview=1"
                },
                "locked_for_user": false
            }
        ]
        """
}