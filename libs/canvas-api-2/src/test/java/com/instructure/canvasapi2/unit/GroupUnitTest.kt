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

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.parse
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test

class GroupUnitTest : Assert() {

    @Test
    fun myGroupsTest() {
        val groupList: Array<Group> = myGroupListJSON.parse()

        Assert.assertNotNull(groupList)

        val group = groupList[0]

        Assert.assertNotNull(group)
        Assert.assertNull(group.description)
        Assert.assertTrue(group.id == 91885L)
        Assert.assertTrue(group.groupCategoryId == 22277L)
        Assert.assertFalse(group.isPublic)
        Assert.assertTrue(group.membersCount == 2)
        Assert.assertTrue(group.courseId == 969287L)
        Assert.assertNull(group.avatarUrl)
    }

    @Test
    fun courseGroupsTest() {
        val groupList: Array<Group> = courseGroupsJSON.parse()

        Assert.assertNotNull(groupList)

        val group = groupList[0]

        Assert.assertNotNull(group)
        Assert.assertNull(group.description)
        Assert.assertTrue(group.groupCategoryId == 41559L)
        Assert.assertTrue(group.id == 175540L)
        Assert.assertFalse(group.isPublic)
        Assert.assertTrue(group.membersCount == 1)
        Assert.assertTrue(group.courseId == 1272783L)
        Assert.assertNull(group.avatarUrl)
        Assert.assertNotNull(group.role)
    }


    /**
     * Self group list
     * @GET("/users/self/groups")
     * void getFirstPageGroups(CanvasCallback<Group[]> callback);
     */
    @Language("JSON")
    private val myGroupListJSON = """
      [
        {
          "description": null,
          "group_category_id": 22277,
          "id": 91885,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "g1",
          "members_count": 2,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 969287,
          "avatar_url": null,
          "role": null,
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 37586,
          "id": 157240,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "Grp 3A, Child 1",
          "members_count": 3,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1284055,
          "avatar_url": null,
          "role": null,
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 37524,
          "id": 156814,
          "is_public": false,
          "join_level": "parent_context_auto_join",
          "max_membership": null,
          "name": "Grp 3A, Student(s1) created",
          "members_count": 7,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1284055,
          "avatar_url": null,
          "role": "student_organized",
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 37522,
          "id": 156809,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "Grp 3A, Student(S2) Created Group",
          "members_count": 5,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1274372,
          "avatar_url": null,
          "role": "student_organized",
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 22278,
          "id": 92115,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "grp1",
          "members_count": 3,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 969287,
          "avatar_url": null,
          "role": null,
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 37456,
          "id": 156198,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "Grp1 1A Child Set1",
          "members_count": 3,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1284055,
          "avatar_url": null,
          "role": null,
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 18825,
          "id": 76314,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "grp2 from ios topdown2 1",
          "members_count": 5,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 930387,
          "avatar_url": null,
          "role": null,
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 18553,
          "id": 75152,
          "is_public": false,
          "join_level": "invitation_only",
          "max_membership": null,
          "name": "IOS topdown2 grp1 1",
          "members_count": 3,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 930387,
          "avatar_url": null,
          "role": null,
          "leader": null
        }
      ]"""


    /**
     * Course Groups
     * @GET("/courses/{courseid}/groups")
     * void getFirstPageGroupsInCourse(@Path("courseid") long courseId, CanvasCallback<Group[]> callback);
     */
    @Language("JSON")
    private val courseGroupsJSON = """
      [
        {
          "description": null,
          "group_category_id": 41559,
          "id": 175540,
          "is_public": false,
          "join_level": "parent_context_auto_join",
          "max_membership": null,
          "name": "HODOR",
          "members_count": 1,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1272783,
          "avatar_url": null,
          "role": "student_organized",
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 41559,
          "id": 175541,
          "is_public": false,
          "join_level": "parent_context_auto_join",
          "max_membership": null,
          "name": "Hodor??",
          "members_count": 1,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1272783,
          "avatar_url": null,
          "role": "student_organized",
          "leader": null
        },
        {
          "description": null,
          "group_category_id": 41559,
          "id": 175539,
          "is_public": false,
          "join_level": "parent_context_auto_join",
          "max_membership": null,
          "name": "Hodorrrrrr",
          "members_count": 1,
          "storage_quota_mb": 50,
          "context_type": "Course",
          "course_id": 1272783,
          "avatar_url": null,
          "role": "student_organized",
          "leader": null
        }
      ]"""
}
