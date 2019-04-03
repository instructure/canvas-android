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
import com.google.gson.reflect.TypeToken;
import com.instructure.canvasapi.model.Bookmark;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import junit.framework.Assert;

import java.util.List;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class BookmarkUnitTest extends Assert {

    @Test
    public void test1() {
        final Gson gson = CanvasRestAdapter.getGSONParser();
        final List<Bookmark> list = gson.fromJson(JSON, new TypeToken<List<Bookmark>>(){}.getType());

        for(Bookmark a : list) {
            assertNotNull(a);
            assertNotNull(a.getId());
            assertNotNull(a.getName());
            assertNotNull(a.getUrl());
            assertNotNull(a.getPosition());
        }
    }

    public String JSON = "[\n" +
            "{\n" +
            "\"id\": 1,\n" +
            "\"name\": \"Test that\",\n" +
            "\"position\": 1,\n" +
            "\"url\": \"https://mobiledev.instructure.com/courses/123456789/pages\",\n" +
            "\"data\": null\n" +
            "}\n" +
            "]";
}
