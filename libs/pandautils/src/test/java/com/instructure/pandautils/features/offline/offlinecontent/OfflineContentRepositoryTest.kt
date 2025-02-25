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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Term
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.settings.SyncFrequency
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.time.OffsetDateTime

class OfflineContentRepositoryTest {

    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val fileSyncSettingsDao: FileSyncSettingsDao = mockk(relaxed = true)
    private val courseFileSharedRepository: CourseFileSharedRepository = mockk(relaxed = true)
    private val syncSettingsFacade: SyncSettingsFacade = mockk(relaxed = true)
    private val localFileDao: LocalFileDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)

    private val repository = OfflineContentRepository(
        coursesApi,
        courseSyncSettingsDao,
        fileSyncSettingsDao,
        courseFileSharedRepository,
        syncSettingsFacade,
        localFileDao,
        fileSyncProgressDao
    )

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
    fun `Create default course settings`() = runTest {
        val expected = CourseSyncSettingsEntity(1L, "Course", false)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns null

        val syncSettings = repository.findCourseSyncSettings(Course(id = 1L, name = "Course"))

        coVerify(exactly = 1) { courseSyncSettingsDao.insert(expected) }

        assertEquals(expected, syncSettings.courseSyncSettings)
    }

    @Test
    fun `Add file calls dao`() = runTest {
        val fileSettings = FileSyncSettingsEntity(1L, "testFile.pdf", 1L, null)

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
        val courseSyncSettings = CourseSyncSettingsEntity(1L, "Course", true)

        repository.updateCourseSyncSettings(1L, courseSyncSettings, emptyList())

        coVerify(exactly = 1) { courseSyncSettingsDao.update(courseSyncSettings) }
    }

    @Test
    fun `Get sync settings calls facade`() = runTest {
        val expected = SyncSettingsEntity(1L, true, SyncFrequency.DAILY, true)
        coEvery { syncSettingsFacade.getSyncSettings() } returns expected

        val result = repository.getSyncSettings()

        coVerify(exactly = 1) { syncSettingsFacade.getSyncSettings() }
        assertEquals(expected, result)
    }

    @Test
    fun `Is file synced calls dao`() = runTest {
        val expected = true
        coEvery { localFileDao.existsById(1L) } returns expected

        val result = repository.isFileSynced(1L)

        coVerify(exactly = 1) { localFileDao.existsById(1L) }
        assertEquals(expected, result)
    }

    @Test
    fun `Get in progress file size returns 0 when not found`() = runTest {
        coEvery { fileSyncProgressDao.findByFileId(1L) } returns null

        val result = repository.getInProgressFileSize(1L)

        coVerify(exactly = 1) { fileSyncProgressDao.findByFileId(1L) }
        assertEquals(0L, result)
    }

    @Test
    fun `Get in progress file size returns correctly`() = runTest {
        coEvery { fileSyncProgressDao.findByFileId(1L) } returns FileSyncProgressEntity(
            1L, 1L, "File name", 50,
            1000, false, ProgressState.IN_PROGRESS
        )

        val result = repository.getInProgressFileSize(1L)

        assertEquals(500L, result)
    }
}
