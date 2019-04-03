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
import com.instructure.canvasapi.model.CustomColumn;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class CustomGradebookColumnUnitTest extends Assert {

    @Test
    public void testCustomColumnData() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        CustomColumn[] customColumns = gson.fromJson(customColumnData, CustomColumn[].class);

        assertNotNull(customColumns);
        assertEquals(3, customColumns.length);

        for(CustomColumn customColumn : customColumns){
            assertNotNull(customColumn.getId());
            assertNotNull(customColumn.getPosition());
            assertNotNull(customColumn.isTeacher_notes());
            assertNotNull(customColumn.isHidden());
        }
    }

    private static final String customColumnData = "["
            +"{\"id\":1234,"
                +"\"position\":0,"
                +"\"teacher_notes\":false,"
                +"\"title\":\"Column1\","
                +"\"hidden\":false},"
            +"{\"id\":2345,"
                +"\"position\":1,"
                +"\"teacher_notes\":false,"
                +"\"title\":\"Column2\","
                +"\"hidden\":false},"
            +"{\"id\":3456,"
                +"\"position\":2,"
                +"\"teacher_notes\":true,"
                +"\"title\":\"Column3\","
                +"\"hidden\":false}"
        +"]";
}
