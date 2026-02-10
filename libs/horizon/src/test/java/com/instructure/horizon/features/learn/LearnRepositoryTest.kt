/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LearnRepositoryTest {
    private val horizonGetCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val testUser = User(id = 123L)
    private val coursesWithProgress = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Course 1",
            courseImageUrl = "https://example.com/image1.png",
            progress = 50.0,
            courseSyllabus = "Syllabus 1"
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Course 2",
            courseImageUrl = "https://example.com/image2.png",
            progress = 100.0,
            courseSyllabus = "Syllabus 2"
        )
    )

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(coursesWithProgress)
    }

    @Test
    fun `getCoursesWithProgress returns list of courses with progress`() = runTest {
        val repository = getRepository()
        val result = repository.getCoursesWithProgress(false)

        assertEquals(2, result.size)
        assertEquals(coursesWithProgress, result)
        coVerify { horizonGetCoursesManager.getCoursesWithProgress(123L, false) }
    }

    @Test
    fun `getCoursesWithProgress with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getCoursesWithProgress(true)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(123L, true) }
    }

    @Test
    fun `getCoursesWithProgress uses -1 when user is null`() = runTest {
        every { apiPrefs.user } returns null
        val repository = getRepository()
        repository.getCoursesWithProgress(false)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(-1L, false) }
    }

    @Test
    fun `getCoursesById returns list of courses`() = runTest {
        val courseIds = listOf(1L, 2L, 3L)
        val expectedCourses = courseIds.map { id ->
            CourseWithModuleItemDurations(
                courseId = id,
                courseName = "Course $id",
                moduleItemsDuration = listOf("30m", "45m"),
                startDate = null,
                endDate = null
            )
        }

        coEvery { horizonGetCoursesManager.getProgramCourses(1L, false) } returns DataResult.Success(expectedCourses[0])
        coEvery { horizonGetCoursesManager.getProgramCourses(2L, false) } returns DataResult.Success(expectedCourses[1])
        coEvery { horizonGetCoursesManager.getProgramCourses(3L, false) } returns DataResult.Success(expectedCourses[2])

        val repository = getRepository()
        val result = repository.getCoursesById(courseIds)

        assertEquals(3, result.size)
        assertEquals(expectedCourses, result)
        coVerify { horizonGetCoursesManager.getProgramCourses(1L, false) }
        coVerify { horizonGetCoursesManager.getProgramCourses(2L, false) }
        coVerify { horizonGetCoursesManager.getProgramCourses(3L, false) }
    }

    @Test
    fun `getCoursesById with forceNetwork true calls API with force network`() = runTest {
        val courseIds = listOf(1L)
        val course = CourseWithModuleItemDurations(
            courseId = 1L,
            courseName = "Course 1",
            moduleItemsDuration = listOf("30m"),
            startDate = null,
            endDate = null
        )
        coEvery { horizonGetCoursesManager.getProgramCourses(1L, true) } returns DataResult.Success(course)

        val repository = getRepository()
        repository.getCoursesById(courseIds, forceNetwork = true)

        coVerify { horizonGetCoursesManager.getProgramCourses(1L, true) }
    }

    @Test
    fun `getCoursesById with empty list returns empty list`() = runTest {
        val repository = getRepository()
        val result = repository.getCoursesById(emptyList())

        assertEquals(0, result.size)
    }

    private fun getRepository(): LearnRepository {
        return LearnRepository(horizonGetCoursesManager, getProgramsManager, apiPrefs)
    }
}
