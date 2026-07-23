/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.offline

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class InlineRceMediaTest {

    @Test
    fun `findLocalImageSrcs returns empty for null, blank, and no-image html`() {
        assertTrue(InlineRceMedia.findLocalImageSrcs(null).isEmpty())
        assertTrue(InlineRceMedia.findLocalImageSrcs("").isEmpty())
        assertTrue(InlineRceMedia.findLocalImageSrcs("<p>hello</p>").isEmpty())
    }

    @Test
    fun `findLocalImageSrcs ignores already-uploaded https images`() {
        val html = """<p>hi</p><img src="https://canvas.example.com/files/1/download" alt="a">"""
        assertTrue(InlineRceMedia.findLocalImageSrcs(html).isEmpty())
    }

    @Test
    fun `findLocalImageSrcs returns each distinct local src in order`() {
        val html = """
            <img src="file:///data/a.jpg" alt="a">
            <p>middle</p>
            <img alt="b" src="file:///data/b.jpg">
            <img src="https://canvas.example.com/files/9">
        """.trimIndent()

        assertEquals(
            listOf("file:///data/a.jpg", "file:///data/b.jpg"),
            InlineRceMedia.findLocalImageSrcs(html)
        )
    }

    @Test
    fun `rewriteLocalImageSrc swaps only the matching src and preserves the rest of the tag`() {
        val html = """<img src="file:///data/a.jpg" alt="my photo">"""

        val result = InlineRceMedia.rewriteLocalImageSrc(html, "file:///data/a.jpg", "https://canvas/files/7")

        assertEquals("""<img src="https://canvas/files/7" alt="my photo">""", result)
    }

    @Test
    fun `rewriteLocalImageSrc rewrites one image and leaves the other pending`() {
        val html = """<img src="file:///data/a.jpg"><img src="file:///data/b.jpg">"""

        val result = InlineRceMedia.rewriteLocalImageSrc(html, "file:///data/a.jpg", "https://canvas/files/7")

        assertEquals("""<img src="https://canvas/files/7"><img src="file:///data/b.jpg">""", result)
        assertEquals(listOf("file:///data/b.jpg"), InlineRceMedia.findLocalImageSrcs(result))
    }

    @Test
    fun `rewriteLocalImageSrc rewrites a src with whitespace around the equals sign`() {
        val html = """<img src = "file:///data/a.jpg" alt="a">"""

        val result = InlineRceMedia.rewriteLocalImageSrc(html, "file:///data/a.jpg", "https://canvas/files/7")

        assertEquals("""<img src="https://canvas/files/7" alt="a">""", result)
        assertTrue(InlineRceMedia.findLocalImageSrcs(result).isEmpty())
    }

    @Test
    fun `rewriteLocalImageSrc is a no-op when the src is already rewritten`() {
        val html = """<img src="https://canvas/files/7" alt="a">"""

        val result = InlineRceMedia.rewriteLocalImageSrc(html, "file:///data/a.jpg", "https://canvas/files/7")

        assertEquals(html, result)
    }
}
