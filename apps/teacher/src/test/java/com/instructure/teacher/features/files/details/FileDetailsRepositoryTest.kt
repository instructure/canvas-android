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

package com.instructure.teacher.features.files.details

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class FileDetailsRepositoryTest {

    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)
    private val featuresApi: FeaturesAPI.FeaturesInterface = mockk(relaxed = true)

    private val repository = FileDetailsRepository(fileFolderApi, featuresApi)

    @Test
    fun `Get file folder from url successfully returns data`() = runTest {
        val expected = FileFolder(id = 1L)

        coEvery { fileFolderApi.getFileFolderFromURL(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getFileFolderFromURL("url")
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get file folder from url failure throws exception`() = runTest {
        coEvery { fileFolderApi.getFileFolderFromURL(any(), any()) } returns DataResult.Fail()

        repository.getFileFolderFromURL("url")
    }

    @Test
    fun `Get enabled features for course successfully returns data`() = runTest {
        val expected = listOf("feature")

        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCourseFeatures(1L)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get enabled features for course failure throws exception`() = runTest {
        coEvery { featuresApi.getEnabledFeaturesForCourse(any(), any()) } returns DataResult.Fail()

        repository.getCourseFeatures(1L)
    }

    @Test
    fun `Get course file licences successfully returns data`() = runTest {
        val expected = listOf(License(id = "licence", name = "name", url = "url"))

        coEvery { fileFolderApi.getCourseFileLicenses(any(), any()) } returns DataResult.Success(expected)

        val result = repository.getCourseFileLicences(1L)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get course file licences failure throws exception`() = runTest {
        coEvery { fileFolderApi.getCourseFileLicenses(any(), any()) } returns DataResult.Fail()

        repository.getCourseFileLicences(1L)
    }
}
