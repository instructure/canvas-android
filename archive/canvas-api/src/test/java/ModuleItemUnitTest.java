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
import com.instructure.canvasapi.model.ModuleItem;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class ModuleItemUnitTest extends Assert {

    @Test
    public void testModuleItem() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        ModuleItem[] moduleItems = gson.fromJson(moduleItemJSON, ModuleItem[].class);

        for (ModuleItem moduleItem : moduleItems){
            assertTrue(moduleItem.getId() > 0);

            assertNotNull(moduleItem.getType());

            assertNotNull(moduleItem.getTitle());

            assertNotNull(moduleItem.getHtml_url());

            assertNotNull(moduleItem.getUrl());

            if(moduleItem.getCompletionRequirement() != null) {
                assertNotNull(moduleItem.getCompletionRequirement().getType());
            }
        }
    }

    @Test
    public void testModuleItemMasteryPath() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        ModuleItem[] moduleItems = gson.fromJson(moduleItemWithMasteryPath, ModuleItem[].class);

        for (ModuleItem moduleItem : moduleItems){
            assertTrue(moduleItem.getId() > 0);

            assertNotNull(moduleItem.getType());

            assertNotNull(moduleItem.getTitle());

            assertNotNull(moduleItem.getHtml_url());

            assertNotNull(moduleItem.getUrl());

            if(moduleItem.getId() == 1) {
                //first module item has a mastery paths
                assertNotNull(moduleItem.getMasteryPaths());
            } else {
                assertNull(moduleItem.getMasteryPaths());
            }
        }
    }
    String moduleItemJSON = "[\n" +
            "{\n" +
            "\"id\": 9012239,\n" +
            "\"indent\": 0,\n" +
            "\"position\": 1,\n" +
            "\"title\": \"Android 101\",\n" +
            "\"type\": \"Assignment\",\n" +
            "\"module_id\": 1059720,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/modules/items/9012239\",\n" +
            "\"content_id\": 2241839,\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/assignments/2241839\",\n" +
            "\"completion_requirement\": {\n" +
            "\"type\": \"must_submit\",\n" +
            "\"completed\": true\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"id\": 9012244,\n" +
            "\"indent\": 0,\n" +
            "\"position\": 2,\n" +
            "\"title\": \"Favorite App Video\",\n" +
            "\"type\": \"Assignment\",\n" +
            "\"module_id\": 1059720,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/modules/items/9012244\",\n" +
            "\"content_id\": 2241864,\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/assignments/2241864\",\n" +
            "\"completion_requirement\": {\n" +
            "\"type\": \"min_score\",\n" +
            "\"min_score\": \"5\",\n" +
            "\"completed\": true\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"id\": 9012248,\n" +
            "\"indent\": 0,\n" +
            "\"position\": 3,\n" +
            "\"title\": \"Android vs. iOS\",\n" +
            "\"type\": \"Discussion\",\n" +
            "\"module_id\": 1059720,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/modules/items/9012248\",\n" +
            "\"content_id\": 1369942,\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/discussion_topics/1369942\",\n" +
            "\"completion_requirement\": {\n" +
            "\"type\": \"must_contribute\",\n" +
            "\"completed\": false\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"id\": 9012251,\n" +
            "\"indent\": 0,\n" +
            "\"position\": 4,\n" +
            "\"title\": \"Easy Quiz\",\n" +
            "\"type\": \"Quiz\",\n" +
            "\"module_id\": 1059720,\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/modules/items/9012251\",\n" +
            "\"content_id\": 757314,\n" +
            "\"url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/quizzes/757314\",\n" +
            "\"completion_requirement\": {\n" +
            "\"type\": \"must_submit\",\n" +
            "\"completed\": true\n" +
            "}\n" +
            "}\n" +
            "]";


    String moduleItemWithMasteryPath =
            "[\n" +
            "  {\n" +
            "    \"id\": 1,\n" +
            "    \"title\": \"Assignment 1~1\",\n" +
            "    \"position\": 1,\n" +
            "    \"indent\": 0,\n" +
            "    \"type\": \"Assignment\",\n" +
            "    \"module_id\": 1,\n" +
            "    \"html_url\": \"http://canvas.docker/courses/1/modules/items/1\",\n" +
            "    \"content_id\": 1,\n" +
            "    \"url\": \"http://canvas.docker/api/v1/courses/1/assignments/1\",\n" +
            "    \"mastery_paths\": {\n" +
            "      \"locked\": false,\n" +
            "      \"assignment_sets\": [\n" +
            "        {\n" +
            "          \"id\": 2,\n" +
            "          \"scoring_range_id\": 2,\n" +
            "          \"created_at\": \"2016-08-03T19:04:44.860Z\",\n" +
            "          \"updated_at\": \"2016-08-03T19:04:44.860Z\",\n" +
            "          \"position\": 1,\n" +
            "          \"assignments\": [\n" +
            "            {\n" +
            "              \"id\": 2,\n" +
            "              \"assignment_id\": \"5\",\n" +
            "              \"created_at\": \"2016-08-03T19:04:44.865Z\",\n" +
            "              \"updated_at\": \"2016-08-03T19:04:44.865Z\",\n" +
            "              \"override_id\": 8,\n" +
            "              \"assignment_set_id\": 2,\n" +
            "              \"position\": 1,\n" +
            "              \"model\": {\n" +
            "                \"id\": 5,\n" +
            "                \"title\": \"Quiz 1~1\",\n" +
            "                \"description\": \"\",\n" +
            "                \"due_at\": null,\n" +
            "                \"unlock_at\": null,\n" +
            "                \"lock_at\": null,\n" +
            "                \"points_possible\": 0,\n" +
            "                \"min_score\": null,\n" +
            "                \"max_score\": null,\n" +
            "                \"grading_type\": \"points\",\n" +
            "                \"submission_types\": [\n" +
            "                   \"online_quiz\"],\n" +
            "                \"workflow_state\": \"published\",\n" +
            "                \"context_id\": 1,\n" +
            "                \"context_type\": \"Course\",\n" +
            "                \"updated_at\": \"2016-08-11T17:32:34Z\",\n" +
            "                \"context_code\": \"course_1\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 5,\n" +
            "    \"title\": \"Quiz 1~1\",\n" +
            "    \"position\": 3,\n" +
            "    \"indent\": 0,\n" +
            "    \"type\": \"Quiz\",\n" +
            "    \"module_id\": 1,\n" +
            "    \"html_url\": \"http://canvas.docker/courses/1/modules/items/5\",\n" +
            "    \"content_id\": 1,\n" +
            "    \"url\": \"http://canvas.docker/api/v1/courses/1/quizzes/1\",\n" +
            "    \"mastery_paths\": null\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 7,\n" +
            "    \"title\": \"Discussion 1~1\",\n" +
            "    \"position\": 5,\n" +
            "    \"indent\": 0,\n" +
            "    \"type\": \"Discussion\",\n" +
            "    \"module_id\": 1,\n" +
            "    \"html_url\": \"http://canvas.docker/courses/1/modules/items/7\",\n" +
            "    \"content_id\": 1,\n" +
            "    \"url\": \"http://canvas.docker/api/v1/courses/1/discussion_topics/1\",\n" +
            "    \"mastery_paths\": null\n" +
            "  },\n" +
            "  {\n" +
            "    \"id\": 8,\n" +
            "    \"title\": \"Assignment 1~2\",\n" +
            "    \"position\": 6,\n" +
            "    \"indent\": 0,\n" +
            "    \"type\": \"Assignment\",\n" +
            "    \"module_id\": 1,\n" +
            "    \"html_url\": \"http://canvas.docker/courses/1/modules/items/8\",\n" +
            "    \"content_id\": 2,\n" +
            "    \"url\": \"http://canvas.docker/api/v1/courses/1/assignments/2\",\n" +
            "    \"mastery_paths\": null\n" +
            "  }\n" +
            "]";
}
