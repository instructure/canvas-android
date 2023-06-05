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

package com.instructure.pandautils.features.offline.offlinecontent

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class OfflineContentRepositoryTest {

    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val fileSyncSettingsDao: FileSyncSettingsDao = mockk(relaxed = true)

    private val repository =
        OfflineContentRepository(coursesApi, fileFolderApi, courseSyncSettingsDao, fileSyncSettingsDao)

    @Test
    fun `Returns course`() = runTest {
        val course = Course(id = 1L, name = "Course")

        coEvery { coursesApi.getCourse(any(), any()) } returns DataResult.Success(course)

        val result = repository.getCourse(course.id)

        Assert.assertEquals(course, result)
    }

    @Test
    fun `Throws exception when course requests fails`() = runTest {
        coEvery { coursesApi.getCourse(any(), any()) } returns DataResult.Fail()

        Assert.assertThrows(IllegalStateException::class.java, ThrowingRunnable {
            runBlocking { repository.getCourse(1) }
        })
    }

    @Test
    fun `Returns courses`() = runTest {
        val courses = listOf(
            Course(
                id = 1L,
                name = "Course 1",
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
            ),
            Course(
                id = 2L,
                name = "Course 2",
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
            )
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = repository.getCourses()

        Assert.assertEquals(courses, result)
    }

    @Test
    fun `Returns courses only with active enrollment and valid term`() = runTest {
        val courses = listOf(
            Course(
                id = 1L,
                name = "Course 1",
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))
            ),
            Course(
                id = 2L,
                name = "Course 2",
                enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)),
                term = Term(endAt = OffsetDateTime.now().minusDays(1).withNano(0).toString())
            ),
            Course(id = 3L, name = "Course 3")
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = repository.getCourses()

        Assert.assertEquals(listOf(courses.first()), result)
    }

    @Test
    fun `Throws exception when courses requests fails`() = runTest {
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Fail()

        Assert.assertThrows(IllegalStateException::class.java, ThrowingRunnable {
            runBlocking { repository.getCourses() }
        })
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
    fun `Create default course settings`() = runTest {
        val expected = CourseSyncSettingsEntity(1L, false, false, false, false, false)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns null

        val syncSettings = repository.findCourseSyncSettings(1L)

        coVerify(exactly = 1) { courseSyncSettingsDao.insert(expected) }

        assertEquals(expected, syncSettings.courseSyncSettings)
    }

    @Test
    fun `Add file calls dao`() = runTest {
        val fileSettings = FileSyncSettingsEntity(1L, 1L, null)

        repository.saveFileSettings(fileSettings)

        coVerify(exactly = 1) { fileSyncSettingsDao.insert(fileSettings) }
    }

    @Test
    fun `Delete file calls dao`() = runTest {
        repository.deleteFileSettings(1L)

        coVerify(exactly = 1) { fileSyncSettingsDao.deleteById(1L) }
    }

    @Test
    fun `Delete files calls dao`() = runTest {
        repository.deleteFileSettings(listOf(1L, 2L))

        coVerify(exactly = 1) { fileSyncSettingsDao.deleteByIds(listOf(1L, 2L)) }
    }

    @Test
    fun `Course settings update updates db`() = runTest {
        val courseSyncSettings = CourseSyncSettingsEntity(1L, true, false, false, false, false)

        repository.updateCourseSyncSettings(courseSyncSettings)

        coVerify(exactly = 1) { courseSyncSettingsDao.update(courseSyncSettings) }
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
