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

import android.webkit.URLUtil
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.student.features.files.search.FileSearchLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class FileSearchLocalDataSourceTest {

    private val fileFolderDao: FileFolderDao = mockk(relaxed = true)
    private val localFileDao: LocalFileDao = mockk(relaxed = true)

    private val fileSearchLocalDataSource = FileSearchLocalDataSource(fileFolderDao, localFileDao)

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
    fun `File Search replaces url with path`() = runTest {
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
        coEvery { fileFolderDao.searchCourseFiles(any(), any()) } returns files.map { FileFolderEntity(it) }

        val expected = files.map { it.copy(url = "path_${it.id}", thumbnailUrl = null) }
        val result = fileSearchLocalDataSource.searchFiles(Course(1L), "")

        coVerify {
            localFileDao.findByIds(listOf(1, 2, 3))
            fileFolderDao.searchCourseFiles(1L, "")
        }

        TestCase.assertEquals(expected, result)
    }
}