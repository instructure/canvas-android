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
import com.instructure.canvasapi.model.Page;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class PageUnitTest extends Assert{

    @Test
    public void testPage() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Page page = gson.fromJson(pageJSON, Page.class);

        assertNotNull(page);

        assertEquals(page.getBody(), "body");

        assertNotNull(page.getCreate_at());

        assertEquals(page.getTitle(), "Front Page");

        assertNotNull(page.getUpdated_at());

        assertEquals(page.getUrl(), "front-page");

        assertFalse(page.isHide_from_students());
    }

    String pageJSON = "{\"created_at\":\"2011-01-10T08:26:38-07:00\",\"editing_roles\":\"teachers\",\"hide_from_students\":false,\"title\":\"Front Page\",\"updated_at\":\"2013-05-29T13:23:43-06:00\",\"url\":\"front-page\",\"last_edited_by\":{\"id\":170000003828513,\"display_name\":\"Derrick Hathaway\",\"avatar_image_url\":\"https://secure.gravatar.com/avatar/1753d19b1ddf16cb0a31d983f97f4488?s=50\\u0026d=https%3A%2F%2Fmobiledev.instructure.com%2Fimages%2Fdotted_pic.png\",\"html_url\":\"https://mobiledev.instructure.com/courses/24219/users/17~3828513\"},\"published\":true,\"front_page\":true,\"html_url\":\"https://mobiledev.instructure.com/courses/24219/wiki/front-page\",\"locked_for_user\":false,\"body\":\"body\"}";

}
