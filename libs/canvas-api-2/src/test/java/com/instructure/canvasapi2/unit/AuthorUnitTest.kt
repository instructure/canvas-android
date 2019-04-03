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

import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AuthorUnitTest : Assert() {

    @Test
    fun testAuthor() {
        val author: Author = authorJSON.parse()
        Assert.assertNotNull(author)
        Assert.assertNotNull(author.id)
        Assert.assertNotNull(author.displayName)
        Assert.assertNotNull(author.avatarImageUrl)
        Assert.assertNotNull(author.htmlUrl)
    }

    @Language("JSON")
    private val authorJSON = """
      {
        "id": 3360251,
        "display_name": "Brady BobLaw",
        "avatar_image_url": "https://mobiledev.instructure.com/files/65129556/download?download_frd=1&verifier=7fiex2XkIhokFK3jkFljObf5aj2QACgnG",
        "html_url": "https://mobiledev.instructure.com/courses/12345/users/123455"
      }"""

}
