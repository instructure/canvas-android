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
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class StreamUnitTest extends Assert{

    @Test
    public void personalStreamTest(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        StreamItem[] personalStream = gson.fromJson(personalStreamJSON, StreamItem[].class);

        assertNotNull(personalStream);

        StreamItem streamItem = personalStream[0];

        assertNotNull(streamItem);

        assertTrue(streamItem.getId() == 98502910);
        assertTrue(StreamItem.Type.isDiscussionTopic(streamItem));
        assertTrue(streamItem.getCourseId() == 1393179);
    }

    @Test
    public void testStreamItemCourse() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        StreamItem streamItem = gson.fromJson(courseStreamItemJSON, StreamItem.class);

        assertNotNull(streamItem);

        assertNotNull(streamItem.getHtmlUrl());
        assertNotNull(streamItem.getContextType());
        assertNotNull(streamItem.getType());
        assertTrue(streamItem.getId() > 0);
    }

    //personal stream
    //@GET("/users/self/activity_stream")
    //void getUserStream(Callback<StreamItem[]> callback);
    final String personalStreamJSON = "[\n" +
            "{\n" +
            "\"created_at\": \"2014-07-15T21:46:46Z\",\n" +
            "\"updated_at\": \"2014-07-15T21:46:50Z\",\n" +
            "\"id\": 98502910,\n" +
            "\"title\": \"new discussion threaded 1\",\n" +
            "\"message\": \"<p>new discussion threaded 1</p>\",\n" +
            "\"type\": \"DiscussionTopic\",\n" +
            "\"read_state\": false,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1393179,\n" +
            "\"discussion_topic_id\": 5919006,\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/courses/1393179/discussion_topics/5919006\",\n" +
            "\"total_root_discussion_entries\": 0,\n" +
            "\"require_initial_post\": false,\n" +
            "\"user_has_posted\": null,\n" +
            "\"root_discussion_entries\": []\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-15T19:09:45Z\",\n" +
            "\"updated_at\": \"2014-07-15T19:12:00Z\",\n" +
            "\"id\": 98491440,\n" +
            "\"title\": \"yyyy\",\n" +
            "\"message\": \"<p>yyyy</p>\",\n" +
            "\"type\": \"DiscussionTopic\",\n" +
            "\"read_state\": false,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1393179,\n" +
            "\"discussion_topic_id\": 5918712,\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/courses/1393179/discussion_topics/5918712\",\n" +
            "\"total_root_discussion_entries\": 0,\n" +
            "\"require_initial_post\": false,\n" +
            "\"user_has_posted\": null,\n" +
            "\"root_discussion_entries\": []\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-10T17:23:35Z\",\n" +
            "\"updated_at\": \"2014-07-10T17:23:35Z\",\n" +
            "\"id\": 98218837,\n" +
            "\"title\": \"Assignment Created - The world cup write up, IOS Topdown 4 (Old Data)\",\n" +
            "\"message\": \"          \\nA new assignment has been created for your course, IOS Topdown 4 (Old Data)\\n\\nThe world cup write up\\n\\n  due: No Due Date\\n\\nClick here to view the assignment: \\nhttps://mobileqa.instructure.com/courses/1098050/assignments/5152053\\n\\n\\n\\n\\n\\n\\n          ________________________________________\\n\\n          You received this email because you are participating in one or more classes using Canvas.  To change or turn off email notifications, visit:\\nhttps://mobileqa.instructure.com/profile/communication\\n\\n\\n\",\n" +
            "\"type\": \"Message\",\n" +
            "\"read_state\": true,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1098050,\n" +
            "\"message_id\": null,\n" +
            "\"notification_category\": \"Due Date\",\n" +
            "\"url\": \"https://mobileqa.instructure.com/courses/1098050/assignments/5152053\",\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/courses/1098050/assignments/5152053\"\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-07-02T20:59:54Z\",\n" +
            "\"updated_at\": \"2014-07-02T20:59:54Z\",\n" +
            "\"id\": 97859114,\n" +
            "\"title\": null,\n" +
            "\"message\": null,\n" +
            "\"type\": \"Conversation\",\n" +
            "\"read_state\": true,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1098050,\n" +
            "\"conversation_id\": 3058273,\n" +
            "\"private\": false,\n" +
            "\"participant_count\": 2,\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/conversations/3058273\"\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-06-30T16:10:07Z\",\n" +
            "\"updated_at\": \"2014-06-30T16:10:07Z\",\n" +
            "\"id\": 97736706,\n" +
            "\"title\": \"Assignment Created - Media Submission 2, IOS Topdown 4 (Old Data)\",\n" +
            "\"message\": \"          \\nA new assignment has been created for your course, IOS Topdown 4 (Old Data)\\n\\nMedia Submission 2\\n\\n  due: No Due Date\\n\\nClick here to view the assignment: \\nhttps://mobileqa.instructure.com/courses/1098050/assignments/5111404\\n\\n\\n\\n\\n\\n\\n          ________________________________________\\n\\n          You received this email because you are participating in one or more classes using Canvas.  To change or turn off email notifications, visit:\\nhttps://mobileqa.instructure.com/profile/communication\\n\\n\\n\",\n" +
            "\"type\": \"Message\",\n" +
            "\"read_state\": true,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1098050,\n" +
            "\"message_id\": null,\n" +
            "\"notification_category\": \"Due Date\",\n" +
            "\"url\": \"https://mobileqa.instructure.com/courses/1098050/assignments/5111404\",\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/courses/1098050/assignments/5111404\"\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-06-30T16:04:05Z\",\n" +
            "\"updated_at\": \"2014-06-30T16:04:11Z\",\n" +
            "\"id\": 97736153,\n" +
            "\"title\": \"Media Submission 1\",\n" +
            "\"message\": \"<p>testing media submission 1</p>\",\n" +
            "\"type\": \"DiscussionTopic\",\n" +
            "\"read_state\": true,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1098050,\n" +
            "\"discussion_topic_id\": 5844224,\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/courses/1098050/discussion_topics/5844224\",\n" +
            "\"total_root_discussion_entries\": 0,\n" +
            "\"require_initial_post\": false,\n" +
            "\"user_has_posted\": null,\n" +
            "\"root_discussion_entries\": []\n" +
            "},\n" +
            "{\n" +
            "\"created_at\": \"2014-06-23T19:19:26Z\",\n" +
            "\"updated_at\": \"2014-06-23T19:19:26Z\",\n" +
            "\"id\": 97404523,\n" +
            "\"title\": \"  Test B, Graded, Group Assignment, Grades by group, Manually Created\",\n" +
            "\"message\": \"<p> Test B, Graded, Group Assignment, Grades by group, Manually Created</p>\",\n" +
            "\"type\": \"DiscussionTopic\",\n" +
            "\"read_state\": true,\n" +
            "\"context_type\": \"Group\",\n" +
            "\"group_id\": 157240,\n" +
            "\"discussion_topic_id\": 4974242,\n" +
            "\"html_url\": \"https://mobileqa.instructure.com/groups/157240/discussion_topics/4974242\",\n" +
            "\"total_root_discussion_entries\": 4,\n" +
            "\"require_initial_post\": null,\n" +
            "\"user_has_posted\": null,\n" +
            "\"root_discussion_entries\": [\n" +
            "{\n" +
            "\"user\": {\n" +
            "\"user_id\": 3558540,\n" +
            "\"user_name\": \"S3First S3Last(5C)\"\n" +
            "},\n" +
            "\"message\": \"Sssss3\"\n" +
            "},\n" +
            "{\n" +
            "\"user\": {\n" +
            "\"user_id\": 3564934,\n" +
            "\"user_name\": \"S5First S5Last(4X)\"\n" +
            "},\n" +
            "\"message\": \"Ssss5\"\n" +
            "},\n" +
            "{\n" +
            "\"user\": {\n" +
            "\"user_id\": 3558540,\n" +
            "\"user_name\": \"S3First S3Last(5C)\"\n" +
            "},\n" +
            "\"message\": \"discuss\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "]";


    String courseStreamItemJSON =
            "{\n" +
            "\"created_at\": \"2015-02-23T23:41:16Z\",\n" +
            "\"updated_at\": \"2015-02-23T23:41:16Z\",\n" +
            "\"id\": 129486849,\n" +
            "\"title\": \"post a discussion from a ta perspective\",\n" +
            "\"message\": \"hasjdf;lk alksjdfa;k sfal;jdflaksjdflas f;ljaslf kajsfl;ajsf\",\n" +
            "\"type\": \"DiscussionTopic\",\n" +
            "\"read_state\": false,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 836357,\n" +
            "\"discussion_topic_id\": 9834412,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/836357/discussion_topics/9834412\",\n" +
            "\"total_root_discussion_entries\": 0,\n" +
            "\"require_initial_post\": null,\n" +
            "\"user_has_posted\": null,\n" +
            "\"root_discussion_entries\": []\n" +
            "}";

}
