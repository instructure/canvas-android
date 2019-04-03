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
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class ScheduleItemUnitTest extends Assert{

    @Test
    public void testScheduleItemCalendar() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        ScheduleItem[] scheduleItems = gson.fromJson(scheduleItemCalendarJSON, ScheduleItem[].class);

        assertNotNull(scheduleItems);

        for(ScheduleItem scheduleItem : scheduleItems) {
            assertNotNull(scheduleItem);
            assertNotNull(scheduleItem.getStartDate());
            assertNotNull(scheduleItem.getTitle());
            assertNotNull(scheduleItem.getContextType());
            assertNotNull(scheduleItem.getHtmlUrl());
            assertTrue(scheduleItem.getId() > 0);
            assertTrue(scheduleItem.getContextId() > 0);
        }
    }

    @Test
    public void testScheduleItemAssignment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Assignment[] assignments = gson.fromJson(scheduleItemAssignmentJSON, Assignment[].class);

        assertNotNull(assignments);

        for(Assignment assignment : assignments) {
            ScheduleItem scheduleItem = assignment.toScheduleItem();
            assertNotNull(scheduleItem);
            assertTrue(scheduleItem.getId() > 0);
            assertNotNull(scheduleItem.getTitle());
            assertNotNull(scheduleItem.getType());
        }
    }

    //region scheduleItemCalendar

    String scheduleItemCalendarJSON = "[\n" +
            "{\n" +
            "\"all_day\": true,\n" +
            "\"all_day_date\": \"2012-10-17\",\n" +
            "\"created_at\": \"2012-10-06T01:09:52Z\",\n" +
            "\"end_at\": \"2012-10-17T06:00:00Z\",\n" +
            "\"id\": 673956,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2012-10-17T06:00:00Z\",\n" +
            "\"title\": \"No Class\",\n" +
            "\"updated_at\": \"2012-10-06T01:09:52Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/673956\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=673956&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": true,\n" +
            "\"all_day_date\": \"2013-05-01\",\n" +
            "\"created_at\": \"2013-04-30T19:19:18Z\",\n" +
            "\"end_at\": \"2013-05-01T06:00:00Z\",\n" +
            "\"id\": 921108,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-05-01T06:00:00Z\",\n" +
            "\"title\": \"Link to another event\",\n" +
            "\"updated_at\": \"2013-04-30T19:20:09Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p><a href=\\\"https://mobiledev.instructure.com/courses/833052/calendar_events/921098\\\">https://mobiledev.instructure.com/courses/833052/calendar_events/921098</a></p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/921108\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=921108&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2013-04-30T19:10:08Z\",\n" +
            "\"end_at\": \"2013-05-03T00:00:00Z\",\n" +
            "\"id\": 921098,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-05-02T23:00:00Z\",\n" +
            "\"title\": \"Come party with Joshua\",\n" +
            "\"updated_at\": \"2013-04-30T19:12:27Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>We're going to party like it's 1989! Of course, I was only 9 years old then, but that doesn't mean I didn't know how to party.</p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/921098\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=921098&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": true,\n" +
            "\"all_day_date\": \"2013-10-29\",\n" +
            "\"created_at\": \"2013-10-28T22:41:46Z\",\n" +
            "\"end_at\": \"2013-10-29T06:00:00Z\",\n" +
            "\"id\": 1252004,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-10-29T06:00:00Z\",\n" +
            "\"title\": \"Tuesday!\",\n" +
            "\"updated_at\": \"2013-10-28T22:41:46Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1252004\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1252004&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2013-10-04T17:39:19Z\",\n" +
            "\"end_at\": \"2013-11-13T21:00:00Z\",\n" +
            "\"id\": 1215732,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-11-13T19:00:00Z\",\n" +
            "\"title\": \"Kit Kat\",\n" +
            "\"updated_at\": \"2013-11-12T23:41:13Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>Break me off a piece of that Kit Kat bar.</p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1215732\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1215732&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2013-12-13T22:21:48Z\",\n" +
            "\"end_at\": \"2013-12-14T03:00:00Z\",\n" +
            "\"id\": 1708201,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-12-14T02:00:00Z\",\n" +
            "\"title\": \"Party Tonight\",\n" +
            "\"updated_at\": \"2013-12-13T22:21:48Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>Come all and have fun</p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1708201\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1708201&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2013-12-13T22:22:31Z\",\n" +
            "\"end_at\": \"2013-12-14T03:00:00Z\",\n" +
            "\"id\": 1708202,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2013-12-14T02:00:00Z\",\n" +
            "\"title\": \"Party Tonight\",\n" +
            "\"updated_at\": \"2013-12-13T22:22:31Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p>Come all and have fun</p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1708202\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1708202&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-01-15T20:21:21Z\",\n" +
            "\"end_at\": \"2014-01-17T22:00:00Z\",\n" +
            "\"id\": 1761739,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2014-01-17T21:00:00Z\",\n" +
            "\"title\": \"Class time\",\n" +
            "\"updated_at\": \"2014-01-15T20:21:21Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1761739\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1761739&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": false,\n" +
            "\"all_day_date\": null,\n" +
            "\"created_at\": \"2014-02-06T21:14:30Z\",\n" +
            "\"end_at\": \"2014-02-07T23:00:00Z\",\n" +
            "\"id\": 1790273,\n" +
            "\"location_address\": null,\n" +
            "\"location_name\": null,\n" +
            "\"start_at\": \"2014-02-07T21:00:00Z\",\n" +
            "\"title\": \"Test my calendar\",\n" +
            "\"updated_at\": \"2014-02-06T21:14:30Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": null,\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/1790273\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=1790273&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "},\n" +
            "{\n" +
            "\"all_day\": true,\n" +
            "\"all_day_date\": \"2015-02-19\",\n" +
            "\"created_at\": \"2015-02-10T21:03:17Z\",\n" +
            "\"end_at\": \"2015-02-19T07:00:00Z\",\n" +
            "\"id\": 2235263,\n" +
            "\"location_address\": \"\",\n" +
            "\"location_name\": \"\",\n" +
            "\"start_at\": \"2015-02-19T07:00:00Z\",\n" +
            "\"title\": \"This is an event\",\n" +
            "\"updated_at\": \"2015-02-10T21:03:52Z\",\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"description\": \"<p><a id=\\\"\\\" title=\\\"Discussion Index\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/discussion_topics\\\" data-api-endpoint=\\\"https://mobiledev.instructure.com/api/v1/courses/833052/discussion_topics\\\" data-api-returntype=\\\"[Discussion]\\\">Discussion 1</a></p>\",\n" +
            "\"context_code\": \"course_833052\",\n" +
            "\"child_events_count\": 0,\n" +
            "\"parent_event_id\": null,\n" +
            "\"hidden\": false,\n" +
            "\"child_events\": [],\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/calendar_events/2235263\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/calendar?event_id=2235263&include_contexts=course_833052#7b2273686f77223a2267726f75705f636f757273655f383333303532227d\"\n" +
            "}" +
            "]";

    //endregion

    //region scheduleItemAssignment
    String scheduleItemAssignmentJSON = "[\n" +
            "{\n" +
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
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-03-19T20:24:48Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 7050730,\n" +
            "\"attempt\": null,\n" +
            "\"body\": null,\n" +
            "\"grade\": null,\n" +
            "\"grade_matches_current_submission\": null,\n" +
            "\"graded_at\": null,\n" +
            "\"grader_id\": null,\n" +
            "\"id\": 52520237,\n" +
            "\"score\": null,\n" +
            "\"submission_type\": null,\n" +
            "\"submitted_at\": null,\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"unsubmitted\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/7050730/submissions/3360251?preview=1\"\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-07-23T16:21:38Z\",\n" +
            "\"description\": \"<p>Submit assignments with multiple attachments</p>\",\n" +
            "\"due_at\": \"2014-10-29T21:11:05Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"percent\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 5206403,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 200,\n" +
            "\"position\": 2,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-10T20:27:39Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Multiple Attachments\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": true,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5206403\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": false,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"343141_819\",\n" +
            "\"points\": 8,\n" +
            "\"description\": \"Essay was in PDF format\",\n" +
            "\"long_description\": \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus condimentum placerat dignissim. Donec tellus nunc, tincidunt sed nisl sed, suscipit venenatis mi. Sed suscipit urna quis felis feugiat elementum. Aenean feugiat molestie augue et sagittis. Vivamus tincidunt et nibh eu fermentum.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"blank\",\n" +
            "\"points\": 8,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"blank_2\",\n" +
            "\"points\": 4,\n" +
            "\"description\": \"This essay was in PDF but the PDF was really just an image and so it doesn't count  new line\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_2870\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_6862\",\n" +
            "\"points\": 12,\n" +
            "\"description\": \"Good Spelling\",\n" +
            "\"long_description\": \"Aliquam faucibus augue nec justo malesuada, at cursus libero egestas. Nullam iaculis libero volutpat orci fringilla aliquet. Proin pulvinar pulvinar urna a aliquet. Quisque congue ligula felis, eget venenatis nunc posuere vitae. Donec commodo, velit nec mollis facilisis, metus sapien tempor urna, at posuere lorem velit quis mi. Praesent tristique magna a vestibulum adipiscing. Aliquam eu felis eros.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"343141_4445\",\n" +
            "\"points\": 12,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_2255\",\n" +
            "\"points\": 6,\n" +
            "\"description\": \"Okay\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_1930\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_811\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Correct Grammar\",\n" +
            "\"long_description\": \"Etiam nec ornare magna. Morbi hendrerit, nisi quis sodales semper, enim neque fringilla elit, faucibus fermentum enim magna vel ante. Sed convallis diam libero. Mauris molestie aliquet convallis. Sed feugiat magna a eros aliquet molestie.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"343141_8367\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_3758\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Mostly Correct\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_9282\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_9479\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"Suspendisse sed nisl quis nunc ornare fermentum. Quisque facilisis ante ligula, at volutpat quam consectetur vel. Integer dignissim dui turpis, nec lobortis tortor condimentum eget. Proin non sagittis urna. Ut pharetra vitae arcu ut pretium. Nulla vitae scelerisque arcu, quis faucibus eros. Cras ultrices iaculis dui, tempor gravida tortor congue et. Donec varius auctor tellus, nec sagittis arcu dignissim vitae.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"343141_9707\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_6511\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Rating Description\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"343141_8007\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 343141,\n" +
            "\"title\": \"Some Rubric (1)\",\n" +
            "\"points_possible\": 40,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2014-10-29T21:11:05Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 5206403,\n" +
            "\"attempt\": 14,\n" +
            "\"body\": null,\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2015-03-09T21:56:12Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 37518778,\n" +
            "\"submission_type\": \"online_upload\",\n" +
            "\"submitted_at\": \"2015-02-06T18:25:06Z\",\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": true,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5206403/submissions/3360251?preview=1\",\n" +
            "\"attachments\": [\n" +
            "{\n" +
            "\"id\": 64091912,\n" +
            "\"folder_id\": 2891717,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"139353068671_thumb.jpg\",\n" +
            "\"filename\": \"139353068671_thumb.jpg\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/files/64091912/download?download_frd=1&verifier=tMUYFt3dRFmjYswSK1cef6EdLNET8jyzdc29jrdE\",\n" +
            "\"size\": 7961,\n" +
            "\"created_at\": \"2015-02-06T18:25:04Z\",\n" +
            "\"updated_at\": \"2015-02-06T18:25:05Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/64091912/139353068671_thumb_thumb.jpg?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139511&Signature=aOGcUEarHCnoTTv2oDifOVZ%2F0yk%3D\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"preview_url\": null\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2012-10-06T00:23:46Z\",\n" +
            "\"description\": \"<p>Answer all these questions.<a id=\\\"\\\" title=\\\"Quiz List\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/quizzes\\\">Quiz List</a></p>\",\n" +
            "\"due_at\": \"2012-11-01T05:59:00Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 2241845,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 0,\n" +
            "\"position\": 3,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-09T22:28:43Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Extra Credit Quiz\",\n" +
            "\"submission_types\": [\n" +
            "\"online_quiz\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241845\",\n" +
            "\"has_overrides\": false,\n" +
            "\"quiz_id\": 757313,\n" +
            "\"hide_download_submissions_button\": true,\n" +
            "\"anonymous_submissions\": false,\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2012-11-01T05:59:00Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 2241845,\n" +
            "\"attempt\": 1,\n" +
            "\"body\": \"user: 3360251, quiz: 757313, score: 100000, time: 2015-01-02 19:40:08 +0000\",\n" +
            "\"grade\": \"100000\",\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2015-01-02T19:40:08Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 11193438,\n" +
            "\"score\": 100000,\n" +
            "\"submission_type\": \"online_quiz\",\n" +
            "\"submitted_at\": \"2012-11-08T22:04:57Z\",\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": true,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241845/submissions/3360251?preview=1\"\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-07-23T21:29:31Z\",\n" +
            "\"description\": \"<p><a id=\\\"\\\" title=\\\"Empty Page\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/pages/empty-page\\\">Empty Page</a><a id=\\\"\\\" title=\\\"Elevensies\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/pages/elevensies\\\">Elevensies</a>Submit an online URL</p>\",\n" +
            "\"due_at\": \"2015-06-06T05:59:59Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 5208375,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 2,\n" +
            "\"position\": 4,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-11T21:17:29Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Online URL Assignment\",\n" +
            "\"submission_types\": [\n" +
            "\"on_paper\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5208375\",\n" +
            "\"has_overrides\": true,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"343388_2621\",\n" +
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
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 343388,\n" +
            "\"title\": \"Submitted\",\n" +
            "\"points_possible\": 5,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-06-06T05:59:59Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 5208375,\n" +
            "\"attempt\": 1,\n" +
            "\"body\": null,\n" +
            "\"grade\": \"5\",\n" +
            "\"grade_matches_current_submission\": false,\n" +
            "\"graded_at\": \"2015-01-12T22:22:11Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 43438485,\n" +
            "\"score\": 5,\n" +
            "\"submission_type\": \"online_url\",\n" +
            "\"submitted_at\": \"2014-11-24T17:46:35Z\",\n" +
            "\"url\": \"http://www.cnn.com\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"submitted\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5208375/submissions/3360251?preview=1\",\n" +
            "\"attachments\": [\n" +
            "{\n" +
            "\"id\": 60660403,\n" +
            "\"folder_id\": null,\n" +
            "\"content-type\": \"image/png\",\n" +
            "\"display_name\": \"websnappr20141124-15069-6q2fmi.png\",\n" +
            "\"filename\": \"websnappr20141124-15069-6q2fmi.png\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/files/60660403/download?download_frd=1&verifier=jpiM5zBgXZ3FEIOUUplMVT2GPfdChDdNsveGUqI2\",\n" +
            "\"size\": 2406239,\n" +
            "\"created_at\": \"2014-11-24T17:46:43Z\",\n" +
            "\"updated_at\": \"2014-11-24T17:46:43Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/60660403/websnappr20141124-15069-6q2fmi_thumb.png?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139512&Signature=7oWv1Wx25ly2AhD7EivOqLgZ%2FD8%3D\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"preview_url\": null\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2012-10-06T01:01:27Z\",\n" +
            "\"description\": \"<ol>\\n<li>List a favorite app</li>\\n<li>Tell what you think the creator did well</li>\\n<li>Tell what they could improve.</li>\\n</ol>\",\n" +
            "\"due_at\": \"2015-06-27T05:59:00Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 2241860,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 5,\n" +
            "\"position\": 5,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-13T23:49:17Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"App Discussion\",\n" +
            "\"submission_types\": [\n" +
            "\"discussion_topic\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241860\",\n" +
            "\"has_overrides\": false,\n" +
            "\"discussion_topic\": {\n" +
            "\"assignment_id\": 2241860,\n" +
            "\"delayed_post_at\": null,\n" +
            "\"discussion_type\": \"side_comment\",\n" +
            "\"id\": 1370025,\n" +
            "\"last_reply_at\": \"2015-03-13T22:21:12Z\",\n" +
            "\"lock_at\": null,\n" +
            "\"podcast_has_student_posts\": null,\n" +
            "\"position\": 2,\n" +
            "\"posted_at\": \"2012-10-06T01:01:27Z\",\n" +
            "\"root_topic_id\": null,\n" +
            "\"title\": \"App Discussion\",\n" +
            "\"user_name\": null,\n" +
            "\"discussion_subentry_count\": 8,\n" +
            "\"permissions\": {\n" +
            "\"attach\": false,\n" +
            "\"update\": false,\n" +
            "\"delete\": false\n" +
            "},\n" +
            "\"message\": \"<ol>\\n<li>List a favorite app</li>\\n<li>Tell what you think the creator did well</li>\\n<li>Tell what they could improve.</li>\\n</ol>\",\n" +
            "\"require_initial_post\": null,\n" +
            "\"user_can_see_posts\": true,\n" +
            "\"podcast_url\": null,\n" +
            "\"read_state\": \"read\",\n" +
            "\"unread_count\": 1,\n" +
            "\"subscribed\": true,\n" +
            "\"topic_children\": [],\n" +
            "\"attachments\": [],\n" +
            "\"published\": true,\n" +
            "\"can_unpublish\": false,\n" +
            "\"locked\": false,\n" +
            "\"can_lock\": false,\n" +
            "\"author\": {},\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/discussion_topics/1370025\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/courses/833052/discussion_topics/1370025\",\n" +
            "\"pinned\": false,\n" +
            "\"group_category_id\": null,\n" +
            "\"can_group\": false,\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-06-27T05:59:00Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 2241860,\n" +
            "\"attempt\": 1,\n" +
            "\"body\": null,\n" +
            "\"grade\": \"7\",\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2014-11-12T20:58:56Z\",\n" +
            "\"grader_id\": 3356518,\n" +
            "\"id\": 10186105,\n" +
            "\"score\": 7,\n" +
            "\"submission_type\": \"discussion_topic\",\n" +
            "\"submitted_at\": \"2012-10-09T01:51:59Z\",\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241860/submissions/3360251?preview=1\",\n" +
            "\"discussion_entries\": [\n" +
            "{\n" +
            "\"created_at\": \"2012-10-09T01:51:59Z\",\n" +
            "\"id\": 3052775,\n" +
            "\"parent_id\": null,\n" +
            "\"updated_at\": \"2012-10-09T01:51:59Z\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"user_name\": \"bla@gmail.com\",\n" +
            "\"message\": \"<p>I like shazam. One big button to do what you want. I wish it started up faster though, sometimes the song is over by the time I find the app and it's finished starting.</p>\",\n" +
            "\"read_state\": \"read\",\n" +
            "\"forced_read_state\": false\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-08T21:07:42Z\",\n" +
            "\"id\": 10485920,\n" +
            "\"parent_id\": null,\n" +
            "\"updated_at\": \"2014-07-08T21:07:42Z\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"user_name\": \"bla@gmail.com\",\n" +
            "\"message\": \"<p>What about the Canvas for Android app?</p>\",\n" +
            "\"read_state\": \"read\",\n" +
            "\"forced_read_state\": false\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-15T20:54:35Z\",\n" +
            "\"id\": 10581899,\n" +
            "\"parent_id\": null,\n" +
            "\"updated_at\": \"2014-07-15T20:54:35Z\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"user_name\": \"bla@gmail.com\",\n" +
            "\"message\": \"<p>And the polling app!</p>\\n<p><a href=\\\"https://help.instructure.com/entries/46160714-Polls-for-Canvas-iOS-Android-1-0-Release-Notes\\\">Here are the release notes</a></p>\\n<p><a title=\\\"Dozen\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/pages/dozen\\\" data-api-endpoint=\\\"https://mobiledev.instructure.com/api/v1/courses/833052/pages/dozen\\\" data-api-returntype=\\\"Page\\\">And here's a link to a page!</a></p>\",\n" +
            "\"read_state\": \"read\",\n" +
            "\"forced_read_state\": false\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-15T21:52:25Z\",\n" +
            "\"id\": 10582439,\n" +
            "\"parent_id\": null,\n" +
            "\"updated_at\": \"2014-07-15T21:52:25Z\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"user_name\": \"bla@gmail.com\",\n" +
            "\"message\": \"<p><a class=\\\" instructure_image_thumbnail instructure_file_link\\\" title=\\\"code-broken.jpg\\\" href=\\\"https://mobiledev.instructure.com/courses/833052/files/39506637/download?wrap=1\\\" data-api-endpoint=\\\"https://mobiledev.instructure.com/api/v1/files/39506637\\\" data-api-returntype=\\\"File\\\">A file link!</a></p>\",\n" +
            "\"read_state\": \"read\",\n" +
            "\"forced_read_state\": false\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-11-05T18:31:13Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": \"2015-01-24T06:59:59Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"gpa_scale\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6193386,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 25,\n" +
            "\"position\": 6,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-09T22:43:44Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Multiple Due Dates Assignment\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6193386\",\n" +
            "\"has_overrides\": true,\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-01-24T06:59:59Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 6193386,\n" +
            "\"attempt\": null,\n" +
            "\"body\": null,\n" +
            "\"grade\": null,\n" +
            "\"grade_matches_current_submission\": null,\n" +
            "\"graded_at\": null,\n" +
            "\"grader_id\": null,\n" +
            "\"id\": 52307539,\n" +
            "\"score\": null,\n" +
            "\"submission_type\": null,\n" +
            "\"submitted_at\": null,\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"unsubmitted\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6193386/submissions/3360251?preview=1\"\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-11-25T18:26:11Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": null,\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"gpa_scale\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6332133,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 25,\n" +
            "\"position\": 7,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2014-11-25T18:26:50Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"GPA Scale | Use Rubric : true\",\n" +
            "\"submission_types\": [\n" +
            "\"online_url\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": false,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6332133\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"422949_7331\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"fdsfs1232dsfsfsa\",\n" +
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
            "\"id\": \"422949_6340\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"f2ffds dfadgw3r\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"422949_9109\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_4067\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_1420\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"cve2rfd233\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"422949_6888\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_9960\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_7490\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"vr32rg\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"422949_4714\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_3054\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_4123\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"gfdsxxxxbfx\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"422949_2293\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"422949_5490\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 422949,\n" +
            "\"title\": \"Some Rubric (10)\",\n" +
            "\"points_possible\": 25,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": true,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 6332133,\n" +
            "\"attempt\": null,\n" +
            "\"body\": null,\n" +
            "\"grade\": null,\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2015-01-08T20:33:42Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 47972648,\n" +
            "\"score\": null,\n" +
            "\"submission_type\": null,\n" +
            "\"submitted_at\": null,\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6332133/submissions/3360251?preview=1\"\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-07-30T20:21:13Z\",\n" +
            "\"description\": \"<p>Same students, different submissions.</p>\",\n" +
            "\"due_at\": \"2015-05-18T18:04:48Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"gpa_scale\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 5261722,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 40,\n" +
            "\"position\": 8,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-03-09T22:12:17Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"SubmissionTypes Assignment\",\n" +
            "\"submission_types\": [\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5261722\",\n" +
            "\"has_overrides\": false,\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-05-18T18:04:48Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 5261722,\n" +
            "\"attempt\": 15,\n" +
            "\"body\": null,\n" +
            "\"grade\": \"B\",\n" +
            "\"grade_matches_current_submission\": false,\n" +
            "\"graded_at\": \"2015-01-09T21:45:13Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 41802655,\n" +
            "\"score\": 34,\n" +
            "\"submission_type\": \"media_recording\",\n" +
            "\"submitted_at\": \"2014-12-02T18:55:55Z\",\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"submitted\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/5261722/submissions/3360251?preview=1\",\n" +
            "\"media_comment\": {\n" +
            "\"content-type\": \"video/mp4\",\n" +
            "\"display_name\": null,\n" +
            "\"media_id\": \"m-54DhcWnQcNc8m5tCLocxZT39jRNHskvX\",\n" +
            "\"media_type\": \"video\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-54DhcWnQcNc8m5tCLocxZT39jRNHskvX&redirect=1&type=mp4\"\n" +
            "}\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2014-04-22T22:18:19Z\",\n" +
            "\"due_at\": \"2014-11-17T06:59:00Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"points\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 4840751,\n" +
            "\"lock_at\": \"2015-03-14T05:59:59Z\",\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 43,\n" +
            "\"position\": 9,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": \"2014-04-21T06:00:00Z\",\n" +
            "\"updated_at\": \"2015-03-12T19:48:40Z\",\n" +
            "\"lock_info\": {\n" +
            "\"asset_string\": \"assignment_4840751\",\n" +
            "\"lock_at\": \"2015-03-14T05:59:59Z\"\n" +
            "},\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"Upload any file\",\n" +
            "\"submission_types\": [\n" +
            "\"online_upload\",\n" +
            "\"online_url\",\n" +
            "\"online_text_entry\",\n" +
            "\"media_recording\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": true,\n" +
            "\"description\": null,\n" +
            "\"muted\": true,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/4840751\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"324234_6446\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Well Written Essay\",\n" +
            "\"long_description\": \"This is a long description, that is very long. Because this is a long description.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"blank\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Beautiful Essay\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_7629\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Average Essay\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"blank_2\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"Terrible Essay\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_4825\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"No Mistakes\",\n" +
            "\"long_description\": \"This is a long description, that is very long. Because this is a long description.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"324234_9581\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"1 mistake or less\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_2998\",\n" +
            "\"points\": 4,\n" +
            "\"description\": \"2 mistakes\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_3511\",\n" +
            "\"points\": 3,\n" +
            "\"description\": \"3 mistakes\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_4657\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"More than 3 mistakes\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_5484\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"On Time\",\n" +
            "\"long_description\": \"This is a long description, that is very long. Because this is a long description.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"324234_7407\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"On Time\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_5711\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"Late\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_8781\",\n" +
            "\"points\": 15,\n" +
            "\"description\": \"Essay Format\",\n" +
            "\"long_description\": \"This is a long description, that is very long. Because this is a long description.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"324234_3550\",\n" +
            "\"points\": 15,\n" +
            "\"description\": \"Well Formatted\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_2136\",\n" +
            "\"points\": 8,\n" +
            "\"description\": \"Some mistakes in essay format\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_6516\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"Lots of mistakes\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_2324\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Is a nice person\",\n" +
            "\"long_description\": \"This is a long description, that is very long. Because this is a long description.\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"324234_8895\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"324234_5663\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 324234,\n" +
            "\"title\": \"File Rubric\",\n" +
            "\"points_possible\": 40,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2014-11-17T06:59:00Z\",\n" +
            "\"unlock_at\": \"2014-04-21T06:00:00Z\",\n" +
            "\"lock_at\": \"2015-03-14T05:59:59Z\",\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": false,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 4840751,\n" +
            "\"attempt\": 2,\n" +
            "\"body\": null,\n" +
            "\"grade_matches_current_submission\": false,\n" +
            "\"graded_at\": \"2014-09-03T19:16:48Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 37518822,\n" +
            "\"submission_type\": \"online_upload\",\n" +
            "\"submitted_at\": \"2014-11-25T18:45:44Z\",\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"submitted\",\n" +
            "\"late\": true,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/4840751/submissions/3360251?preview=1\",\n" +
            "\"attachments\": [\n" +
            "{\n" +
            "\"id\": 60760541,\n" +
            "\"folder_id\": 2891717,\n" +
            "\"content-type\": \"image/jpeg\",\n" +
            "\"display_name\": \"1416940985760-2.jpg\",\n" +
            "\"filename\": \"1416940985760.jpg\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/files/60760541/download?download_frd=1&verifier=2vVkyEPvXJpiG87BkpYWtLqv7G30HSJZdhKbSaQh\",\n" +
            "\"size\": 7352,\n" +
            "\"created_at\": \"2014-11-25T18:45:42Z\",\n" +
            "\"updated_at\": \"2014-11-25T18:45:43Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"https://instructure-uploads.s3.amazonaws.com/account_99298/thumbnails/60760541/1416940985760_thumb.jpg?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1427139513&Signature=5CNmo1I0SYxLbFUFwHaD%2BBp954s%3D\",\n" +
            "\"locked_for_user\": false,\n" +
            "\"preview_url\": null\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "\"locked_for_user\": true,\n" +
            "\"lock_explanation\": \"This assignment was locked Mar 13 at 11:59pm.\"\n" +
            "},\n" +
            "{\n" +
            "\"assignment_group_id\": 1681571,\n" +
            "\"automatic_peer_reviews\": false,\n" +
            "\"created_at\": \"2015-01-02T22:12:27Z\",\n" +
            "\"description\": \"\",\n" +
            "\"due_at\": \"2015-02-27T19:02:15Z\",\n" +
            "\"grade_group_students_individually\": false,\n" +
            "\"grading_standard_id\": null,\n" +
            "\"grading_type\": \"not_graded\",\n" +
            "\"group_category_id\": null,\n" +
            "\"id\": 6544286,\n" +
            "\"lock_at\": null,\n" +
            "\"peer_reviews\": false,\n" +
            "\"points_possible\": 0,\n" +
            "\"position\": 10,\n" +
            "\"post_to_sis\": null,\n" +
            "\"unlock_at\": null,\n" +
            "\"updated_at\": \"2015-02-06T19:01:12Z\",\n" +
            "\"course_id\": 833052,\n" +
            "\"name\": \"No Grade\",\n" +
            "\"submission_types\": [\n" +
            "\"none\"\n" +
            "],\n" +
            "\"has_submitted_submissions\": false,\n" +
            "\"muted\": false,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6544286\",\n" +
            "\"has_overrides\": false,\n" +
            "\"use_rubric_for_grading\": true,\n" +
            "\"free_form_criterion_comments\": false,\n" +
            "\"rubric\": [\n" +
            "{\n" +
            "\"id\": \"447144_7222\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description 1\",\n" +
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
            "\"id\": \"447144_872\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"447144_6267\",\n" +
            "\"points\": 5,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"447144_6772\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "],\n" +
            "\"rubric_settings\": {\n" +
            "\"id\": 447144,\n" +
            "\"title\": \"Confuzzled\",\n" +
            "\"points_possible\": 10,\n" +
            "\"free_form_criterion_comments\": false\n" +
            "},\n" +
            "\"all_dates\": [\n" +
            "{\n" +
            "\"due_at\": \"2015-02-27T19:02:15Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"lock_at\": null,\n" +
            "\"base\": true\n" +
            "}\n" +
            "],\n" +
            "\"published\": true,\n" +
            "\"unpublishable\": true,\n" +
            "\"submission\": {\n" +
            "\"assignment_id\": 6544286,\n" +
            "\"attempt\": null,\n" +
            "\"body\": null,\n" +
            "\"grade\": \"10\",\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2015-02-06T19:01:12Z\",\n" +
            "\"grader_id\": 5020852,\n" +
            "\"id\": 47938642,\n" +
            "\"score\": 10,\n" +
            "\"submission_type\": null,\n" +
            "\"submitted_at\": null,\n" +
            "\"url\": null,\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": false,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/6544286/submissions/3360251?preview=1\"\n" +
            "},\n" +
            "\"locked_for_user\": false\n" +
            "}" +
            "]";

    //endregion
}
