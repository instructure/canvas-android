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
import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class RecipientUnitTest extends Assert {

    @Test
    public void testRecipient() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Recipient[] recipients = gson.fromJson(recipientJSON, Recipient[].class);

        assertNotNull(recipients);

        for(Recipient recipient : recipients) {
            assertNotNull(recipient);

            assertNotNull(recipient.getName());
            assertNotNull(recipient.getAvatarURL());
            assertNotNull(recipient.getRecipientType());
            assertNotNull(recipient.getStringId());
        }
    }

    String recipientJSON = "[\n" +
            "{\n" +
            "\"id\": \"course_1016013\",\n" +
            "\"name\": \"An In-Depth Study of the Year 2000\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 4,\n" +
            "\"permissions\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"course_833052\",\n" +
            "\"name\": \"Android Development\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 43,\n" +
            "\"permissions\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"course_953090\",\n" +
            "\"name\": \"Android Unit Tests\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 4,\n" +
            "\"permissions\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"course_1279999\",\n" +
            "\"name\": \"Candroid\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 10,\n" +
            "\"permissions\": {}\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"section_892683\",\n" +
            "\"name\": \"Advanced\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 6,\n" +
            "\"permissions\": {},\n" +
            "\"context_name\": \"Android Development\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"section_889720\",\n" +
            "\"name\": \"Android Development\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 34,\n" +
            "\"permissions\": {},\n" +
            "\"context_name\": \"Android Development\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"group_220118\",\n" +
            "\"name\": \"Add another one\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 4,\n" +
            "\"permissions\": {},\n" +
            "\"context_name\": \"Beginning iOS Development\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"group_155489\",\n" +
            "\"name\": \"Sorry guys, another group\",\n" +
            "\"avatar_url\": \"https://mobiledev.instructure.com/images/messages/avatar-group-50.png\",\n" +
            "\"type\": \"context\",\n" +
            "\"user_count\": 4,\n" +
            "\"permissions\": {},\n" +
            "\"context_name\": \"Beginning iOS Development\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 5803223,\n" +
            "\"name\": \"acannon+s@instructure.com\",\n" +
            "\"common_courses\": {\n" +
            "\"24219\": [\n" +
            "\"StudentEnrollment\"\n" +
            "]\n" +
            "},\n" +
            "\"common_groups\": {\n" +
            "\"220118\": [\n" +
            "\"Member\"\n" +
            "]\n" +
            "},\n" +
            "\"avatar_url\": \"https://secure.gravatar.com/avatar/827ab0b5176ee8ce02b780b272dbf857?s=50&d=https%3A%2F%2Fcanvas.instructure.com%2Fimages%2Fmessages%2Favatar-50.png\"\n" +
            "},\n" +
            "{\n" +
            "\"id\": 5803222,\n" +
            "\"name\": \"acannon+t@instructure.com\",\n" +
            "\"common_courses\": {\n" +
            "\"24219\": [\n" +
            "\"TeacherEnrollment\"\n" +
            "]\n" +
            "},\n" +
            "\"common_groups\": {},\n" +
            "\"avatar_url\": \"https://secure.gravatar.com/avatar/452e281f449f50719c4fca1b06289bbb?s=50&d=https%3A%2F%2Fcanvas.instructure.com%2Fimages%2Fmessages%2Favatar-50.png\"\n" +
            "}" +
            "]";
}
