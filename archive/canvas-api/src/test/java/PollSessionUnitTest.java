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
import com.instructure.canvasapi.model.PollSession;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class PollSessionUnitTest extends Assert {

    @Test
    public void testPollSession() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        PollSession[] pollSessions= gson.fromJson(pollSessionJSON, PollSession[].class);

        assertNotNull(pollSessions);

        for(PollSession pollSession : pollSessions) {
            assertNotNull(pollSession);

            assertNotNull(pollSession.getCreated_at());

            assertTrue(pollSession.getId() > 0);

            assertTrue(pollSession.getCourse_id() > 0);
            assertTrue(pollSession.getCourse_section_id() > 0);
            assertTrue(pollSession.getPoll_id() > 0);

        }
    }

    String pollSessionJSON = "[\n" +
            "{\n" +
            "\"id\": \"1230\",\n" +
            "\"is_published\": true,\n" +
            "\"course_id\": \"833052\",\n" +
            "\"course_section_id\": \"892683\",\n" +
            "\"created_at\": \"2015-03-17T15:52:23Z\",\n" +
            "\"poll_id\": \"712\",\n" +
            "\"has_submitted\": false,\n" +
            "\"poll_submissions\": [],\n" +
            "\"has_public_results\": false,\n" +
            "\"results\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1229\",\n" +
            "\"is_published\": true,\n" +
            "\"course_id\": \"833052\",\n" +
            "\"course_section_id\": \"892682\",\n" +
            "\"created_at\": \"2015-03-17T15:52:23Z\",\n" +
            "\"poll_id\": \"712\",\n" +
            "\"has_submitted\": false,\n" +
            "\"poll_submissions\": [],\n" +
            "\"has_public_results\": false,\n" +
            "\"results\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1227\",\n" +
            "\"is_published\": true,\n" +
            "\"course_id\": \"833052\",\n" +
            "\"course_section_id\": \"889720\",\n" +
            "\"created_at\": \"2015-03-17T15:52:22Z\",\n" +
            "\"poll_id\": \"712\",\n" +
            "\"has_submitted\": false,\n" +
            "\"poll_submissions\": [],\n" +
            "\"has_public_results\": false,\n" +
            "\"results\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1228\",\n" +
            "\"is_published\": true,\n" +
            "\"course_id\": \"833052\",\n" +
            "\"course_section_id\": \"1772044\",\n" +
            "\"created_at\": \"2015-03-17T15:52:22Z\",\n" +
            "\"poll_id\": \"712\",\n" +
            "\"has_submitted\": false,\n" +
            "\"poll_submissions\": [],\n" +
            "\"has_public_results\": false,\n" +
            "\"results\": {}\n" +
            "}" +
            "]";
}
