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
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class EnrollmentUnitTest extends Assert {

    @Test
    public void courseEnrollmentsTest(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Enrollment[] enrollments = gson.fromJson(courseEnrollmentsJSON, Enrollment[].class);

        assertNotNull(enrollments);

        Enrollment enrollment = enrollments[0];

        assertNotNull(enrollment);

        assertTrue(enrollment.getCourseId() == 1383418);
        assertTrue(enrollment.getId() == 23221371);
        assertTrue(enrollment.getRole().equals("StudentEnrollment"));
        assertTrue(enrollment.getCurrentScore() == 191.5);
        assertTrue(enrollment.getUserId() == 5834817);

    }

    final String courseEnrollmentsJSON = "[\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 1383418,\n" +
            "\"course_section_id\": 1586838,\n" +
            "\"created_at\": \"2014-06-27T16:30:59Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 23221371,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"StudentEnrollment\",\n" +
            "\"updated_at\": \"2014-07-14T16:49:15Z\",\n" +
            "\"user_id\": 5834817,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-15T21:17:27Z\",\n" +
            "\"total_activity_time\": 26692,\n" +
            "\"grades\": {\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/1383418/grades/5834817\",\n" +
            "\"current_score\": 191.5,\n" +
            "\"final_score\": 115.5,\n" +
            "\"current_grade\": null,\n" +
            "\"final_grade\": null\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/1383418/users/5834817\",\n" +
            "\"user\": {\n" +
            "\"id\": 5834817,\n" +
            "\"name\": \"Hackdown Hodor\",\n" +
            "\"sortable_name\": \"Hodor, Hackdown\",\n" +
            "\"short_name\": \"Hackdown Hodor\",\n" +
            "\"login_id\": \"bla@gmail.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 1383418,\n" +
            "\"course_section_id\": 1586838,\n" +
            "\"created_at\": \"2014-06-16T19:15:41Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 23097257,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"TeacherEnrollment\",\n" +
            "\"updated_at\": \"2014-07-14T16:48:46Z\",\n" +
            "\"user_id\": 5814789,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-17T18:36:01Z\",\n" +
            "\"total_activity_time\": 20396,\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/1383418/users/5814789\",\n" +
            "\"user\": {\n" +
            "\"id\": 5814789,\n" +
            "\"name\": \"bla@gmail.com\",\n" +
            "\"sortable_name\": \"bla@gmail.com\",\n" +
            "\"short_name\": \"bla@gmail.com\",\n" +
            "\"login_id\": \"bla@gmail.com\"\n" +
            "}\n" +
            "}\n" +
            "]";
}
