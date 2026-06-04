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

package com.instructure.pandautils.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUtilsTest {

    @Test
    fun `mimeClass audio returns true`() {
        assertTrue(shouldOpenMediaInternally(null, null, "audio"))
    }

    @Test
    fun `mimeClass video returns true`() {
        assertTrue(shouldOpenMediaInternally(null, null, "video"))
    }

    @Test
    fun `mimeClass is case and whitespace insensitive`() {
        assertTrue(shouldOpenMediaInternally(null, null, "  Video  "))
        assertTrue(shouldOpenMediaInternally(null, null, "AUDIO"))
    }

    @Test
    fun `mimeClass image returns false`() {
        assertFalse(shouldOpenMediaInternally(null, null, "image"))
    }

    @Test
    fun `mimeType audio prefix returns true`() {
        assertTrue(shouldOpenMediaInternally(null, "audio/mpeg", null))
    }

    @Test
    fun `mimeType video prefix returns true`() {
        assertTrue(shouldOpenMediaInternally(null, "video/mp4", null))
    }

    @Test
    fun `mimeType dash returns true`() {
        assertTrue(shouldOpenMediaInternally(null, "application/dash+xml", null))
    }

    @Test
    fun `mimeType is case and whitespace insensitive`() {
        assertTrue(shouldOpenMediaInternally(null, "  Video/MP4  ", null))
    }

    @Test
    fun `mimeType pdf returns false`() {
        assertFalse(shouldOpenMediaInternally(null, "application/pdf", null))
    }

    @Test
    fun `url with mp4 extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.mp4", null, null))
    }

    @Test
    fun `url with mpd extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.mpd", null, null))
    }

    @Test
    fun `url with m3u8 extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.m3u8", null, null))
    }

    @Test
    fun `url with mp3 extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.mp3", null, null))
    }

    @Test
    fun `url with m4a extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.m4a", null, null))
    }

    @Test
    fun `url with webm extension returns true`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.webm", null, null))
    }

    @Test
    fun `url ending with cmaf segment returns true`() {
        assertTrue(shouldOpenMediaInternally("https://notorious.example/asset/123/cmaf", null, null))
    }

    @Test
    fun `url with query string is still matched by extension`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.mp4?token=abc", null, null))
    }

    @Test
    fun `url with query string is still matched by cmaf`() {
        assertTrue(shouldOpenMediaInternally("https://notorious.example/asset/123/cmaf?token=abc", null, null))
    }

    @Test
    fun `url with extension is case insensitive`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/File.MP4", null, null))
    }

    @Test
    fun `url with pdf extension returns false`() {
        assertFalse(shouldOpenMediaInternally("https://example.com/file.pdf", null, null))
    }

    @Test
    fun `all null or blank returns false`() {
        assertFalse(shouldOpenMediaInternally(null, null, null))
        assertFalse(shouldOpenMediaInternally("", "", ""))
        assertFalse(shouldOpenMediaInternally("   ", "   ", "   "))
    }

    @Test
    fun `mimeClass takes priority over non-matching mimeType and url`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.pdf", "application/pdf", "audio"))
    }

    @Test
    fun `mimeType takes over non-matching url when mimeClass missing`() {
        assertTrue(shouldOpenMediaInternally("https://example.com/file.pdf", "video/mp4", null))
    }
}