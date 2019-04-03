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
import com.instructure.canvasapi.model.RubricCriterion;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class RubricAssessmentUnitTest extends Assert {

    @Test
    public void testRubricAssessmentRating() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        RubricCriterion[] rubricCriterions = gson.fromJson(rubricAssessmentJSON, RubricCriterion[].class);

        assertNotNull(rubricCriterions);

        for(RubricCriterion rubricCriterion : rubricCriterions) {
            assertNotNull(rubricCriterion);

            assertTrue(rubricCriterion.getPoints() >= 0);
        }
    }


    String rubricAssessmentJSON = "[\n" +
            "{\n" +
            "\"387653_8589\": {\n" +
            "\"points\": 10,\n" +
            "\"comments\": \"fdsfsd\"\n" +
            "},\n" +
            "\"387653_1612\": {\n" +
            "\"points\": 12,\n" +
            "\"comments\": \"test\"\n" +
            "},\n" +
            "\"387653_8896\": {\n" +
            "\"points\": 10,\n" +
            "\"comments\": null\n" +
            "},\n" +
            "\"387653_5670\": {\n" +
            "\"points\": 8,\n" +
            "\"comments\": null\n" +
            "}\n" +
            "}" +
            "]";

}
