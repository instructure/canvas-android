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
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class AssignmentGroupUnitTest extends Assert {

    @Test
    public void testAssignmentGroup() {
        String assignmentGroupJSON = "[\n" +
                "{\n" +
                "\"group_weight\": 0,\n" +
                "\"id\": 534101,\n" +
                "\"name\": \"Extra Credit\",\n" +
                "\"position\": 1,\n" +
                "\"rules\": {}\n" +
                "},\n" +
                "{\n" +
                "\"group_weight\": 0,\n" +
                "\"id\": 534100,\n" +
                "\"name\": \"Assignments\",\n" +
                "\"position\": 2,\n" +
                "\"rules\": {}\n" +
                "}\n" +
                "]";

        Gson gson = CanvasRestAdapter.getGSONParser();
        AssignmentGroup[] assignmentGroup = gson.fromJson(assignmentGroupJSON, AssignmentGroup[].class);

        assertNotNull(assignmentGroup);

        assertEquals(2, assignmentGroup.length);

        assertNotNull(assignmentGroup[0].getName());
        assertNotNull(assignmentGroup[1].getName());

        assertTrue(assignmentGroup[0].getId() > 0);
        assertTrue(assignmentGroup[1].getId() > 0);
    }
}
