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
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test

class EnrollmentUnitTest : Assert() {

    @Test
    fun courseEnrollmentsTest() {
        val enrollments: Array<Enrollment> = courseEnrollmentsJSON.parse()

        Assert.assertNotNull(enrollments)

        val enrollment = enrollments[0]

        Assert.assertNotNull(enrollment)
        Assert.assertTrue(enrollment.courseId == 1383418L)
        Assert.assertTrue(enrollment.id == 23221371L)
        Assert.assertTrue(enrollment.role?.apiRoleString == "StudentEnrollment")
        Assert.assertTrue(enrollment.currentScore == 191.5)
        Assert.assertTrue(enrollment.userId == 5834817L)

    }

    @Language("JSON")
    private val courseEnrollmentsJSON = """
      [
        {
          "associated_user_id": null,
          "course_id": 1383418,
          "course_section_id": 1586838,
          "created_at": "2014-06-27T16:30:59Z",
          "end_at": null,
          "id": 23221371,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "StudentEnrollment",
          "updated_at": "2014-07-14T16:49:15Z",
          "user_id": 5834817,
          "enrollment_state": "active",
          "role": "StudentEnrollment",
          "last_activity_at": "2014-07-15T21:17:27Z",
          "total_activity_time": 26692,
          "grades": {
            "html_url": "https://mobiledev.instructure.com/courses/1383418/grades/5834817",
            "current_score": 191.5,
            "final_score": 115.5,
            "current_grade": null,
            "final_grade": null
          },
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "https://mobiledev.instructure.com/courses/1383418/users/5834817",
          "user": {
            "id": 5834817,
            "name": "Hackdown Hodor",
            "sortable_name": "Hodor, Hackdown",
            "short_name": "Hackdown Hodor",
            "login_id": "bla@gmail.com"
          }
        },
        {
          "associated_user_id": null,
          "course_id": 1383418,
          "course_section_id": 1586838,
          "created_at": "2014-06-16T19:15:41Z",
          "end_at": null,
          "id": 23097257,
          "limit_privileges_to_course_section": false,
          "root_account_id": 99298,
          "start_at": null,
          "type": "TeacherEnrollment",
          "updated_at": "2014-07-14T16:48:46Z",
          "user_id": 5814789,
          "enrollment_state": "active",
          "role": "TeacherEnrollment",
          "last_activity_at": "2014-07-17T18:36:01Z",
          "total_activity_time": 20396,
          "sis_course_id": null,
          "course_integration_id": null,
          "sis_section_id": null,
          "section_integration_id": null,
          "html_url": "https://mobiledev.instructure.com/courses/1383418/users/5814789",
          "user": {
            "id": 5814789,
            "name": "bla@gmail.com",
            "sortable_name": "bla@gmail.com",
            "short_name": "bla@gmail.com",
            "login_id": "bla@gmail.com"
          }
        }
      ]"""
}
