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
package com.instructure.pandautils.features.file.upload.scanner

import android.app.ActivityManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DocumentScannerManagerTest {

    private val context: Context = mockk(relaxed = true)
    private val activityManager: ActivityManager = mockk(relaxed = true)

    private lateinit var documentScannerManager: DocumentScannerManager

    @Before
    fun setup() {
        every { context.getSystemService(Context.ACTIVITY_SERVICE) } returns activityManager
        documentScannerManager = DocumentScannerManager(context)
    }

    @Test
    fun `isDeviceSupported returns true when RAM is above threshold`() {
        val memoryInfo = ActivityManager.MemoryInfo()
        memoryInfo.totalMem = (2L * 1024 * 1024 * 1024) // 2 GB
        val slot = slot<ActivityManager.MemoryInfo>()
        every { activityManager.getMemoryInfo(capture(slot)) } answers {
            slot.captured.totalMem = memoryInfo.totalMem
        }

        assertTrue(documentScannerManager.isDeviceSupported())
    }

    @Test
    fun `isDeviceSupported returns false when RAM is below threshold`() {
        val memoryInfo = ActivityManager.MemoryInfo()
        memoryInfo.totalMem = (1L * 1024 * 1024 * 1024) // 1 GB
        val slot = slot<ActivityManager.MemoryInfo>()
        every { activityManager.getMemoryInfo(capture(slot)) } answers {
            slot.captured.totalMem = memoryInfo.totalMem
        }

        assertFalse(documentScannerManager.isDeviceSupported())
    }

    @Test
    fun `isDeviceSupported returns true when RAM is just above threshold`() {
        val slot = slot<ActivityManager.MemoryInfo>()
        every { activityManager.getMemoryInfo(capture(slot)) } answers {
            slot.captured.totalMem = (1.8 * 1024 * 1024 * 1024).toLong()
        }

        assertTrue(documentScannerManager.isDeviceSupported())
    }

    @Test
    fun `generateFileName follows expected format`() {
        val fileName = documentScannerManager.generateFileName()

        assertTrue(fileName.startsWith("Scanned_Document_"))
        assertTrue(fileName.endsWith(".pdf"))
    }

    @Test
    fun `handleScanResultFromIntent returns empty result for null intent`() {
        val result = documentScannerManager.handleScanResultFromIntent(null)

        assertNull(result.pdfUri)
        assertTrue(result.pageUris.isEmpty())
    }
}