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
import com.instructure.canvasapi.model.PollChoice;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class PollChoiceUnitTest extends Assert {

    @Test
    public void testPollChoice() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        PollChoice[] pollChoices= gson.fromJson(pollChoiceJSON, PollChoice[].class);

        assertNotNull(pollChoices);

        for(PollChoice pollChoice : pollChoices) {
            assertNotNull(pollChoice);

            assertNotNull(pollChoice.getText());

            assertTrue(pollChoice.getPosition() >= 0);

            assertTrue(pollChoice.getId() > 0);
        }
    }

    String pollChoiceJSON = "[\n" +
            "{\n" +
            "\"id\": \"762\",\n" +
            "\"text\": \"Ghbb\",\n" +
            "\"position\": 0,\n" +
            "\"is_correct\": false\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"761\",\n" +
            "\"text\": \"Nnnbbb\",\n" +
            "\"position\": 1,\n" +
            "\"is_correct\": false\n" +
            "}" +
            "]";
}
