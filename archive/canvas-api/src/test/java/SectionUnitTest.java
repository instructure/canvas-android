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
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class SectionUnitTest extends Assert {

    @Test
    public void sectionsTest() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Section[] sections = gson.fromJson(sectionJSON, Section[].class);

        assertNotNull(sections);

        Section section = sections[0];

        assertNotNull(section);

        assertTrue(section.getCourse_id() == 1098050);
        assertTrue(section.getId() == 1243410);
        assertNull(section.getEnd_at());
    }

    @Test
    public void sectionsWithStudentsTest(){
        Gson gson = CanvasRestAdapter.getGSONParser();
        Section[] sections = gson.fromJson(sectionsWithStudentsJSON, Section[].class);

        assertNotNull(sections);

        Section section = sections[0];

        assertNotNull(section);

        List<User> sectionStudents = section.getStudents();

        assertNotNull(sectionStudents);

        User user = sectionStudents.get(0);

        assertNotNull(user);

        assertTrue(user.getId() == 3558540);
        assertNotNull(user.getName());
        assertNotNull(user.getShortName());
    }


    //course section
    //@GET("/{courseid}/sections")
    //void getFirstPageSectionsList(@Path("courseid") long courseID, Callback<Section[]> callback);
    final String sectionJSON = "[\n" +
            "{\n" +
            "\"course_id\": 1098050,\n" +
            "\"end_at\": null,\n" +
            "\"id\": 1243410,\n" +
            "\"name\": \"IOS Topdown 4 (June 19 2013)\",\n" +
            "\"nonxlist_course_id\": null,\n" +
            "\"start_at\": null\n" +
            "}\n" +
            "]";


    //sections with students
    //@GET("/{courseid}/sections?include[]=students")
    //void getCourseSectionsWithStudents(@Path("courseid") long courseID, Callback<Section[]> callback);
    final String sectionsWithStudentsJSON = "[\n" +
            "{\n" +
            "\"course_id\": 1098050,\n" +
            "\"end_at\": null,\n" +
            "\"id\": 1243410,\n" +
            "\"name\": \"IOS Topdown 4 (June 19 2013)\",\n" +
            "\"nonxlist_course_id\": null,\n" +
            "\"start_at\": null,\n" +
            "\"students\": [\n" +
            "{\n" +
            "\"id\": 3558540,\n" +
            "\"name\": \"S3First S3Last(5C)\",\n" +
            "\"sortable_name\": \"S3Last(5C), S3First\",\n" +
            "\"short_name\": \"S3First S3Last(5C)\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3564935,\n" +
            "\"name\": \"S6First S6Last(IPad 3)\",\n" +
            "\"sortable_name\": \"3), S6First S6Last(IPad\",\n" +
            "\"short_name\": \"s6\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3564934,\n" +
            "\"name\": \"S5First S5Last(4X)\",\n" +
            "\"sortable_name\": \"S5Last(4X), S5First\",\n" +
            "\"short_name\": \"S5First S5Last(4X)\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3558541,\n" +
            "\"name\": \"S4First S4Last(Mini Retina)\",\n" +
            "\"sortable_name\": \"Retina), S4First S4Last(Mini\",\n" +
            "\"short_name\": \"S4First S4Last(Mini Retina)\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3558537,\n" +
            "\"name\": \"S2First S2Last(Mini v1)\",\n" +
            "\"sortable_name\": \"v1), S2First S2Last(Mini\",\n" +
            "\"short_name\": \"S2First S2Last(Mini v1)\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3564936,\n" +
            "\"name\": \"S7First S7Last\",\n" +
            "\"sortable_name\": \"S7Last, S7First\",\n" +
            "\"short_name\": \"S7First S7Last\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 3558536,\n" +
            "\"name\": \"S1First S1Last(5S)\",\n" +
            "\"sortable_name\": \"S1Last(5S), S1First\",\n" +
            "\"short_name\": \"S1First S1Last(5S)\"\n" +
            "}\n" +
            "]\n" +
            "}\n" +
            "]";


}
