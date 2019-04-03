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
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class UserUnitTest extends Assert {

    @Test
    public void testUser() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        User user = gson.fromJson(userJSON, User.class);

        assertEquals(user.getAvatarURL(), "https://www.example.com");

        assertEquals(user.getId(), 1111);

        assertEquals(user.getEmail(), "primary_email");

        assertEquals(user.getLoginId(), "login_id");

        assertEquals(user.getName(), "Sam Franklen");

        assertEquals(user.getShortName(),"Samf");
    }

    String userJSON = "{\"id\":1111,\"name\":\"Sam Franklen\",\"short_name\":\"Samf\",\"sortable_name\":\"Franklen, Sam\",\"login_id\":\"login_id\",\"avatar_url\":\"https://www.example.com\",\"title\":null,\"bio\":null,\"primary_email\":\"primary_email\",\"time_zone\":\"America/Denver\",\"calendar\":{\"ics\":\"https://mobiledev.instructure.com/feeds/calendars/user_8JCkdINx6RO3dB8Ao5aPQCJO49p8XUpCbZgmqk7X.ics\"}}";
}
