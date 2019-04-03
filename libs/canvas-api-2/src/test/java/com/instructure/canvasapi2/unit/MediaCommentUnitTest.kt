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

import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class MediaCommentUnitTest : Assert() {

    @Test
    fun testMediaComment() {
        val mediaComment: MediaComment = mediaCommentJSON.parse()

        Assert.assertNotNull(mediaComment)
        Assert.assertNotNull(mediaComment.mediaType)
        Assert.assertNotNull(mediaComment.mediaId)
        Assert.assertNotNull(mediaComment.contentType)
        Assert.assertNotNull(mediaComment.url)
    }

    @Language("JSON")
    private var mediaCommentJSON = """
      {
        "content-type": "video/mp4",
        "display_name": null,
        "media_id": "m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa",
        "media_type": "video",
        "url": "https://mobiledev.instructure.com/users/3360251/media_download?entryId=m-C8q5qs5QXaR13VzeBwDoFWwn896LpZa&redirect=1&type=mp4"
      }"""

}
