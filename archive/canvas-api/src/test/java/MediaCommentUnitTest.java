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
import com.instructure.canvasapi.model.MediaComment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class MediaCommentUnitTest extends Assert {

    @Test
    public void testMediaComment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        MediaComment mediaComment = gson.fromJson(mediaCommentJSON, MediaComment.class);

        assertNotNull(mediaComment);

        assertNotNull(mediaComment.getMediaType());
        assertNotNull(mediaComment.getMediaId());
        assertNotNull(mediaComment.getMimeType());
        assertNotNull(mediaComment.getUrl());
    }

    String mediaCommentJSON =
           "{\n" +
            "\"content-type\": \"video/mp4\",\n" +
            "\"display_name\": null,\n" +
            "\"media_id\": \"m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa\",\n" +
            "\"media_type\": \"video\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa&redirect=1&type=mp4\"\n" +
            "}";

}
