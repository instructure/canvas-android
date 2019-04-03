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
import com.instructure.canvasapi.model.Author;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class AuthorUnitTest extends Assert {

    @Test
    public void testAuthor() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        Author author = gson.fromJson(authorJSON, Author.class);

        assertNotNull(author);

        assertNotNull(author.getId());
        assertNotNull(author.getDisplayName());
        assertNotNull(author.getAvatarImageUrl());
        assertNotNull(author.getHtmlUrl());
    }

    public static final String authorJSON = "{"
                +"\"id\": 3360251,"
                +"\"display_name\": \"Brady BobLaw\","
                +"\"avatar_image_url\": \"https://mobiledev.instructure.com/files/65129556/download?download_frd=1&verifier=7fiex2XkIhokFK3jkFljObf5aj2QACgnG\","
                +"\"html_url\": \"https://mobiledev.instructure.com/courses/12345/users/123455\""
            +"}";

}
