
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
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class CourseUnitTest extends Assert{

    @Test
    public void testCourse(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Course course = gson.fromJson(courseJSON, Course.class);

        assertNotNull(course);

        assertTrue(course.getId() == 1383418);
        assertTrue(course.getApplyAssignmentGroupWeights());
        assertNotNull(course.getName());
        assertTrue(course.getNeedsGradingCount() == 0);
        assertFalse(course.isFinalGradeHidden());
        assertFalse(course.isPublic());
        assertTrue(course.canCreateDiscussion());
        assertTrue(course.getTerm().getId() == 3142);
    }

    @Test
    public void testFavoriteCourses(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Course[] favoriteCourses = gson.fromJson(favoriteCoursesJSON, Course[].class);
        Course course = favoriteCourses[0];
        assertNotNull(favoriteCourses);

        assertNotNull(course);

        assertTrue(course.getId() == 1383420);
        assertFalse(course.getApplyAssignmentGroupWeights());
        assertNotNull(course.getName());
        assertTrue(course.getNeedsGradingCount() == 0);
        assertFalse(course.isFinalGradeHidden());
        assertTrue(course.isPublic());
        assertTrue(course.getTerm().getId() == 3142);
    }

    //vanilla course
    //@GET("/courses/{courseid}?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count")
    //void getCourse(@Path("courseid") long courseId, CanvasCallback<Course> callback);
    final String courseJSON = "{\n" +
            "\"account_id\": 99299,\n" +
            "\"course_code\": \"TEE-101\",\n" +
            "\"default_view\": \"feed\",\n" +
            "\"id\": 1383418,\n" +
            "\"is_public\": false,\n" +
            "\"license\": \"private\",\n" +
            "\"name\": \"Trevor's Emporium Extraordinarium\",\n" +
            "\"start_at\": \"2014-06-16T19:18:59Z\",\n" +
            "\"end_at\": null,\n" +
            "\"public_syllabus\": false,\n" +
            "\"storage_quota_mb\": 500,\n" +
            "\"hide_final_grades\": false,\n" +
            "\"permissions\": {\n" +
            "\"create_discussion_topic\": true\n" +
            "},\n" +
            "\"term\": {\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"sis_term_id\": null\n" +
            "},\n" +
            "\"apply_assignment_group_weights\": true,\n" +
            "\"calendar\": {\n" +
            "\"ics\": \"https://mobiledev.instructure.com/feeds/calendars/course_V80l8TpKunkQOkK2msOaajKOEDDc3oUQkyl9KHoM.ics\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"integration_id\": null,\n" +
            "\"enrollments\": [\n" +
            "{\n" +
            "\"type\": \"teacher\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"enrollment_state\": \"active\"\n" +
            "}\n" +
            "],\n" +
            "\"needs_grading_count\": 0,\n" +
            "\"workflow_state\": \"available\"\n" +
            "}";

    //favorite courses
    //@GET("/users/self/favorites/courses?include[]=term&include[]=total_scores&include[]=license&include[]=is_public&include[]=needs_grading_count")
    //void getFavoriteCourses(CanvasCallback<Course[]> callback);
    final String favoriteCoursesJSON = "[\n" +
            "{\n" +
            "\"account_id\": 99299,\n" +
            "\"course_code\": \"abcdefghijklmnopqrstuvwxyz\",\n" +
            "\"default_view\": \"feed\",\n" +
            "\"id\": 1383420,\n" +
            "\"is_public\": true,\n" +
            "\"license\": \"public_domain\",\n" +
            "\"name\": \"abcdefghijklmnopqrstuvwxyz\",\n" +
            "\"start_at\": \"2014-06-16T19:21:13Z\",\n" +
            "\"end_at\": null,\n" +
            "\"public_syllabus\": false,\n" +
            "\"storage_quota_mb\": 500,\n" +
            "\"term\": {\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"sis_term_id\": null\n" +
            "},\n" +
            "\"apply_assignment_group_weights\": false,\n" +
            "\"calendar\": {\n" +
            "\"ics\": \"https://mobiledev.instructure.com/feeds/calendars/course_t1ldkwlkCgHnQFds7qnA1V6Zw9NQDZ5m8FQZRKe6.ics\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"integration_id\": null,\n" +
            "\"enrollments\": [\n" +
            "{\n" +
            "\"type\": \"teacher\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"enrollment_state\": \"active\"\n" +
            "}\n" +
            "],\n" +
            "\"needs_grading_count\": 0,\n" +
            "\"hide_final_grades\": false,\n" +
            "\"workflow_state\": \"available\"\n" +
            "},\n" +
            "{\n" +
            "\"account_id\": 99299,\n" +
            "\"course_code\": \"Candroid\",\n" +
            "\"default_view\": \"modules\",\n" +
            "\"id\": 1279999,\n" +
            "\"is_public\": false,\n" +
            "\"license\": \"private\",\n" +
            "\"name\": \"Candroid\",\n" +
            "\"start_at\": \"2014-03-06T07:00:00Z\",\n" +
            "\"end_at\": null,\n" +
            "\"public_syllabus\": false,\n" +
            "\"storage_quota_mb\": 500,\n" +
            "\"term\": {\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"sis_term_id\": null\n" +
            "},\n" +
            "\"apply_assignment_group_weights\": false,\n" +
            "\"calendar\": {\n" +
            "\"ics\": \"https://mobiledev.instructure.com/feeds/calendars/course_iQsZnvF73lcKz37GJHOziVS13HB7lcFekFDv8zqI.ics\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"integration_id\": null,\n" +
            "\"enrollments\": [\n" +
            "{\n" +
            "\"type\": \"teacher\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"enrollment_state\": \"active\"\n" +
            "}\n" +
            "],\n" +
            "\"needs_grading_count\": 0,\n" +
            "\"hide_final_grades\": false,\n" +
            "\"workflow_state\": \"available\"\n" +
            "},\n" +
            "{\n" +
            "\"account_id\": 99299,\n" +
            "\"course_code\": \"TEE-101\",\n" +
            "\"default_view\": \"feed\",\n" +
            "\"id\": 1383418,\n" +
            "\"is_public\": false,\n" +
            "\"license\": \"private\",\n" +
            "\"name\": \"Trevor's Emporium Extraordinarium\",\n" +
            "\"start_at\": \"2014-06-16T19:18:59Z\",\n" +
            "\"end_at\": null,\n" +
            "\"public_syllabus\": false,\n" +
            "\"storage_quota_mb\": 500,\n" +
            "\"term\": {\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"sis_term_id\": null\n" +
            "},\n" +
            "\"apply_assignment_group_weights\": true,\n" +
            "\"calendar\": {\n" +
            "\"ics\": \"https://mobiledev.instructure.com/feeds/calendars/course_V80l8TpKunkQOkK2msOaajKOEDDc3oUQkyl9KHoM.ics\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"integration_id\": null,\n" +
            "\"enrollments\": [\n" +
            "{\n" +
            "\"type\": \"teacher\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"enrollment_state\": \"active\"\n" +
            "}\n" +
            "],\n" +
            "\"needs_grading_count\": 0,\n" +
            "\"hide_final_grades\": false,\n" +
            "\"workflow_state\": \"available\"\n" +
            "},\n" +
            "{\n" +
            "\"account_id\": 99299,\n" +
            "\"course_code\": \"Android\",\n" +
            "\"default_view\": \"feed\",\n" +
            "\"id\": 833052,\n" +
            "\"is_public\": false,\n" +
            "\"license\": \"private\",\n" +
            "\"name\": \"Android Development\",\n" +
            "\"start_at\": \"2012-10-09T06:00:00Z\",\n" +
            "\"end_at\": \"2014-11-22T05:55:00Z\",\n" +
            "\"public_syllabus\": false,\n" +
            "\"storage_quota_mb\": 500,\n" +
            "\"term\": {\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\",\n" +
            "\"sis_term_id\": null\n" +
            "},\n" +
            "\"apply_assignment_group_weights\": true,\n" +
            "\"calendar\": {\n" +
            "\"ics\": \"https://mobiledev.instructure.com/feeds/calendars/course_gCcqY1HU2LJCQxiPHWmrQNGnl7QRL3Xig6M237zD.ics\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"integration_id\": null,\n" +
            "\"enrollments\": [\n" +
            "{\n" +
            "\"type\": \"student\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"computed_current_score\": null,\n" +
            "\"computed_final_score\": null,\n" +
            "\"computed_current_grade\": null,\n" +
            "\"computed_final_grade\": null\n" +
            "}\n" +
            "],\n" +
            "\"hide_final_grades\": false,\n" +
            "\"workflow_state\": \"available\"\n" +
            "}\n" +
            "]";

}
