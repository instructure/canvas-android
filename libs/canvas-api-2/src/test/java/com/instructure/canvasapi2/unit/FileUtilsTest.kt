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

import com.instructure.canvasapi2.utils.FileUtils

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.io.File
import java.io.IOException
import java.util.ArrayList

import org.junit.Assert.assertEquals

class FileUtilsTest {

    @Rule
    @JvmField
    val tempFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun deleteAllFilesInDirectory_null() {
        assertEquals(false, FileUtils.deleteAllFilesInDirectory(null))
    }

    @Test
    fun deleteAllFilesInDirectory_singleDir() {
        val rootDir = tempFolder.root
        FileUtils.deleteAllFilesInDirectory(rootDir)
        assertEquals(false, rootDir.exists())
    }

    @Test
    @Throws(IOException::class)
    fun deleteAllFilesInDirectory_nestedDirs() {
        val rootDir = tempFolder.root
        val testDirs = ArrayList<File>()
        val testFiles = ArrayList<File>()

        testDirs.add(rootDir)
        for (i in 0..4) {
            val dir = File(rootDir, "testDir$i")
            dir.mkdir()
            testDirs.add(dir)
        }

        for (dir in testDirs) {
            for (i in 0..4) {
                val file = File(dir, "testFile$i")
                file.createNewFile()
                testFiles.add(file)
            }
        }

        FileUtils.deleteAllFilesInDirectory(rootDir)

        testFiles.addAll(testDirs)
        for (file in testFiles) {
            assertEquals(false, file.exists())
        }
    }

    @Test
    fun getFileExtensionFromMimetype_null() {
        assertEquals("", FileUtils.getFileExtensionFromMimeType(null))
    }

    @Test
    fun getFileExtensionFromMimetype_validInput() {
        assertEquals("png", FileUtils.getFileExtensionFromMimeType("image/png"))
    }

    @Test
    fun getFileExtensionFromMimetype_invalidInput() {
        assertEquals("jpg", FileUtils.getFileExtensionFromMimeType("jpg"))
    }

    @Test
    fun notoriousCodeFromMimeType_null() {
        assertEquals("0", FileUtils.notoriousCodeFromMimeType(null))
    }

    @Test
    fun notoriousCodeFromMimeType_video() {
        assertEquals("1", FileUtils.notoriousCodeFromMimeType("video/mp4"))
    }

    @Test
    fun notoriousCodeFromMimeType_audio() {
        assertEquals("5", FileUtils.notoriousCodeFromMimeType("audio/mp3"))
    }

    @Test
    fun notoriousCodeFromMimeType_invalid() {
        assertEquals("0", FileUtils.notoriousCodeFromMimeType("pdf"))
    }

    @Test
    fun mediaTypeFromNotoriousCode_video() {
        assertEquals("video", FileUtils.mediaTypeFromNotoriousCode(1))
    }

    @Test
    fun mediaTypeFromNotoriousCode_audio() {
        assertEquals("audio", FileUtils.mediaTypeFromNotoriousCode(5))
    }

    @Test
    fun mediaTypeFromNotoriousCode_other() {
        assertEquals("", FileUtils.mediaTypeFromNotoriousCode(123))
    }
}
