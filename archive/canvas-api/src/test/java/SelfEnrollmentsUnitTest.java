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
import com.google.gson.reflect.TypeToken;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class SelfEnrollmentsUnitTest extends Assert {

    @Test
    public void test1() {


        Gson gson = CanvasRestAdapter.getGSONParser();
        List<Enrollment> list = gson.fromJson(JSON, new TypeToken<List<Enrollment>>(){}.getType());
        assertNotNull(list);

        assertTrue(list.size() > 0);

        for(Enrollment e : list) {
            assertNotNull(e.getType());
            assertNotNull(e.getEnrollmentState());
            assertNotNull(e.getRole());
            assertTrue(e.isObserver() || e.isStudent() || e.isTA() || e.isTeacher());
        }
    }

    ///users/self/enrollments
    final String JSON = "[\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 1279999,\n" +
            "\"course_section_id\": 1486081,\n" +
            "\"created_at\": \"2014-03-17T16:36:36Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 21917612,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"TeacherEnrollment\",\n" +
            "\"updated_at\": \"2014-06-16T22:48:18Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-09T18:49:31Z\",\n" +
            "\"total_activity_time\": 2042,\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 833052,\n" +
            "\"course_section_id\": 889720,\n" +
            "\"created_at\": \"2014-03-17T16:37:33Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 21917613,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"StudentEnrollment\",\n" +
            "\"updated_at\": \"2014-06-23T19:56:18Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-09T22:04:48Z\",\n" +
            "\"total_activity_time\": 1217,\n" +
            "\"grades\": {\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"current_score\": null,\n" +
            "\"final_score\": 0,\n" +
            "\"current_grade\": null,\n" +
            "\"final_grade\": null\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 24219,\n" +
            "\"course_section_id\": 31105,\n" +
            "\"created_at\": \"2014-03-17T16:37:54Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 21917614,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"StudentEnrollment\",\n" +
            "\"updated_at\": \"2014-06-23T16:27:55Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-09T15:35:39Z\",\n" +
            "\"total_activity_time\": 4689,\n" +
            "\"grades\": {\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\"\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 36376,\n" +
            "\"course_section_id\": 986129,\n" +
            "\"created_at\": \"2014-03-17T16:38:04Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 21917615,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"StudentEnrollment\",\n" +
            "\"updated_at\": \"2014-06-16T22:48:18Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-09T15:53:45Z\",\n" +
            "\"total_activity_time\": 0,\n" +
            "\"grades\": {\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"current_score\": null,\n" +
            "\"final_score\": 0,\n" +
            "\"current_grade\": null,\n" +
            "\"final_grade\": null\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 1383420,\n" +
            "\"course_section_id\": 1586842,\n" +
            "\"created_at\": \"2014-06-16T19:21:02Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 23097825,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"TeacherEnrollment\",\n" +
            "\"updated_at\": \"2014-06-16T19:21:13Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"TeacherEnrollment\",\n" +
            "\"last_activity_at\": \"2014-07-14T16:37:01Z\",\n" +
            "\"total_activity_time\": 4381,\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"associated_user_id\": null,\n" +
            "\"course_id\": 1279999,\n" +
            "\"course_section_id\": 1486081,\n" +
            "\"created_at\": \"2014-06-16T22:48:03Z\",\n" +
            "\"end_at\": null,\n" +
            "\"id\": 23100952,\n" +
            "\"limit_privileges_to_course_section\": false,\n" +
            "\"root_account_id\": 99298,\n" +
            "\"start_at\": null,\n" +
            "\"type\": \"StudentEnrollment\",\n" +
            "\"updated_at\": \"2014-06-16T22:48:18Z\",\n" +
            "\"user_id\": 5347622,\n" +
            "\"enrollment_state\": \"active\",\n" +
            "\"role\": \"StudentEnrollment\",\n" +
            "\"last_activity_at\": \"2014-06-16T22:48:19Z\",\n" +
            "\"total_activity_time\": 0,\n" +
            "\"grades\": {\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"current_score\": null,\n" +
            "\"final_score\": null,\n" +
            "\"current_grade\": null,\n" +
            "\"final_grade\": null\n" +
            "},\n" +
            "\"sis_course_id\": null,\n" +
            "\"course_integration_id\": null,\n" +
            "\"sis_section_id\": null,\n" +
            "\"section_integration_id\": null,\n" +
            "\"html_url\": \"http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg\",\n" +
            "\"user\": {\n" +
            "\"id\": 5347622,\n" +
            "\"name\": \"noreply@instructure.com\",\n" +
            "\"sortable_name\": \"noreply@instructure.com\",\n" +
            "\"short_name\": \"noreply@instructure.com\",\n" +
            "\"login_id\": \"noreply@instructure.com\"\n" +
            "}\n" +
            "}\n" +
            "]";

}
