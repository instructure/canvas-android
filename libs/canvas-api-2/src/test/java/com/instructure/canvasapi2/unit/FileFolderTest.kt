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

import com.instructure.canvasapi2.models.FileFolder

import org.junit.Test

import org.junit.Assert.assertEquals

class FileFolderTest {

    @Test
    fun compareTo_FolderAndFile() {
        val folder = FileFolder(fullName = "fileFolder")
        val file = FileFolder(displayName = "fileFolder")

        assertEquals(-1, folder.compareTo(file).toLong())
    }

    @Test
    fun compareTo_FileAndFolder() {
        val folder = FileFolder(fullName = "fileFolder")
        val file = FileFolder(displayName = "fileFolder")

        assertEquals(1, file.compareTo(folder).toLong())
    }

    @Test
    fun compareTo_FolderAndFolder() {
        val folder1 = FileFolder(fullName = "Folder 1")
        val folder2 = FileFolder(fullName = "Folder 2")

        assertEquals(-1, folder1.compareTo(folder2).toLong())
    }

    @Test
    fun compareTo_FolderAndFolderReversed() {
        val folder1 = FileFolder(fullName = "Folder 1")
        val folder2 = FileFolder(fullName = "Folder 2")

        assertEquals(1, folder2.compareTo(folder1).toLong())
    }

    @Test
    fun compareTo_FileAndFile() {
        val file1 = FileFolder(fullName = "File 1")
        val file2 = FileFolder(fullName = "File 2")

        assertEquals(-1, file1.compareTo(file2).toLong())
    }

    @Test
    fun compareTo_FileAndFileReversed() {
        val file1 = FileFolder(fullName = "File 1")
        val file2 = FileFolder(fullName = "File 2")

        assertEquals(1, file2.compareTo(file1).toLong())
    }
}