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
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AttachmentUnitTest : Assert() {

    @Test
    fun test1() {
        val list: Array<Attachment> = JSON.parse()

        Assert.assertNotNull(list)

        val attachment = list[0]
        Assert.assertTrue(attachment.id > 0)
        Assert.assertNotNull(attachment.displayName)
        Assert.assertNotNull(attachment.filename)
        Assert.assertNotNull(attachment.url)
        Assert.assertNotNull(attachment.thumbnailUrl)
        Assert.assertNotNull(attachment.contentType)
    }

    @Language("JSON")
    internal val JSON = """
      [
        {
          "id": 52795562,
          "content-type": "image/jpeg",
          "display_name": "thankyou.jpg",
          "filename": "thankyou.jpg",
          "url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "size": 151357,
          "created_at": "2014-07-14T16:18:57Z",
          "updated_at": "2014-07-14T16:18:59Z",
          "unlock_at": null,
          "locked": false,
          "hidden": false,
          "lock_at": null,
          "hidden_for_user": false,
          "thumbnail_url": "http://www.dailystormer.com/wp-content/uploads/2014/05/1398802810722.jpeg",
          "locked_for_user": false
        }
      ]"""

}
