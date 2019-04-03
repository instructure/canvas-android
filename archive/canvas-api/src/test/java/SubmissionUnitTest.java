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
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.SubmissionComment;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricGradleTestRunner.class)
public class SubmissionUnitTest extends Assert{

    @Test
    public void testSubmission() {

        Gson gson = CanvasRestAdapter.getGSONParser();
        Submission submission = gson.fromJson(submissionJson, Submission.class);

        assertNotNull(submission);

        assertTrue(submission.getId() > 0);

        assertNotNull(submission.getBody());

        assertNotNull(submission.getGrade());

        assertTrue(submission.getScore() > 0);

        assertNotNull(submission.getPreviewUrl());

        assertNotNull(submission.getSubmissionType());

        assertNotNull(submission.getUrl());

        assertNotNull(submission.getWorkflowState());

        assertNotNull(submission.getComments());

        SubmissionComment comment = submission.getComments().get(0);
        isCommentValid(comment);

        assertNotNull(submission.getAttachments());
        Attachment attachment = submission.getAttachments().get(0);
        isValidAttachment(attachment);
    }

    public static void isCommentValid(SubmissionComment submissionComment){
        assertNotNull(submissionComment.getCreatedAt());

        assertNotNull(submissionComment.getAuthor());

        assertNotNull(submissionComment.getAuthorName());

        assertNotNull(submissionComment.getComment());

        assertTrue(submissionComment.getAuthorID() > 0);
    }

    public static void isValidAttachment(Attachment attachment) {

        assertNotNull(attachment);

        assertTrue(attachment.getId() > 0);

        assertNotNull(attachment.getDisplayName());

        assertNotNull(attachment.getThumbnailUrl());

        assertNotNull(attachment.getFilename());

        assertNotNull(attachment.getMimeType());

        assertNotNull(attachment.getUrl());
    }

    String submissionJson = "{\n" +
            "\"assignment_id\": 2241864,\n" +
            "\"attempt\": 67,\n" +
            "\"body\": \"Hi\",\n" +
            "\"grade\": \"7\",\n" +
            "\"grade_matches_current_submission\": true,\n" +
            "\"graded_at\": \"2013-10-10T22:49:28Z\",\n" +
            "\"grader_id\": 170000003356518,\n" +
            "\"id\": 10186331,\n" +
            "\"score\": 7,\n" +
            "\"submission_type\": \"online_url\",\n" +
            "\"submitted_at\": \"2013-09-12T19:47:21Z\",\n" +
            "\"url\": \"http://Google.com\",\n" +
            "\"user_id\": 3360251,\n" +
            "\"workflow_state\": \"graded\",\n" +
            "\"late\": true,\n" +
            "\"preview_url\": \"https://mobiledev.instructure.com/courses/833052/assignments/2241864/submissions/3360251?preview=1\",\n" +
            "\"attachments\": [\n" +
            "{\n" +
            "\"id\": 41967377,\n" +
            "\"content-type\": \"image/png\",\n" +
            "\"display_name\": \"websnappr20130418-12214-1gq3ocy.png\",\n" +
            "\"filename\": \"websnappr20130418-12214-1gq3ocy.png\",\n" +
            "\"url\": \"https://mobiledev.instructure.com/files/41967377/download?download_frd=1&verifier=NmzxhKhq1yI7tvQssfiFj968UPMVNisxxqoM2c3h\",\n" +
            "\"size\": 46640,\n" +
            "\"created_at\": \"2013-09-12T19:47:25Z\",\n" +
            "\"updated_at\": \"2013-09-12T19:47:25Z\",\n" +
            "\"unlock_at\": null,\n" +
            "\"locked\": false,\n" +
            "\"hidden\": false,\n" +
            "\"lock_at\": null,\n" +
            "\"hidden_for_user\": false,\n" +
            "\"thumbnail_url\": \"https://instructure-uploads.s3.amazonaws.com/thumbnails/32243761/websnappr20130418-12214-1gq3ocy_thumb.png?AWSAccessKeyId=AKIAJBQ7MOX3B5WFZGBA&Expires=1381963822&Signature=k%2FgYsnnQzqtkC7khLocgI%2BpkDxw%3D\",\n" +
            "\"locked_for_user\": false\n" +
            "}\n" +
            "],\n" +
            "\"submission_comments\": [\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"wow@gmail.com\",\n" +
            "\"comment\": \"Here's a video of an app that my friend and I made. It's awesome!!!\",\n" +
            "\"created_at\": \"2012-10-09T01:56:38Z\",\n" +
            "\"id\": 2309112,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady L\",\n" +
            "\"comment\": \"@#$%&-+(0<>\",\n" +
            "\"created_at\": \"2013-03-08T23:14:15Z\",\n" +
            "\"id\": 3285506,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady L\",\n" +
            "\"comment\": \"boo\",\n" +
            "\"created_at\": \"2013-03-29T19:52:20Z\",\n" +
            "\"id\": 3452510,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady L\",\n" +
            "\"comment\": \"Hi\",\n" +
            "\"created_at\": \"2013-05-06T23:10:37Z\",\n" +
            "\"id\": 3756966,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady L\",\n" +
            "\"comment\": \"Hi\",\n" +
            "\"created_at\": \"2013-05-06T23:24:14Z\",\n" +
            "\"id\": 3757100,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "},\n" +
            "{\n" +
            "\"author_id\": 3360251,\n" +
            "\"author_name\": \"Brady L\",\n" +
            "\"comment\": \"Check\",\n" +
            "\"created_at\": \"2013-05-09T20:36:50Z\",\n" +
            "\"id\": 3782691,\n" +
            "\"avatar_path\": \"/images/users/3360251-639eed86c4\",\n" +
            "\"author\": {\n" +
            "\"id\": 3360251,\n" +
            "\"display_name\": \"Brady L\",\n" +
            "\"avatar_image_url\": \"https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz\",\n" +
            "\"html_url\": \"https://mobiledev.instructure.com/courses/833052/users/3360251\"\n" +
            "}\n" +
            "}\n" +
            "]\n" +
            "}";

}
