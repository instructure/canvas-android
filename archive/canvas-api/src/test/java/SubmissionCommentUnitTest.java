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
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class SubmissionCommentUnitTest extends Assert {

    @Test
    public void testSubmissionComment() {
        Gson gson = CanvasRestAdapter.getGSONParser();
        SubmissionComment submissionComment = gson.fromJson(submissionCommentJSON, SubmissionComment.class);

        assertNotNull(submissionComment);

        assertNotNull(submissionComment.getAuthor());
        assertNotNull(submissionComment.getAuthorName());
        assertNotNull(submissionComment.getComment());
        assertNotNull(submissionComment.getCreatedAt());
        assertTrue(submissionComment.getAuthor().getId() > 0);
    }

    String submissionCommentJSON =
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady\",\n" +
            "\"comment\": \"This is a media comment.\",\n" +
            "\"created_at\": \"2015-03-16T22:38:41Z\",\n" +
            "\"id\": 10582507,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"media_comment\": {\n" +
            "\"content-type\": \"video/mp4\",\n" +
            "\"display_name\": null,\n" +
            "\"media_id\": \"m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa\",\n" +
            "\"media_type\": \"video\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa&redirect=1&type=mp4\"\n" +
            "},\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/65129556/download?download_frd=1&verifier=7fiex2XkIhokblO7lUCNA85cKfjObf5aj2QACgnG\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "}";
}
