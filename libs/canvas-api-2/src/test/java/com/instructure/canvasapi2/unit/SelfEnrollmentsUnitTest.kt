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

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class SelfEnrollmentsUnitTest : Assert() {

    @Test
    fun test1() {
        val list: Array<Enrollment> = JSON.parse()
        Assert.assertNotNull(list)
        Assert.assertTrue(list.isNotEmpty())

        for (e in list) {
            Assert.assertNotNull(e.type)
            Assert.assertNotNull(e.enrollmentState)
            Assert.assertNotNull(e.role)
            Assert.assertTrue(e.isObserver || e.isStudent || e.isTA || e.isTeacher)
        }
    }

    @Language("JSON")
    private val JSON = """
      [
        {
          "associated_user_id": null,
          "course_id": 1279999,
          "course_section_id": 1486081,
          "created_at": "2014-03-17T16:36:36Z",
          "end_at": null,
          "id": 21917612,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "TeacherEnrollment",
          "updated_at": "2014-06-16T22:48:18Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "TeacherEnrollment",
          "last_activity_at": "2014-07-09T18:49:31Z",
          "total_activity_time": 2042,
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 833052,
          "course_section_id": 889720,
          "created_at": "2014-03-17T16:37:33Z",
          "end_at": null,
          "id": 21917613,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "StudentEnrollment",
          "updated_at": "2014-06-23T19:56:18Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "StudentEnrollment",
          "last_activity_at": "2014-07-09T22:04:48Z",
          "total_activity_time": 1217,
          "grades": {
            "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
            "current_score": null,
            "final_score": 0,
            "current_grade": null,
            "final_grade": null
          },
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 24219,
          "course_section_id": 31105,
          "created_at": "2014-03-17T16:37:54Z",
          "end_at": null,
          "id": 21917614,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "StudentEnrollment",
          "updated_at": "2014-06-23T16:27:55Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "StudentEnrollment",
          "last_activity_at": "2014-07-09T15:35:39Z",
          "total_activity_time": 4689,
          "grades": {
            "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg"
          },
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 36376,
          "course_section_id": 986129,
          "created_at": "2014-03-17T16:38:04Z",
          "end_at": null,
          "id": 21917615,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "StudentEnrollment",
          "updated_at": "2014-06-16T22:48:18Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "StudentEnrollment",
          "last_activity_at": "2014-07-09T15:53:45Z",
          "total_activity_time": 0,
          "grades": {
            "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
            "current_score": null,
            "final_score": 0,
            "current_grade": null,
            "final_grade": null
          },
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 1383420,
          "course_section_id": 1586842,
          "created_at": "2014-06-16T19:21:02Z",
          "end_at": null,
          "id": 23097825,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "TeacherEnrollment",
          "updated_at": "2014-06-16T19:21:13Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "TeacherEnrollment",
          "last_activity_at": "2014-07-14T16:37:01Z",
          "total_activity_time": 4381,
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 1279999,
          "course_section_id": 1486081,
          "created_at": "2014-06-16T22:48:03Z",
          "end_at": null,
          "id": 23100952,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "StudentEnrollment",
          "updated_at": "2014-06-16T22:48:18Z",
          "user_id": 5347622,
          "enrollment_state": "active",
          "role": "StudentEnrollment",
          "last_activity_at": "2014-06-16T22:48:19Z",
          "total_activity_time": 0,
          "grades": {
            "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
            "current_score": null,
            "final_score": null,
            "current_grade": null,
            "final_grade": null
          },
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "user": {
            "id": 5347622,
            "name": "noreply@instructure.com",
            "sortable_name": "noreply@instructure.com",
            "short_name": "noreply@instructure.com",
            "login_id": "noreply@instructure.com"
          }
        }
      ]"""

}
