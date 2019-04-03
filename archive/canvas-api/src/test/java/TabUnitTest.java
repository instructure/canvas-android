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
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class TabUnitTest extends Assert {

    @Test
    public void testTabs() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Tab[] tabs = gson.fromJson(tabJSON, Tab[].class);

        assertNotNull(tabs);

        for(Tab tab : tabs) {
            assertNotNull(tab);
            assertNotNull(tab.getType());
            assertNotNull(tab.getExternalUrl());
            assertNotNull(tab.getLabel());
            assertNotNull(tab.getTabId());
        }
    }

    String tabJSON = "[\n" +
            "{\n" +
            "\"id\": \"home\",\n" +
            "\"html_url\": \"/courses/833052\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052\",\n" +
            "\"position\": 1,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Home\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"syllabus\",\n" +
            "\"html_url\": \"/courses/833052/assignments/syllabus\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/syllabus\",\n" +
            "\"position\": 2,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Syllabus\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"people\",\n" +
            "\"html_url\": \"/courses/833052/users\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/users\",\n" +
            "\"position\": 3,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"People\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"discussions\",\n" +
            "\"html_url\": \"/courses/833052/discussion_topics\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/discussion_topics\",\n" +
            "\"position\": 4,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Discussions\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"modules\",\n" +
            "\"html_url\": \"/courses/833052/modules\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/modules\",\n" +
            "\"position\": 5,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Modules\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"assignments\",\n" +
            "\"html_url\": \"/courses/833052/assignments\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/assignments\",\n" +
            "\"position\": 6,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Assignments\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"conferences\",\n" +
            "\"html_url\": \"/courses/833052/conferences\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/conferences\",\n" +
            "\"position\": 7,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Conferences\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"grades\",\n" +
            "\"html_url\": \"/courses/833052/grades\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/grades\",\n" +
            "\"position\": 8,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Grades\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"quizzes\",\n" +
            "\"html_url\": \"/courses/833052/quizzes\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/quizzes\",\n" +
            "\"position\": 9,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Quizzes\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"announcements\",\n" +
            "\"html_url\": \"/courses/833052/announcements\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/announcements\",\n" +
            "\"position\": 10,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Announcements\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"files\",\n" +
            "\"html_url\": \"/courses/833052/files\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/files\",\n" +
            "\"position\": 11,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Files\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"collaborations\",\n" +
            "\"html_url\": \"/courses/833052/collaborations\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/collaborations\",\n" +
            "\"position\": 12,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Collaborations\",\n" +
            "\"type\": \"internal\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"context_external_tool_131971\",\n" +
            "\"html_url\": \"/courses/833052/external_tools/131971\",\n" +
            "\"full_url\": \"https://mobiledev.instructure.com/courses/833052/external_tools/131971\",\n" +
            "\"position\": 13,\n" +
            "\"visibility\": \"public\",\n" +
            "\"label\": \"Redirect Tool\",\n" +
            "\"type\": \"external\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/external_tools/sessionless_launch?id=131971&launch_type=course_navigation\"\n" +
            "}" +
            "]";
}
