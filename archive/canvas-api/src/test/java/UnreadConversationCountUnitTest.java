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
import com.instructure.canvasapi.model.UnreadConversationCount;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class UnreadConversationCountUnitTest extends Assert {

    @Test
    public void testUnreadConversationCount() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        UnreadConversationCount unreadConversationCount = gson.fromJson(unreadConversationCountJSON, UnreadConversationCount.class);

        assertEquals(unreadConversationCount.getUnreadCount(), "3");
    }

    String unreadConversationCountJSON = "{\"unread_count\":\"3\"}";

}
