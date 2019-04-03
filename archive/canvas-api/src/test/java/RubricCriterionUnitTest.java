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
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class RubricCriterionUnitTest extends Assert {

    @Test
    public void testRubricCriterion() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        RubricCriterion[] rubricCriterions = gson.fromJson(rubricCriterionJSON, RubricCriterion[].class);

        assertNotNull(rubricCriterions);

        for(RubricCriterion rubricCriterion : rubricCriterions) {
            assertNotNull(rubricCriterion);

            assertNotNull(rubricCriterion.getCriterionDescription());

            assertNotNull(rubricCriterion.getId());

            assertTrue(rubricCriterion.getPoints() >= 0);
        }
    }

    @Test
    public void testRubricCriterionRating() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        RubricCriterion[] rubricCriterions = gson.fromJson(rubricCriterionJSON, RubricCriterion[].class);

        for(RubricCriterion rubricCriterion : rubricCriterions) {
            if(rubricCriterion.getGradedCriterionRating() != null) {
                List<RubricCriterionRating> ratings = rubricCriterion.getRatings();
                for(RubricCriterionRating rubricCriterionRating : ratings) {
                    assertNotNull(rubricCriterionRating.getId());
                    assertNotNull(rubricCriterionRating.getRatingDescription());
                }
            }
        }
    }


    String rubricCriterionJSON = "[\n" +
            "{\n" +
            "\"id\": \"387653_8589\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
                "{\n" +
                "\"id\": \"blank\",\n" +
                "\"points\": 10,\n" +
                "\"description\": \"Full Marks\"\n" +
                "},\n" +
            "{\n" +
            "\"id\": \"blank_2\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_1612\",\n" +
            "\"points\": 12,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"387653_8361\",\n" +
            "\"points\": 12,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_870\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_8896\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"387653_7003\",\n" +
            "\"points\": 10,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_6719\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_5670\",\n" +
            "\"points\": 8,\n" +
            "\"description\": \"Description of criterion\",\n" +
            "\"long_description\": \"\",\n" +
            "\"ratings\": [\n" +
            "{\n" +
            "\"id\": \"387653_3621\",\n" +
            "\"points\": 8,\n" +
            "\"description\": \"Full Marks\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"387653_2577\",\n" +
            "\"points\": 0,\n" +
            "\"description\": \"No Marks\"\n" +
            "}\n" +
            "]\n" +
            "}" +
            "]";

}
