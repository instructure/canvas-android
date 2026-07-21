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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

class HorizonPendingFileStagingTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = mockk(relaxed = true)
    private val contentResolver: ContentResolver = mockk(relaxed = true)
    private val uri: Uri = mockk(relaxed = true)

    private lateinit var staging: HorizonPendingFileStaging

    @Before
    fun setUp() {
        every { context.filesDir } returns tempFolder.root
        every { context.contentResolver } returns contentResolver
        staging = HorizonPendingFileStaging(context)
    }

    @Test
    fun `stage copies uri content into the action directory`() {
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("hello".toByteArray())

        val file = staging.stage(actionId = 7L, uri = uri, fileName = "doc.txt")

        assertTrue(file.exists())
        assertEquals("hello", file.readText())
        assertEquals(pendingRoot().resolve("7/doc.txt").absolutePath, file.absolutePath)
    }

    @Test(expected = IOException::class)
    fun `stage throws when the content provider stream cannot be opened`() {
        every { contentResolver.openInputStream(uri) } returns null

        staging.stage(actionId = 7L, uri = uri, fileName = "doc.txt")
    }

    @Test
    fun `filesFor enumerates staged files and is empty when nothing staged`() {
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("a".toByteArray())
        staging.stage(actionId = 7L, uri = uri, fileName = "doc.txt")

        assertEquals(listOf("doc.txt"), staging.filesFor(7L).map { it.name })
        assertTrue(staging.filesFor(99L).isEmpty())
    }

    @Test
    fun `delete removes only the target action directory`() {
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("a".toByteArray()) andThen ByteArrayInputStream("b".toByteArray())
        staging.stage(actionId = 7L, uri = uri, fileName = "a.txt")
        staging.stage(actionId = 8L, uri = uri, fileName = "b.txt")

        staging.delete(7L)

        assertFalse(pendingRoot().resolve("7").exists())
        assertTrue(pendingRoot().resolve("8").exists())
    }

    @Test
    fun `deleteOrphans drops directories whose action is not active`() {
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("a".toByteArray()) andThen ByteArrayInputStream("b".toByteArray())
        staging.stage(actionId = 7L, uri = uri, fileName = "a.txt")
        staging.stage(actionId = 8L, uri = uri, fileName = "b.txt")
        File(pendingRoot(), "junk").mkdirs()

        staging.deleteOrphans(activeActionIds = setOf(7L))

        assertTrue(pendingRoot().resolve("7").exists())
        assertFalse(pendingRoot().resolve("8").exists())
        assertFalse(pendingRoot().resolve("junk").exists())
    }

    @Test
    fun `inline staging uses a separate root, isolated from the pending-action root`() {
        every { contentResolver.openInputStream(uri) } returns
            ByteArrayInputStream("action".toByteArray()) andThen ByteArrayInputStream("inline".toByteArray())
        staging.stage(actionId = 7L, uri = uri, fileName = "a.txt")
        val inlineFile = staging.stageInline(key = "1_10", uri = uri, fileName = "img.jpg")

        assertEquals("inline", inlineFile.readText())
        assertEquals(inlineRoot().resolve("1_10/img.jpg").absolutePath, inlineFile.absolutePath)
        assertEquals(listOf("img.jpg"), staging.inlineFilesFor("1_10").map { it.name })
        assertTrue(staging.inlineFilesFor("2_20").isEmpty())
    }

    @Test
    fun `deleteOrphans for pending actions does not touch the inline root`() {
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream("x".toByteArray())
        staging.stageInline(key = "1_10", uri = uri, fileName = "img.jpg")

        staging.deleteOrphans(activeActionIds = emptySet())

        assertTrue(inlineRoot().resolve("1_10").exists())
    }

    @Test
    fun `deleteInline removes only the target inline key`() {
        every { contentResolver.openInputStream(uri) } returns
            ByteArrayInputStream("a".toByteArray()) andThen ByteArrayInputStream("b".toByteArray())
        staging.stageInline(key = "1_10", uri = uri, fileName = "a.jpg")
        staging.stageInline(key = "1_20", uri = uri, fileName = "b.jpg")

        staging.deleteInline("1_10")

        assertFalse(inlineRoot().resolve("1_10").exists())
        assertTrue(inlineRoot().resolve("1_20").exists())
    }

    @Test
    fun `deleteInlineOrphans keeps active keys and drops the rest`() {
        every { contentResolver.openInputStream(uri) } returns
            ByteArrayInputStream("a".toByteArray()) andThen ByteArrayInputStream("b".toByteArray())
        staging.stageInline(key = "1_10", uri = uri, fileName = "a.jpg")
        staging.stageInline(key = "1_20", uri = uri, fileName = "b.jpg")

        staging.deleteInlineOrphans(activeKeys = setOf("1_10"))

        assertTrue(inlineRoot().resolve("1_10").exists())
        assertFalse(inlineRoot().resolve("1_20").exists())
    }

    private fun pendingRoot() = File(tempFolder.root, "horizon-pending")

    private fun inlineRoot() = File(tempFolder.root, "horizon-pending-inline")
}
