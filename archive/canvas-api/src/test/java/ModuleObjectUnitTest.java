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
import com.instructure.canvasapi.model.ModuleObject;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class ModuleObjectUnitTest extends Assert {

    @Test
    public void testModuleObject() {
        Gson gson = CanvasRestAdapter.getGSONParser();

        ModuleObject[] moduleObjects = gson.fromJson(moduleObjectJSON, ModuleObject[].class);

        for(int i = 0; i < moduleObjects.length; i++) {
            Assert.assertTrue(moduleObjects[i].getId() > 0);

            Assert.assertNotNull(moduleObjects[i].getName());


            //only the module object with index of 1 has an unlock date
            if(i == 1) {
                Assert.assertNotNull(moduleObjects[i].getUnlock_at());
            }

            assertNotNull(moduleObjects[i].getState());


            //objects 1 - 3 have prerequisite ids
            if(i > 0) {
                for(int j = 0; j < moduleObjects[i].getPrerequisite_ids().length; j++){
                    assertTrue(moduleObjects[i].getPrerequisite_ids()[j] > 0);
                }
            }
        }
    }

    String moduleObjectJSON = "[\n" +
            "{\n" +
            "\"id\": 1059720,\n" +
            "\"name\": \"Beginners\",\n" +
            "\"position\": 1,\n" +
            "\"unlock_at\": null,\n" +
            "\"require_sequential_progress\": false,\n" +
            "\"prerequisite_module_ids\": [],\n" +
            "\"state\": \"started\",\n" +
            "\"completed_at\": null,\n" +
            "\"items_count\": 4,\n" +
            "\"items_url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059720/items\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 1059721,\n" +
            "\"name\": \"Advanced\",\n" +
            "\"position\": 2,\n" +
            "\"unlock_at\": \"2013-07-31T06:00:00Z\",\n" +
            "\"require_sequential_progress\": false,\n" +
            "\"prerequisite_module_ids\": [\n" +
            "1059720\n" +
            "],\n" +
            "\"state\": \"locked\",\n" +
            "\"completed_at\": null,\n" +
            "\"items_count\": 1,\n" +
            "\"items_url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059721/items\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 1059722,\n" +
            "\"name\": \"User Interface\",\n" +
            "\"position\": 3,\n" +
            "\"unlock_at\": null,\n" +
            "\"require_sequential_progress\": false,\n" +
            "\"prerequisite_module_ids\": [\n" +
            "1059721\n" +
            "],\n" +
            "\"state\": \"locked\",\n" +
            "\"completed_at\": null,\n" +
            "\"items_count\": 0,\n" +
            "\"items_url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059722/items\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 1059723,\n" +
            "\"name\": \"Jelly Bean\",\n" +
            "\"position\": 4,\n" +
            "\"unlock_at\": null,\n" +
            "\"require_sequential_progress\": false,\n" +
            "\"prerequisite_module_ids\": [\n" +
            "1059722\n" +
            "],\n" +
            "\"state\": \"locked\",\n" +
            "\"completed_at\": null,\n" +
            "\"items_count\": 0,\n" +
            "\"items_url\": \"https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059723/items\"\n" +
            "}\n" +
            "]";
}
