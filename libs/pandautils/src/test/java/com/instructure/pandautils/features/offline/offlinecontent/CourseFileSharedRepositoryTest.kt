/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.offline.offlinecontent

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class CourseFileSharedRepositoryTest {

    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val repository = CourseFileSharedRepository(fileFolderApi)

    @Test
    fun `Return all course files and folders`() = runTest {
        val root = FileFolder(id = 1)
        val files = listOf(FileFolder(id = 2), FileFolder(id = 3))
        val folders = listOf(FileFolder(id = 4), FileFolder(id = 5))
        val subfolderFiles = listOf(FileFolder(id = 6), FileFolder(id = 7))

        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Success(root)
        coEvery { fileFolderApi.getFirstPageFiles(1, any()) } returns DataResult.Success(files)
        coEvery { fileFolderApi.getFirstPageFolders(1, any()) } returns DataResult.Success(folders)
        coEvery { fileFolderApi.getFirstPageFiles(4, any()) } returns DataResult.Success(subfolderFiles)
        coEvery { fileFolderApi.getFirstPageFolders(4, any()) } returns DataResult.Success(emptyList())
        coEvery { fileFolderApi.getFirstPageFiles(5, any()) } returns DataResult.Success(emptyList())
        coEvery { fileFolderApi.getFirstPageFolders(5, any()) } returns DataResult.Success(emptyList())

        val result = repository.getCourseFoldersAndFiles(1)

        Assert.assertEquals((listOf(root) + files + folders + subfolderFiles).sortedBy { it.id }, result.sortedBy { it.id })
    }

    @Test
    fun `Returns course files`() = runTest {
        val root = FileFolder(id = 1)
        val files = listOf(FileFolder(id = 2), FileFolder(id = 3))
        val folders = listOf(FileFolder(id = 4), FileFolder(id = 5))
        val subfolderFiles = listOf(FileFolder(id = 6), FileFolder(id = 7))

        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Success(root)
        coEvery { fileFolderApi.getFirstPageFiles(1, any()) } returns DataResult.Success(files)
        coEvery { fileFolderApi.getFirstPageFolders(1, any()) } returns DataResult.Success(folders)
        coEvery { fileFolderApi.getFirstPageFiles(4, any()) } returns DataResult.Success(subfolderFiles)
        coEvery { fileFolderApi.getFirstPageFolders(4, any()) } returns DataResult.Success(emptyList())
        coEvery { fileFolderApi.getFirstPageFiles(5, any()) } returns DataResult.Success(emptyList())
        coEvery { fileFolderApi.getFirstPageFolders(5, any()) } returns DataResult.Success(emptyList())

        val result = repository.getCourseFiles(1)

        Assert.assertEquals(files + subfolderFiles, result)
    }

    @Test
    fun `Hidden and locked files and folders are filtered`() = runTest {
        val root = FileFolder(id = 1)
        val files = listOf(
            FileFolder(id = 2),
            FileFolder(id = 3, isHidden = true),
            FileFolder(id = 4, isLocked = true),
            FileFolder(id = 5, isHiddenForUser = true),
            FileFolder(id = 6, isLockedForUser = true)
        )
        val folders = listOf(
            FileFolder(id = 7),
            FileFolder(id = 8, isHidden = true),
            FileFolder(id = 9, isLocked = true),
            FileFolder(id = 10, isHiddenForUser = true),
            FileFolder(id = 11, isLockedForUser = true)
        )
        val subFolderFiles = listOf(FileFolder(id = 12))

        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Success(root)
        coEvery { fileFolderApi.getFirstPageFiles(1, any()) } returns DataResult.Success(files)
        coEvery { fileFolderApi.getFirstPageFolders(1, any()) } returns DataResult.Success(folders)
        coEvery { fileFolderApi.getFirstPageFiles(7, any()) } returns DataResult.Success(subFolderFiles)
        coEvery { fileFolderApi.getFirstPageFolders(7, any()) } returns DataResult.Success(emptyList())

        val result = repository.getCourseFiles(1)

        Assert.assertEquals(files.subList(0, 1) + subFolderFiles, result)
    }

    @Test
    fun `Returns empty list when files request fails`() = runTest {
        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getCourseFiles(1)

        Assert.assertEquals(emptyList<FileFolder>(), result)
    }
}