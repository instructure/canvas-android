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

import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class SubmissionCommentUnitTest : Assert() {

    @Test
    fun testSubmissionComment() {
        val submissionComment: SubmissionComment = submissionCommentJSON.parse()

        Assert.assertNotNull(submissionComment)
        Assert.assertNotNull(submissionComment.author)
        Assert.assertNotNull(submissionComment.authorName)
        Assert.assertNotNull(submissionComment.comment)
        Assert.assertNotNull(submissionComment.createdAt)
        Assert.assertTrue(submissionComment.author!!.id > 0)
    }

    @Language("JSON")
    private var submissionCommentJSON = """
      {
        "author_id": 3360251,
        "author_name": "Brady",
        "comment": "This is a media comment.",
        "created_at": "2015-03-16T22:38:41Z",
        "id": 10582507,
        "avatar_path": "/images/users/3360251-639eed86c4",
        "media_comment": {
          "content-type": "video/mp4",
          "display_name": null,
          "media_id": "m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa",
          "media_type": "video",
          "url": "https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa&redirect=1&type=mp4"
        },
        "author": {
          "id": 3360251,
          "display_name": "Brady",
          "avatar_image_url": "https://mobiledev.instructure.com/files/65129556/download?download_frd=1&verifier=7fiex2XkIhokblO7lUCNA85cKfjObf5aj2QACgnG",
          "html_url": "https://mobiledev.instructure.com/courses/833052/users/3360251"
        }
      }"""
}
