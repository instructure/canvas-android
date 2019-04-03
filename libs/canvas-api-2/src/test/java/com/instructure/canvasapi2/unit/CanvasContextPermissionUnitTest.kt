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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class CanvasContextPermissionUnitTest : Assert() {

    @Test
    fun testCoursePermissions() {
        val course: Course = coursePermissionJSON.parse()

        Assert.assertNotNull(course)
        Assert.assertNotNull(course.permissions)
        Assert.assertTrue(course.canCreateDiscussion())
        Assert.assertTrue(course.permissions!!.canCreateDiscussionTopic)
        Assert.assertFalse(course.permissions!!.canUpdateAvatar)
        Assert.assertFalse(course.permissions!!.canUpdateName)
    }

    @Test
    fun testUserPermissions() {
        val user: User = userPermissionJSON.parse()

        Assert.assertNotNull(user)
        Assert.assertNotNull(user.permissions)
        Assert.assertFalse(user.permissions!!.canCreateDiscussionTopic)
        Assert.assertTrue(user.canUpdateAvatar())
        Assert.assertTrue(user.canUpdateName())
        Assert.assertTrue(user.permissions!!.canUpdateAvatar)
        Assert.assertTrue(user.permissions!!.canUpdateName)
    }

    @Language("JSON")
    private val coursePermissionJSON = """
      {
        "account_id": 992299,
        "course_code": "Android",
        "default_view": "wiki",
        "enrollment_term_id": 31142,
        "id": 8333052,
        "is_public": false,
        "name": "Android Development",
        "start_at": "2012-10-03T18:08:01Z",
        "end_at": "2015-11-27T15:38:49Z",
        "public_syllabus": false,
        "storage_quota_mb": 500,
        "is_public_to_auth_users": false,
        "hide_final_grades": false,
        "permissions": {
          "create_discussion_topic": true
        },
        "apply_assignment_group_weights": true,
        "calendar": {
          "ics": "https://mobiledev.instructure.com/feeds/calendars/fdsdfs.ics"
        },
        "sis_course_id": null,
        "integration_id": null,
        "enrollments": [
          {
            "type": "teacher",
            "role": "TeacherEnrollment",
            "role_id": 2442,
            "enrollment_state": "active"
          }
        ],
        "workflow_state": "available"
      }"""

    @Language("JSON")
    private val userPermissionJSON = """
      {
        "id": 3432122,
        "name": "Michael Jordan",
        "sortable_name": "Jordan, Michael",
        "short_name": "Michael Jordan",
        "avatar_url": "https://pandadev.instructure.com/images/thumbnails/466822827/abcdefghh",
        "locale": null,
        "permissions": {
          "can_update_name": true,
          "can_update_avatar": true
        }
      }"""
}
