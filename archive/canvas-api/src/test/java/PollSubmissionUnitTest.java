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
import com.instructure.canvasapi.model.PollSubmission;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class PollSubmissionUnitTest extends Assert {

    @Test
    public void testPollSubmission() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        PollSubmission pollSubmission = gson.fromJson(pollSubmissionJSON, PollSubmission.class);

        assertNotNull(pollSubmission.getCreated_at());

        assertTrue(pollSubmission.getId() > 0);

        assertTrue(pollSubmission.getPoll_choice_id() > 0);
        assertTrue(pollSubmission.getUser_id() > 0);
    }

    String pollSubmissionJSON =
            "{\n" +
            "\"id\": \"7741\",\n" +
            "\"poll_session_id\": \"1230\",\n" +
            "\"poll_choice_id\": \"2212\",\n" +
            "\"user_id\": \"3360251\",\n" +
            "\"created_at\": \"2015-03-17T16:17:08Z\"\n" +
            "}";
}
