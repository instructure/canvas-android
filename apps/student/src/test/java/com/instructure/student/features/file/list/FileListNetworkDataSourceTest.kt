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

package com.instructure.student.features.file.list

import android.webkit.URLUtil
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.files.list.FileListNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileListNetworkDataSourceTest {

    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val fileListNetworkDataSource = FileListNetworkDataSource(fileFolderApi)

    @Before
    fun setup() {
        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) } returns true
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getFolders() calls with correct params`() = runTest {
        val expected = listOf(
            FileFolder(id = 1, name = "Folder 1", parentFolderId = 0),
            FileFolder(id = 2, name = "Folder 2", parentFolderId = 0),
            FileFolder(id = 3, name = "Folder 3", parentFolderId = 0)
        )

        coEvery { fileFolderApi.getFirstPageFolders(any(), any()) } returns DataResult.Success(expected)

        val actual = fileListNetworkDataSource.getFolders(0, true)

        coVerify {
            fileFolderApi.getFirstPageFolders(
                0,
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        }

        assertEquals(DataResult.Success(expected), actual)
    }

    @Test
    fun `getFiles() calls with correct params`() = runTest {
        val expected = listOf(
            FileFolder(id = 1, name = "File 1", folderId = 0),
            FileFolder(id = 2, name = "File 2", folderId = 0),
            FileFolder(id = 3, name = "File 3", folderId = 0)
        )

        coEvery { fileFolderApi.getFirstPageFiles(any(), any()) } returns DataResult.Success(expected)

        val actual = fileListNetworkDataSource.getFiles(0, true)

        coVerify {
            fileFolderApi.getFirstPageFiles(
                0,
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        }

        assertEquals(DataResult.Success(expected), actual)
    }

    @Test
    fun `getFolder calls with correct params`() = runTest {
        val expected = FileFolder(id = 1, name = "Folder 1", parentFolderId = 0)

        coEvery { fileFolderApi.getFolder(any(), any()) } returns DataResult.Success(expected)

        val actual = fileListNetworkDataSource.getFolder(1, true)

        coVerify { fileFolderApi.getFolder(1, RestParams(isForceReadFromNetwork = true)) }

        assertEquals(expected, actual)
    }

    @Test
    fun `getFolder() returns null on error`() = runTest {
        coEvery { fileFolderApi.getFolder(any(), any()) } returns DataResult.Fail()

        val actual = fileListNetworkDataSource.getFolder(1, true)

        assertNull(actual)
    }

    @Test
    fun `getRootFolderForContext() calls with correct params`() = runTest {
        val expected = FileFolder(id = 1, name = "Root", parentFolderId = 0)

        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Success(expected)

        val actual = fileListNetworkDataSource.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), true)

        coVerify {
            fileFolderApi.getRootFolderForContext(
                1,
                CanvasContext.Type.COURSE.apiString,
                RestParams(isForceReadFromNetwork = true)
            )
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `getRootFolderForContext() returns null on error`() = runTest {
        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Fail()

        val actual = fileListNetworkDataSource.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), true)

        assertNull(actual)
    }

    @Test
    fun `getNextPage() calls with correct params`() = runTest {
        val expected = listOf(
            FileFolder(id = 1, name = "File 1", folderId = 0),
            FileFolder(id = 2, name = "File 2", folderId = 0),
            FileFolder(id = 3, name = "File 3", folderId = 0)
        )

        coEvery { fileFolderApi.getNextPageFileFoldersList(any(), any()) } returns DataResult.Success(expected)

        val actual = fileListNetworkDataSource.getNextPage("next_url", true)

        coVerify {
            fileFolderApi.getNextPageFileFoldersList(
                "next_url",
                RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            )
        }

        assertEquals(DataResult.Success(expected), actual)
    }
}