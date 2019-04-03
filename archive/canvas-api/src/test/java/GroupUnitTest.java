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
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class GroupUnitTest extends Assert{

    @Test
    public void myGroupsTest(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Group[] groupList = gson.fromJson(myGroupListJSON, Group[].class);

        assertNotNull(groupList);

        Group group = groupList[0];

        assertNotNull(group);
        assertNull(group.getDescription());
        assertTrue(group.getId() == 91885);
        assertTrue(group.getGroupCategoryId() == 22277);
        assertFalse(group.isPublic());
        assertTrue(group.getMembersCount() == 2);
        assertTrue(group.getCourseId() == 969287);
        assertNull(group.getAvatarUrl());
        //assertNull(group.getRole());
    }

    @Test
    public void courseGroupsTest(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Group[] groupList = gson.fromJson(courseGroupsJSON, Group[].class);

        assertNotNull(groupList);

        Group group = groupList[0];

        assertNotNull(group);
        assertNull(group.getDescription());
        assertTrue(group.getGroupCategoryId() == 41559);
        assertTrue(group.getId() == 175540);
        assertFalse(group.isPublic());
        assertTrue(group.getMembersCount() == 1);
        assertTrue(group.getCourseId() == 1272783);
        assertNull(group.getAvatarUrl());
        assertNotNull(group.getRole());
    }




    //Self group list
    //@GET("/users/self/groups")
    //void getFirstPageGroups(CanvasCallback<Group[]> callback);
    final String myGroupListJSON = "[\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 22277,\n" +
            "\"id\": 91885,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"g1\",\n" +
            "\"members_count\": 2,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 969287,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 37586,\n" +
            "\"id\": 157240,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Grp 3A, Child 1\",\n" +
            "\"members_count\": 3,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1284055,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 37524,\n" +
            "\"id\": 156814,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"parent_context_auto_join\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Grp 3A, Student(s1) created\",\n" +
            "\"members_count\": 7,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1284055,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": \"student_organized\",\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 37522,\n" +
            "\"id\": 156809,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Grp 3A, Student(S2) Created Group\",\n" +
            "\"members_count\": 5,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1274372,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": \"student_organized\",\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 22278,\n" +
            "\"id\": 92115,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"grp1\",\n" +
            "\"members_count\": 3,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 969287,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 37456,\n" +
            "\"id\": 156198,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Grp1 1A Child Set1\",\n" +
            "\"members_count\": 3,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1284055,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 18825,\n" +
            "\"id\": 76314,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"grp2 from ios topdown2 1\",\n" +
            "\"members_count\": 5,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 930387,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 18553,\n" +
            "\"id\": 75152,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"invitation_only\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"IOS topdown2 grp1 1\",\n" +
            "\"members_count\": 3,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 930387,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": null,\n" +
            "\"leader\": null\n" +
            "}\n" +
            "]";


    //Course Groups
    //@GET("/courses/{courseid}/groups")
    //void getFirstPageGroupsInCourse(@Path("courseid") long courseId, CanvasCallback<Group[]> callback);
    final String courseGroupsJSON = "[\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 41559,\n" +
            "\"id\": 175540,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"parent_context_auto_join\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"HODOR\",\n" +
            "\"members_count\": 1,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1272783,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": \"student_organized\",\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 41559,\n" +
            "\"id\": 175541,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"parent_context_auto_join\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Hodor??\",\n" +
            "\"members_count\": 1,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1272783,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": \"student_organized\",\n" +
            "\"leader\": null\n" +
            "},\n" +
            "{\n" +
            "\"description\": null,\n" +
            "\"group_category_id\": 41559,\n" +
            "\"id\": 175539,\n" +
            "\"is_public\": false,\n" +
            "\"join_level\": \"parent_context_auto_join\",\n" +
            "\"max_membership\": null,\n" +
            "\"name\": \"Hodorrrrrr\",\n" +
            "\"members_count\": 1,\n" +
            "\"storage_quota_mb\": 50,\n" +
            "\"context_type\": \"Course\",\n" +
            "\"course_id\": 1272783,\n" +
            "\"avatar_url\": null,\n" +
            "\"role\": \"student_organized\",\n" +
            "\"leader\": null\n" +
            "}\n" +
            "]";
}
