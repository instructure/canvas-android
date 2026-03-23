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
 *
 */

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class StudioOfflineVideoHelperTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = mockk()
    private val apiPrefs: ApiPrefs = mockk()

    private lateinit var helper: StudioOfflineVideoHelper

    @Before
    fun setup() {
        every { context.filesDir } returns tempFolder.root
        every { apiPrefs.user } returns User(id = 42)
        helper = StudioOfflineVideoHelper(context, apiPrefs)
    }

    // region getStudioMediaId

    @Test
    fun `getStudioMediaId returns media id from standard LTI launch URL`() {
        val url = "https://example.instructuremedia.com/lti/launch?custom_arc_launch_type=embed&custom_arc_media_id=abc-123&custom_arc_start_at=0"
        assertEquals("abc-123", helper.getStudioMediaId(url))
    }

    @Test
    fun `getStudioMediaId returns media id when it is the first parameter`() {
        val url = "https://example.com/lti/launch?custom_arc_media_id=first-param"
        assertEquals("first-param", helper.getStudioMediaId(url))
    }

    @Test
    fun `getStudioMediaId returns media id when it is the last parameter`() {
        val url = "https://example.com/lti/launch?custom_arc_launch_type=embed&custom_arc_media_id=last-param"
        assertEquals("last-param", helper.getStudioMediaId(url))
    }

    @Test
    fun `getStudioMediaId returns null for URL without media id`() {
        val url = "https://example.com/lti/launch?custom_arc_launch_type=embed&custom_arc_start_at=0"
        assertNull(helper.getStudioMediaId(url))
    }

    @Test
    fun `getStudioMediaId returns null for null input`() {
        assertNull(helper.getStudioMediaId(null))
    }

    @Test
    fun `getStudioMediaId returns null for empty string`() {
        assertNull(helper.getStudioMediaId(""))
    }

    @Test
    fun `getStudioMediaId returns null for non-Studio external URL`() {
        val url = "https://example.com/some-external-tool?param=value"
        assertNull(helper.getStudioMediaId(url))
    }

    @Test
    fun `getStudioMediaId handles UUID-style media ids`() {
        val url = "https://example.com/lti/launch?custom_arc_media_id=e4be8b75-1234-5678-9abc-def012345678&custom_arc_start_at=0"
        assertEquals("e4be8b75-1234-5678-9abc-def012345678", helper.getStudioMediaId(url))
    }

    // endregion

    // region isStudioVideoAvailableOffline

    @Test
    fun `isStudioVideoAvailableOffline returns true when video file exists`() {
        val mediaId = "test-media-id"
        createVideoFile(mediaId)
        assertTrue(helper.isStudioVideoAvailableOffline(mediaId))
    }

    @Test
    fun `isStudioVideoAvailableOffline returns false when video file does not exist`() {
        assertFalse(helper.isStudioVideoAvailableOffline("nonexistent-id"))
    }

    @Test
    fun `isStudioVideoAvailableOffline returns false when directory exists but file does not`() {
        val mediaId = "dir-only"
        File(tempFolder.root, "42/studio/$mediaId").mkdirs()
        assertFalse(helper.isStudioVideoAvailableOffline(mediaId))
    }

    // endregion

    // region getStudioVideoUri

    @Test
    fun `getStudioVideoUri returns correct file URI`() {
        val mediaId = "video-id"
        createVideoFile(mediaId)
        val expected = "file://${tempFolder.root.absolutePath}/42/studio/$mediaId/$mediaId.mp4"
        assertEquals(expected, helper.getStudioVideoUri(mediaId))
    }

    // endregion

    // region getStudioPosterUri

    @Test
    fun `getStudioPosterUri returns file URI when poster exists`() {
        val mediaId = "poster-id"
        createPosterFile(mediaId)
        val expected = "file://${tempFolder.root.absolutePath}/42/studio/$mediaId/poster.jpg"
        assertEquals(expected, helper.getStudioPosterUri(mediaId))
    }

    @Test
    fun `getStudioPosterUri returns null when poster does not exist`() {
        val mediaId = "no-poster-id"
        createVideoFile(mediaId)
        assertNull(helper.getStudioPosterUri(mediaId))
    }

    @Test
    fun `getStudioPosterUri returns null when directory does not exist`() {
        assertNull(helper.getStudioPosterUri("nonexistent"))
    }

    // endregion

    // region user id handling

    @Test
    fun `uses correct user directory from ApiPrefs`() {
        every { apiPrefs.user } returns User(id = 99)
        val newHelper = StudioOfflineVideoHelper(context, apiPrefs)
        val mediaId = "user-test"

        val dir = File(tempFolder.root, "99/studio/$mediaId")
        dir.mkdirs()
        File(dir, "$mediaId.mp4").createNewFile()

        assertTrue(newHelper.isStudioVideoAvailableOffline(mediaId))
    }

    // endregion

    private fun createVideoFile(mediaId: String) {
        val dir = File(tempFolder.root, "42/studio/$mediaId")
        dir.mkdirs()
        File(dir, "$mediaId.mp4").createNewFile()
    }

    private fun createPosterFile(mediaId: String) {
        val dir = File(tempFolder.root, "42/studio/$mediaId")
        dir.mkdirs()
        File(dir, "poster.jpg").createNewFile()
    }
}