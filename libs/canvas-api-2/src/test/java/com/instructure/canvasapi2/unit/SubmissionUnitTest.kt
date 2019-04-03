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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class SubmissionUnitTest : Assert() {

    @Test
    fun testSubmission() {
        val submission: Submission = submissionJson.parse()

        Assert.assertNotNull(submission)
        Assert.assertTrue(submission.id > 0)
        Assert.assertNotNull(submission.body)
        Assert.assertNotNull(submission.grade)
        Assert.assertTrue(submission.score > 0)
        Assert.assertNotNull(submission.previewUrl)
        Assert.assertNotNull(submission.submissionType)
        Assert.assertNotNull(submission.url)
        Assert.assertNotNull(submission.workflowState)
        Assert.assertNotNull(submission.submissionComments)

        val comment = submission.submissionComments[0]
        isCommentValid(comment)

        Assert.assertNotNull(submission.attachments)
        val attachment = submission.attachments[0]
        isValidAttachment(attachment)
    }

    private fun isCommentValid(submissionComment: SubmissionComment) {
        Assert.assertNotNull(submissionComment.createdAt)
        Assert.assertNotNull(submissionComment.author)
        Assert.assertNotNull(submissionComment.authorName)
        Assert.assertNotNull(submissionComment.comment)
        Assert.assertTrue(submissionComment.authorId > 0)
    }

    private fun isValidAttachment(attachment: Attachment) {
        Assert.assertNotNull(attachment)
        Assert.assertTrue(attachment.id > 0)
        Assert.assertNotNull(attachment.displayName)
        Assert.assertNotNull(attachment.thumbnailUrl)
        Assert.assertNotNull(attachment.filename)
        Assert.assertNotNull(attachment.contentType)
        Assert.assertNotNull(attachment.url)
    }

    @Language("JSON")
    private var submissionJson = """
      {
        "assignment_id": 2241864,
        "attempt": 67,
        "body": "Hi",
        "grade": "7",
        "grade_matches_current_submission": true,
        "graded_at": "2013-10-10T22:49:28Z",
        "grader_id": 170000003356518,
        "id": 10186331,
        "score": 7,
        "submission_type": "online_url",
        "submitted_at": "2013-09-12T19:47:21Z",
        "url": "http://Google.com",
        "user_id": 3360251,
        "workflow_state": "graded",
        "late": true,
        "preview_url": "https://mobiledev.instructure.com/courses/833052/assignments/2241864/submissions/3360251?preview=1",
        "attachments": [
          {
            "id": 41967377,
            "content-type": "image/png",
            "display_name": "websnappr20130418-12214-1gq3ocy.png",
            "filename": "websnappr20130418-12214-1gq3ocy.png",
            "url": "https://mobiledev.instructure.com/files/41967377/download?download_frd=1&verifier=NmzxhKhq1yI7tvQssfiFj968UPMVNisxxqoM2c3h",
            "size": 46640,
            "created_at": "2013-09-12T19:47:25Z",
            "updated_at": "2013-09-12T19:47:25Z",
            "unlock_at": null,
            "locked": false,
            "hidden": false,
            "lock_at": null,
            "hidden_for_user": false,
            "thumbnail_url": "https://instructure-uploads.s3.amazonaws.com/thumbnails/32243761/websnappr20130418-12214-1gq3ocy_thumb.png?AWSAccessKeyId=AKIAJBQ7MOX3B5WFZGBA&Expires=1381963822&Signature=k%2FgYsnnQzqtkC7khLocgI%2BpkDxw%3D",
            "locked_for_user": false
          }
        ],
        "submission_comments": [
          {
            "author_id": 3360251,
            "author_name": "wow@gmail.com",
            "comment": "Here's a video of an app that my friend and I made. It's awesome!!!",
            "created_at": "2012-10-09T01:56:38Z",
            "id": 2309112,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          },
          {
            "author_id": 3360251,
            "author_name": "Brady L",
            "comment": "@#$%&-+(0<>",
            "created_at": "2013-03-08T23:14:15Z",
            "id": 3285506,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          },
          {
            "author_id": 3360251,
            "author_name": "Brady L",
            "comment": "boo",
            "created_at": "2013-03-29T19:52:20Z",
            "id": 3452510,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          },
          {
            "author_id": 3360251,
            "author_name": "Brady L",
            "comment": "Hi",
            "created_at": "2013-05-06T23:10:37Z",
            "id": 3756966,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          },
          {
            "author_id": 3360251,
            "author_name": "Brady L",
            "comment": "Hi",
            "created_at": "2013-05-06T23:24:14Z",
            "id": 3757100,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          },
          {
            "author_id": 3360251,
            "author_name": "Brady L",
            "comment": "Check",
            "created_at": "2013-05-09T20:36:50Z",
            "id": 3782691,
            "avatar_path": "/images/users/3360251-639eed86c4",
            "author": {
              "id": 3360251,
              "display_name": "Brady L",
              "avatar_image_url": "https://mobiledev.instructure.com/files/38549060/download?download_frd=1&verifier=L4h3xgu1bA5Usf7M1WgqQmMJaJJNa6C303N6LlXz",
              "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
            }
          }
        ]
      }"""

}
