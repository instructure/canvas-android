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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.files.list.FileListLocalDataSource
import com.instructure.student.features.files.list.FileListNetworkDataSource
import com.instructure.student.features.files.list.FileListRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FileListRepositoryTest {

    private val fileListLocalDataSource: FileListLocalDataSource = mockk(relaxed = true)
    private val fileListNetworkDataSource: FileListNetworkDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val fileListRepository = FileListRepository(
        fileListLocalDataSource,
        fileListNetworkDataSource,
        networkStateProvider,
        featureFlagProvider
    )

    @Before
    fun setup() {
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { networkStateProvider.isOnline() } returns true
    }

    @Test
    fun `use localDataSource when network is offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false

        assert(fileListRepository.dataSource() is FileListLocalDataSource)
    }

    @Test
    fun `use networkDataSource when network is online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true

        assert(fileListRepository.dataSource() is FileListNetworkDataSource)
    }

    @Test
    fun `Return first page folders if multiple pages`() = runTest {
        val firstPage = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getFolders(any(), any()) } returns DataResult.Success(firstPage, linkHeaders = LinkHeaders(nextUrl = "nextPage"))

        val result = fileListRepository.getFirstPageItems(1, false)

        assertEquals(firstPage, result.dataOrNull)
    }

    @Test
    fun `getFirstPageItems() concats first page files to the last page folders`() = runTest {
        val folders = listOf(mockk<FileFolder>())
        val files = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getFolders(any(), any()) } returns DataResult.Success(folders)
        coEvery { fileListNetworkDataSource.getFiles(any(), any()) } returns DataResult.Success(files, linkHeaders = LinkHeaders(nextUrl = "nextPage"))

        val result = fileListRepository.getFirstPageItems(1, false)

        assertEquals(DataResult.Success(folders + files, linkHeaders = LinkHeaders(nextUrl = "nextPage")), result)
    }


    @Test
    fun `getNextPage() returns only next page if not last`() = runTest {
        val folders = listOf(mockk<FileFolder>())
        val nextNext = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getNextPage("nextPage", any()) } returns DataResult.Success(folders, linkHeaders = LinkHeaders(nextUrl = "nextNextPage"))
        coEvery { fileListNetworkDataSource.getNextPage("nextNextPage", any()) } returns DataResult.Success(nextNext)

        val result = fileListRepository.getNextPage("nextPage", 0L,false)

        assertEquals(DataResult.Success(folders, linkHeaders = LinkHeaders(nextUrl = "nextNextPage")), result)
    }

    @Test
    fun `getNextPage() concats first page files to last page folders`() = runTest {
        val folders = listOf(mockk<FileFolder>())
        val files = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getNextPage("nextPage", any()) } returns DataResult.Success(folders)
        coEvery { fileListNetworkDataSource.getFiles(any(), any()) } returns DataResult.Success(files, linkHeaders = LinkHeaders(nextUrl = "nextNextPage"))

        val result = fileListRepository.getNextPage("nextPage", 0L,false)

        assertEquals(DataResult.Success(folders + files, linkHeaders = LinkHeaders(nextUrl = "nextNextPage")), result)
    }

    @Test
    fun `getFirstPageItems() returns fail if first call fails`() = runTest {
        coEvery { fileListNetworkDataSource.getFolders(any(), any()) } returns DataResult.Fail()

        assertEquals(DataResult.Fail(), fileListRepository.getFirstPageItems(1, false))
    }

    @Test
    fun `getFirstPageItems() returns fail if next page fails`() = runTest {
        val firstPage = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getFolders(any(), any()) } returns DataResult.Success(firstPage)
        coEvery { fileListNetworkDataSource.getFiles(any(), any()) } returns DataResult.Fail()

        assertEquals(DataResult.Fail(), fileListRepository.getFirstPageItems(1, false))
    }

    @Test
    fun `getNextPage() returns fail if the first page fails`() = runTest {
        coEvery { fileListNetworkDataSource.getNextPage("nextPage", any()) } returns DataResult.Fail()

        assertEquals(DataResult.Fail(), fileListRepository.getNextPage("nextPage", 0L,false))
    }

    @Test
    fun `getNextPage() returns fail if the next page fails`() = runTest {
        val folders = listOf(mockk<FileFolder>())

        coEvery { fileListNetworkDataSource.getNextPage("nextPage", any()) } returns DataResult.Success(folders)
        coEvery { fileListNetworkDataSource.getFiles(any(), any()) } returns DataResult.Fail()

        assertEquals(DataResult.Fail(), fileListRepository.getNextPage("nextPage", 0L,false))
    }

    @Test
    fun `getFolder() calls localDataSource when offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        fileListRepository.getFolder(1, false)

        coVerify { fileListLocalDataSource.getFolder(1, false) }
    }

    @Test
    fun `getFolder() calls networkDataSource when online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true
        fileListRepository.getFolder(1, false)

        coVerify { fileListNetworkDataSource.getFolder(1, false) }
    }

    @Test
    fun `getRootFolderForContext() calls localDataSource when offline`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        fileListRepository.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), false)

        coVerify { fileListLocalDataSource.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), false) }
    }

    @Test
    fun `getRootFolderForContext() calls networkDataSource when online`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns true
        fileListRepository.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), false)

        coVerify { fileListNetworkDataSource.getRootFolderForContext(CanvasContext.emptyCourseContext(1L), false) }
    }

}