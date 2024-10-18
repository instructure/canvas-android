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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.student.features.files.list.FileListLocalDataSource
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
import java.util.Date

class FileListLocalDataSourceTest {

    private val fileFolderDao: FileFolderDao = mockk(relaxed = true)
    private val localFileDao: LocalFileDao = mockk(relaxed = true)

    private val fileListLocalDataSource = FileListLocalDataSource(
        fileFolderDao,
        localFileDao
    )

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
    fun `getFolders returns api models`() = runTest {
        val expected = listOf(
            FileFolder(id = 1, name = "Folder 1", parentFolderId = 0),
            FileFolder(id = 2, name = "Folder 2", parentFolderId = 0),
            FileFolder(id = 3, name = "Folder 3", parentFolderId = 0)
        )
        coEvery { fileFolderDao.findVisibleFoldersByParentId(any()) } returns expected.map { FileFolderEntity(it) }

        val result = fileListLocalDataSource.getFolders(0, true)

        coVerify {
            fileFolderDao.findVisibleFoldersByParentId(0)
        }

        assertEquals(DataResult.Success(expected), result)
    }

    @Test
    fun `getFiles replaces url with path`() = runTest {
        val files = listOf(
            FileFolder(id = 1, name = "File 1", url = "url_1", thumbnailUrl = "thumbnail_url_1"),
            FileFolder(id = 2, name = "File 2", url = "url_2", thumbnailUrl = "thumbnail_url_2"),
            FileFolder(id = 3, name = "File 3", url = "url_3", thumbnailUrl = "thumbnail_url_3")
        )

        val localFiles = listOf(
            LocalFileEntity(id = 1, courseId = 1, createdDate = Date(), path = "path_1"),
            LocalFileEntity(id = 2, courseId = 1, createdDate = Date(), path = "path_2"),
            LocalFileEntity(id = 3, courseId = 1, createdDate = Date(), path = "path_3")
        )

        coEvery { localFileDao.findByIds(any()) } returns localFiles
        coEvery { fileFolderDao.findVisibleFilesByFolderId(any()) } returns files.map { FileFolderEntity(it) }

        val expected = files.map { it.copy(url = "path_${it.id}", thumbnailUrl = null) }
        val result = fileListLocalDataSource.getFiles(0, true)

        coVerify {
            localFileDao.findByIds(listOf(1, 2, 3))
            fileFolderDao.findVisibleFilesByFolderId(0)
        }

        assertEquals(DataResult.Success(expected), result)
    }

    @Test
    fun `getFolder returns api model`() = runTest {
        coEvery { fileFolderDao.findById(any()) } returns FileFolderEntity(FileFolder(id = 1, name = "Folder 1", parentFolderId = 0))

        val result = fileListLocalDataSource.getFolder(0, true)

        coVerify {
            fileFolderDao.findById(0)
        }

        assertEquals(FileFolder(id = 1, name = "Folder 1", parentFolderId = 0), result)
    }

    @Test
    fun `getFolder returns null if not exists`() = runTest {
        coEvery { fileFolderDao.findById(any()) } returns null

        val result = fileListLocalDataSource.getFolder(0, true)

        coVerify {
            fileFolderDao.findById(0)
        }

        assertNull(result)
    }

    @Test
    fun `getRootFolderForContext returns api model`() = runTest {
        coEvery { fileFolderDao.findRootFolderForContext(any()) } returns FileFolderEntity(FileFolder(id = 1, name = "Folder 1", parentFolderId = 0))

        val result = fileListLocalDataSource.getRootFolderForContext(CanvasContext.defaultCanvasContext(), true)

        assertEquals(FileFolder(id = 1, name = "Folder 1", parentFolderId = 0), result)
    }

    @Test
    fun `getRootFolderForContext returns null if not exists`() = runTest {
        coEvery { fileFolderDao.findRootFolderForContext(any()) } returns null

        val result = fileListLocalDataSource.getRootFolderForContext(CanvasContext.defaultCanvasContext(), true)

        assertNull(result)
    }

    @Test
    fun `getNextPage always returns empty`() = runTest {
        val result = fileListLocalDataSource.getNextPage("url", true)

        assertEquals(DataResult.Success(emptyList<FileFolder>()), result)
    }
}