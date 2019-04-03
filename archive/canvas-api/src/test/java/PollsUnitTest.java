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
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;


@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class PollsUnitTest extends Assert {

    @Test
    public void testPoll() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Poll[] polls= gson.fromJson(pollsJSON, Poll[].class);

        for(Poll poll : polls) {
            assertNotNull(poll.getCreated_at());

            assertNotNull(poll.getQuestion());

            assertTrue(poll.getId() > 0);
        }
    }

    String pollsJSON = "[\n" +
            "{\n" +
            "\"id\": \"289\",\n" +
            "\"question\": \"Jcjjdjjdd\",\n" +
            "\"description\": null,\n" +
            "\"created_at\": \"2014-08-19T15:34:06Z\",\n" +
            "\"total_results\": {},\n" +
            "\"user_id\": \"4599568\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"270\",\n" +
            "\"question\": \"fewqfewq\",\n" +
            "\"description\": null,\n" +
            "\"created_at\": \"2014-08-11T20:16:44Z\",\n" +
            "\"total_results\": {},\n" +
            "\"user_id\": \"4599568\"\n" +
            "}" +
            "]";

}
