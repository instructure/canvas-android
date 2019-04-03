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
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class CourseUnitTest : Assert() {

    @Test
    fun testCourse() {
        val course: Course = courseJSON.parse()

        Assert.assertNotNull(course)

        Assert.assertTrue(course.id == 1383418L)
        Assert.assertTrue(course.isApplyAssignmentGroupWeights)
        Assert.assertNotNull(course.name)
        Assert.assertTrue(course.needsGradingCount == 0L)
        Assert.assertFalse(course.hideFinalGrades)
        Assert.assertFalse(course.isPublic)
        Assert.assertTrue(course.canCreateDiscussion())
        Assert.assertTrue(course.term?.id == 3142L)
    }

    @Test
    fun testFavoriteCourses() {
        val favoriteCourses: Array<Course> = favoriteCoursesJSON.parse()
        val course = favoriteCourses[0]
        Assert.assertNotNull(favoriteCourses)

        Assert.assertNotNull(course)

        Assert.assertTrue(course.id == 1383420L)
        Assert.assertFalse(course.isApplyAssignmentGroupWeights)
        Assert.assertNotNull(course.name)
        Assert.assertTrue(course.needsGradingCount == 0L)
        Assert.assertFalse(course.hideFinalGrades)
        Assert.assertTrue(course.isPublic)
        Assert.assertTrue(course.term?.id == 3142L)
    }

    /**
     * vanilla course
     * @GET("/courses/{courseid}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count")
     * void getCourse(@Path("courseid") long courseId, CanvasCallback<Course> callback);
     */
    @Language("JSON")
    private val courseJSON = """
      {
        "account_id": 99299,
        "course_code": "TEE-101",
        "default_view": "feed",
        "id": 1383418,
        "is_public": false,
        "license": "private",
        "name": "Trevor's Emporium Extraordinarium",
        "start_at": "2014-06-16T19:18:59Z",
        "end_at": null,
        "public_syllabus": false,
        "storage_quota_mb": 500,
        "hide_final_grades": false,
        "permissions": {
          "create_discussion_topic": true
        },
        "term": {
          "end_at": null,
          "id": 3142,
          "name": "Default Term",
          "start_at": null,
          "workflow_state": "active",
          "sis_term_id": null
        },
        "apply_assignment_group_weights": true,
        "calendar": {
          "ics": "https://mobiledev.instructure.com/feeds/calendars/course_V80l8TpKunkQOkK2msOaajKOEDDc3oUQkyl9KHoM.ics"
        },
        "sis_course_id": null,
        "integration_id": null,
        "enrollments": [
          {
            "type": "teacher",
            "role": "TeacherEnrollment",
            "enrollment_state": "active"
          }
        ],
        "needs_grading_count": 0,
        "workflow_state": "available"
      }"""

    /**
     * favorite courses
     * @GET("/users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count")
     * void getFavoriteCourses(CanvasCallback<Course[]> callback);
     */
    @Language("JSON")
    private val favoriteCoursesJSON = """
      [
        {
          "account_id": 99299,
          "course_code": "abcdefghijklmnopqrstuvwxyz",
          "default_view": "feed",
          "id": 1383420,
          "is_public": true,
          "license": "public_domain",
          "name": "abcdefghijklmnopqrstuvwxyz",
          "start_at": "2014-06-16T19:21:13Z",
          "end_at": null,
          "public_syllabus": false,
          "storage_quota_mb": 500,
          "term": {
            "end_at": null,
            "id": 3142,
            "name": "Default Term",
            "start_at": null,
            "workflow_state": "active",
            "sis_term_id": null
          },
          "apply_assignment_group_weights": false,
          "calendar": {
            "ics": "https://mobiledev.instructure.com/feeds/calendars/course_t1ldkwlkCgHnQFds7qnA1V6Zw9NQDZ5m8FQZRKe6.ics"
          },
          "sis_course_id": null,
          "integration_id": null,
          "enrollments": [
            {
              "type": "teacher",
              "role": "TeacherEnrollment",
              "enrollment_state": "active"
            }
          ],
          "needs_grading_count": 0,
          "hide_final_grades": false,
          "workflow_state": "available"
        },
        {
          "account_id": 99299,
          "course_code": "Candroid",
          "default_view": "modules",
          "id": 1279999,
          "is_public": false,
          "license": "private",
          "name": "Candroid",
          "start_at": "2014-03-06T07:00:00Z",
          "end_at": null,
          "public_syllabus": false,
          "storage_quota_mb": 500,
          "term": {
            "end_at": null,
            "id": 3142,
            "name": "Default Term",
            "start_at": null,
            "workflow_state": "active",
            "sis_term_id": null
          },
          "apply_assignment_group_weights": false,
          "calendar": {
            "ics": "https://mobiledev.instructure.com/feeds/calendars/course_iQsZnvF73lcKz37GJHOziVS13HB7lcFekFDv8zqI.ics"
          },
          "sis_course_id": null,
          "integration_id": null,
          "enrollments": [
            {
              "type": "teacher",
              "role": "TeacherEnrollment",
              "enrollment_state": "active"
            }
          ],
          "needs_grading_count": 0,
          "hide_final_grades": false,
          "workflow_state": "available"
        },
        {
          "account_id": 99299,
          "course_code": "TEE-101",
          "default_view": "feed",
          "id": 1383418,
          "is_public": false,
          "license": "private",
          "name": "Trevor's Emporium Extraordinarium",
          "start_at": "2014-06-16T19:18:59Z",
          "end_at": null,
          "public_syllabus": false,
          "storage_quota_mb": 500,
          "term": {
            "end_at": null,
            "id": 3142,
            "name": "Default Term",
            "start_at": null,
            "workflow_state": "active",
            "sis_term_id": null
          },
          "apply_assignment_group_weights": true,
          "calendar": {
            "ics": "https://mobiledev.instructure.com/feeds/calendars/course_V80l8TpKunkQOkK2msOaajKOEDDc3oUQkyl9KHoM.ics"
          },
          "sis_course_id": null,
          "integration_id": null,
          "enrollments": [
            {
              "type": "teacher",
              "role": "TeacherEnrollment",
              "enrollment_state": "active"
            }
          ],
          "needs_grading_count": 0,
          "hide_final_grades": false,
          "workflow_state": "available"
        },
        {
          "account_id": 99299,
          "course_code": "Android",
          "default_view": "feed",
          "id": 833052,
          "is_public": false,
          "license": "private",
          "name": "Android Development",
          "start_at": "2012-10-09T06:00:00Z",
          "end_at": "2014-11-22T05:55:00Z",
          "public_syllabus": false,
          "storage_quota_mb": 500,
          "term": {
            "end_at": null,
            "id": 3142,
            "name": "Default Term",
            "start_at": null,
            "workflow_state": "active",
            "sis_term_id": null
          },
          "apply_assignment_group_weights": true,
          "calendar": {
            "ics": "https://mobiledev.instructure.com/feeds/calendars/course_gCcqY1HU2LJCQxiPHWmrQNGnl7QRL3Xig6M237zD.ics"
          },
          "sis_course_id": null,
          "integration_id": null,
          "enrollments": [
            {
              "type": "student",
              "role": "StudentEnrollment",
              "enrollment_state": "active",
              "computed_current_score": null,
              "computed_final_score": null,
              "computed_current_grade": null,
              "computed_final_grade": null
            }
          ],
          "hide_final_grades": false,
          "workflow_state": "available"
        }
      ]"""

}
