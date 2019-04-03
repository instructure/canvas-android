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

import com.google.gson.Gson;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class TodoUnitTest extends Assert {


    @Test
    public void testToDo() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        ToDo[] toDoList= gson.fromJson(todoJSON, ToDo[].class);

        assertNotNull(toDoList);

        for(ToDo toDo : toDoList) {
            assertNotNull(toDo);
            assertNotNull(toDo.getType());
            assertNotNull(toDo.getTitle());
            assertNotNull(toDo.getHtmlUrl());
            assertNotNull(toDo.getIgnore());
            assertNotNull(toDo.getIgnorePermanently());

            if(toDo.getAssignment() != null) {
                Assignment assignment = toDo.getAssignment();
                assertTrue(assignment.getId() > 0);
            }
        }
    }

    //https://mobiledev.instructure.com/api/v1/users/self/todo
    String todoJSON = "[\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-09-25T17:29:36Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": \"2015-02-25T17:38:03Z\",\n" +
            "\"grade_group_students_individually\": true,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"percent\",\n" +
            "\"group_category_id\": 36074,\n" +
            "\"id\": 5848306,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 140,\n" +
            "\"position\": 14,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-09T17:36:55Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Group Assignment  | Graded Individually\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5848306\",\n" +
            "\"has_overrides\": false,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5848306/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5848306/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5848306#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 534100,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-08-12T22:40:35Z\",\n" +
            "\"description\": \"<p>srtestse</p>\",\n" +
            "\"due_at\": \"2015-02-28T17:09:05Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 5375271,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 34,\n" +
            "\"position\": 55,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-24T17:09:14Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"testests\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5375271\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"358778_9097\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description 1\",\n" +
            "\"long_description\": \"Long Description for Description 1\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"blank\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_3928\",\n" +
            "\"points\": 3,\n" +
            "\"description\": \"this my description\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"blank_2\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_4153\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description 2\",\n" +
            "\"long_description\": \"Longer Description for Description 2\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"358778_9598\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_4921\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_8990\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description 3\",\n" +
            "\"long_description\": \"Longer Description for Description 3\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"358778_1227\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_2412\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_6696\",\n" +
            "\"points\": 20,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"358778_1639\",\n" +
            "\"points\": 20,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_9634\",\n" +
            "\"points\": 19,\n" +
            "\"description\": \"Rating Description\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_1304\",\n" +
            "\"points\": 18,\n" +
            "\"description\": \"Rating Description\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"358778_326\",\n" +
            "\"points\": 7,\n" +
            "\"description\": \"hjkghkjgk\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 358778,\n" +
            "\"title\": \"Rubric Title (1)\",\n" +
            "\"points_possible\": 35,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5375271/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5375271/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5375271#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2015-01-09T21:47:10Z\",\n" +
            "\"description\": \"<p>ddd</p>\",\n" +
            "\"due_at\": \"2015-02-28T17:15:30Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6624879,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 0,\n" +
            "\"position\": 30,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": \"2015-01-31T07:00:00Z\",\n" +
            "\"updated_at\": \"2015-02-24T17:15:36Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Locked\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": false,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6624879\",\n" +
            "\"has_overrides\": true,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": true,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6624879/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6624879/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6624879#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 534101,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-03-08T00:10:39Z\",\n" +
            "\"description\": \"<p>Quiz</p>\",\n" +
            "\"due_at\": \"2015-02-28T19:51:18Z\",\n" +
            "\"grade_group_students_individually\": null,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 4691021,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 2,\n" +
            "\"position\": 14,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-23T19:51:37Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"This is a Quiz\",\n" +
            "\"submission_types\": [\n" +
            "\"online_quiz\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/4691021\",\n" +
            "\"has_overrides\": false,\n" +
            "\"quiz_id\": 1642787,\n" +
            "\"hide_download_submissions_button\": true,\n" +
            "\"anonymous_submissions\": false,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_4691021/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_4691021/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/4691021#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2015-02-09T21:14:09Z\",\n" +
            "\"description\": \"<p><a id=\\\"media_comment_m-5rBaCRyiyvBByuS72NS5XtuXpdioHAku\\\" class=\\\" instructure_video_link instructure_file_link\\\" title=\\\"12_limits_continuity.mp4\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/files/63383591/download?wrap=1\\\">12_limits_continuity.mp4</a></p>\\n<p> </p>\\n<p> </p>\",\n" +
            "\"due_at\": \"2015-03-13T16:11:08Z\",\n" +
            "\"grade_group_students_individually\": null,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6881563,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 1,\n" +
            "\"position\": 32,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-24T17:09:44Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Limits\",\n" +
            "\"submission_types\": [\n" +
            "\"online_quiz\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6881563\",\n" +
            "\"has_overrides\": false,\n" +
            "\"quiz_id\": 2421689,\n" +
            "\"hide_download_submissions_button\": true,\n" +
            "\"anonymous_submissions\": false,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6881563/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6881563/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6881563#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2015-02-13T21:46:45Z\",\n" +
            "\"description\": \"<p><a href=\\\"http://www.youtube.com/watch?v=K4NRJoCNHIs\\\">http://www.youtube.com/watch?v=K4NRJoCNHIs</a></p>\\r\\n<p><a href=\\\"http://www.math.utah.edu/lectures/math2210/1PostNotes.pdf\\\">Pdf link </a></p>\\r\\n<p><a href=\\\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\\\">http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837</a></p>\\r\\n<p><a id=\\\"\\\" title=\\\"Link assignment\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/assignments/6576095\\\">Link assignment</a></p>\\r\\n<p><a href=\\\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\\\"></a><a href=\\\"http://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview#comment23094068_15768837\\\"></a></p>\\r\\n<p><video preload=\\\"none\\\" class=\\\"instructure_inline_media_comment\\\" data-media_comment_id=\\\"m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW\\\" data-media_comment_type=\\\"video\\\" controls=\\\"controls\\\" poster=\\\"https://mobiledev.instructure.com/media_objects/m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW/thumbnail?height=448&amp;type=3&amp;width=550\\\" src=\\\"https://mobiledev.instructure.com/courses/833052/media_download?entryId=m-3EZx5SHQGfbJHq1iBV8ZmT4tvMfdkKnW&amp;media_type=video&amp;redirect=1\\\"></video></p>\\r\\n<p><iframe src=\\\"https://player.vimeo.com/video/78957784\\\" width=\\\"500\\\" height=\\\"281\\\"></iframe></p>\\r\\n<p> </p>\\r\\n<p> </p>\\r\\n<p><iframe src=\\\"https://www.youtube.com/embed/K4NRJoCNHIs\\\" width=\\\"560\\\" height=\\\"315\\\" allowfullscreen=\\\"\\\"></iframe></p>\",\n" +
            "\"due_at\": \"2015-03-13T16:14:02Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"letter_grade\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6925273,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 6,\n" +
            "\"position\": 33,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-24T17:12:40Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"eMbEdDeD vIdEoS\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": false,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6925273\",\n" +
            "\"has_overrides\": true,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": true,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6925273/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_6925273/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6925273#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-09-10T14:56:54Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": \"2015-03-13T16:15:40Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 5749710,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 40,\n" +
            "\"position\": 18,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-24T17:14:17Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"FreeForm Comment | Uneditted Rubric\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5749710\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": true,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"386318_7798\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"blank\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"blank_2\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_4881\",\n" +
            "\"points\": 4,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"386318_3500\",\n" +
            "\"points\": 4,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_2924\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_5669\",\n" +
            "\"points\": 15,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"386318_8106\",\n" +
            "\"points\": 15,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_4664\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_3898\",\n" +
            "\"points\": 6,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"386318_9273\",\n" +
            "\"points\": 6,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_5238\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_4995\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"386318_159\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"386318_1765\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 386318,\n" +
            "\"title\": \"Some Rubric (4)\",\n" +
            "\"points_possible\": 40,\n" +
            "\"free_form_criterion_comments\": true\n" +
            "},\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5749710/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_5749710/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5749710#submit\"\n" +
            "},\n" +
            "{\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 833052,\n" +
            "\"type\": \"submitting\",\n" +
            "\"assignment\": {\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2015-03-09T23:06:17Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": \"2015-03-19T20:24:48Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 7050730,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 50,\n" +
            "\"position\": 1,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-11T21:30:56Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Video djd Assignment\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/7050730\",\n" +
            "\"has_overrides\": false,\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"ignore\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_7050730/submitting?permanent=0\",\n" +
            "\"ignore_permanently\": \"https://mobiledev.instructure.com/api/v1/users/self/todo/assignment_7050730/submitting?permanent=1\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/7050730#submit\"\n" +
            "}" +
            "]";
}
