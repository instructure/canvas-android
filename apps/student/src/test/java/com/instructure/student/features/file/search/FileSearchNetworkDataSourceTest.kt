/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.file.search

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.features.files.search.FileSearchNetworkDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FileSearchNetworkDataSourceTest {

    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val fileSearchNetworkDataSource = FileSearchNetworkDataSource(fileFolderApi)

    @Test
    fun `searchFiles() calls api and returns data from api`() = runTest {
        coEvery { fileFolderApi.searchFiles(any(), any(), any()) } returns DataResult.Success(listOf(FileFolder(id = 1, name = "File")))

        val result = fileSearchNetworkDataSource.searchFiles(Course(1), "file")

        coVerify { fileFolderApi.searchFiles("courses/1", "file",any()) }
        assertEquals(1, result.size)
        assertEquals("File", result[0].name)
    }

    @Test
    fun `searchFiles() returns empty list for failed result`() = runTest {
        coEvery { fileFolderApi.searchFiles(any(), any(), any()) } returns DataResult.Fail()

        val result = fileSearchNetworkDataSource.searchFiles(Course(1), "file")

        coVerify { fileFolderApi.searchFiles("courses/1", "file",any()) }
        assertEquals(0, result.size)
    }
}