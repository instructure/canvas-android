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
package com.instructure.horizon.features.learn.course.list

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
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

class LearnCourseListRepositoryTest {
    private val horizonGetCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val testUser = User(id = 456L)
    private val coursesWithProgress = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Introduction to Programming",
            courseImageUrl = "https://example.com/prog.png",
            progress = 0.0,
            courseSyllabus = "Learn programming basics"
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Advanced Mathematics",
            courseImageUrl = "https://example.com/math.png",
            progress = 50.0,
            courseSyllabus = "Advanced math topics"
        ),
        CourseWithProgress(
            courseId = 3L,
            courseName = "Web Development",
            courseImageUrl = "https://example.com/web.png",
            progress = 100.0,
            courseSyllabus = "Build modern websites"
        )
    )

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(coursesWithProgress)
    }

    @Test
    fun `getCoursesWithProgress returns list of courses`() = runTest {
        val repository = getRepository()
        val result = repository.getCoursesWithProgress(false)

        assertEquals(3, result.size)
        assertEquals(coursesWithProgress, result)
        coVerify { horizonGetCoursesManager.getCoursesWithProgress(456L, false) }
    }

    @Test
    fun `getCoursesWithProgress with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getCoursesWithProgress(true)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(456L, true) }
    }

    @Test
    fun `getCoursesWithProgress uses -1 when user is null`() = runTest {
        every { apiPrefs.user } returns null
        val repository = getRepository()
        repository.getCoursesWithProgress(false)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(-1L, false) }
    }

    @Test
    fun `getCoursesWithProgress returns empty list when no courses`() = runTest {
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(emptyList())
        val repository = getRepository()
        val result = repository.getCoursesWithProgress(false)

        assertEquals(0, result.size)
    }

    private fun getRepository(): LearnCourseListRepository {
        return LearnCourseListRepository(horizonGetCoursesManager, apiPrefs)
    }
}
