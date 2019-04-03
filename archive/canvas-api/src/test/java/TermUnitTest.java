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
import com.instructure.canvasapi.model.Term;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class TermUnitTest extends Assert {


    @Test
    public void testTerm() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Term term = gson.fromJson(termJSON, Term.class);

        assertNotNull(term);

        assertNotNull(term.getName());
        assertTrue(term.getId() > 0);
    }

    //https://mobiledev.instructure.com/api/v1/courses/833052/?include[]=term&include[]=permissions&include[]=license&include[]=is_public&include[]=needs_grading_count
    String termJSON =
            "{\n" +
            "\"end_at\": null,\n" +
            "\"id\": 3142,\n" +
            "\"name\": \"Default Term\",\n" +
            "\"start_at\": null,\n" +
            "\"workflow_state\": \"active\"\n" +
            "}";
}
