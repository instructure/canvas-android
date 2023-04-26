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

package com.instructure.pandautils.features.offline;

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class OfflineContentRepositoryTest {

    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface = mockk(relaxed = true)

    private val repository = OfflineContentRepository(coursesApi, fileFolderApi)

    @Test
    fun `Returns course`() = runBlockingTest {
        val course = Course(id = 1L, name = "Course")

        coEvery { coursesApi.getCourse(any(), any()) } returns DataResult.Success(course)

        val result = repository.getCourse(course.id)

        Assert.assertEquals(course, result)
    }

    @Test
    fun `Throws exception when course requests fails`() = runBlockingTest {
        coEvery { coursesApi.getCourse(any(), any()) } returns DataResult.Fail()

        Assert.assertThrows(IllegalStateException::class.java, ThrowingRunnable {
            runBlocking { repository.getCourse(1) }
        })
    }

    @Test
    fun `Returns courses`() = runBlockingTest {
        val courses = listOf(
            Course(id = 1L, name = "Course 1", enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
            Course(id = 2L, name = "Course 2", enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE)))
        )

        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Success(courses)

        val result = repository.getCourses()

        Assert.assertEquals(courses, result)
    }

    @Test
    fun `Returns courses only with active enrollment and valid term`() = runBlockingTest {
        val courses = listOf(
            Course(id = 1L, name = "Course 1", enrollments = mutableListOf(Enrollment(enrollmentState = EnrollmentAPI.STATE_ACTIVE))),
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
    fun `Throws exception when courses requests fails`() = runBlockingTest {
        coEvery { coursesApi.getFirstPageCourses(any()) } returns DataResult.Fail()

        Assert.assertThrows(IllegalStateException::class.java, ThrowingRunnable {
            runBlocking { repository.getCourses() }
        })
    }

    @Test
    fun `Returns course files`() = runBlockingTest {
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
    fun `Hidden and locked files are filtered`() = runBlockingTest {
        val root = FileFolder(id = 1)
        val files = listOf(
            FileFolder(id = 2),
            FileFolder(id = 3, isHidden = true),
            FileFolder(id = 4, isLocked = true),
            FileFolder(id = 5, isHiddenForUser = true),
            FileFolder(id = 6, isLockedForUser = true)
        )

        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Success(root)
        coEvery { fileFolderApi.getFirstPageFiles(1, any()) } returns DataResult.Success(files)
        coEvery { fileFolderApi.getFirstPageFolders(1, any()) } returns DataResult.Success(emptyList())

        val result = repository.getCourseFiles(1)

        Assert.assertEquals(files.subList(0, 1), result)
    }

    @Test
    fun `Returns empty list when files request fails`() = runBlockingTest {
        coEvery { fileFolderApi.getRootFolderForContext(any(), any(), any()) } returns DataResult.Fail()

        val result = repository.getCourseFiles(1)

        Assert.assertEquals(emptyList<FileFolder>(), result)
    }
}
