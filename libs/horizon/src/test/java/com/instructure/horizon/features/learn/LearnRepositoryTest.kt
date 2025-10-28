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
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
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

    private val userId = 1L

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = userId, name = "Test User")
    }

    @Test
    fun `Test successful courses with progress retrieval`() = runTest {
        val courses = listOf(
            CourseWithProgress(
                courseId = 1L,
                courseName = "Course 1",
                courseSyllabus = "",
                progress = 50.0
            ),
            CourseWithProgress(
                courseId = 2L,
                courseName = "Course 2",
                courseSyllabus = "",
                progress = 75.0
            )
        )
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Success(courses)

        val result = getRepository().getCoursesWithProgress(false)

        assertEquals(2, result.size)
        assertEquals(courses, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed courses retrieval throws exception`() = runTest {
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Fail()

        getRepository().getCoursesWithProgress(false)
    }

    @Test
    fun `Test successful programs retrieval`() = runTest {
        val programs = listOf(
            Program(
                id = "1",
                name = "Program 1",
                description = "Program 1 Description",
                sortedRequirements = emptyList(),
                startDate = null,
                endDate = null,
                variant = ProgramVariantType.LINEAR,
            ),
            Program(
                id = "2",
                name = "Program 2",
                description = "Program 2 Description",
                sortedRequirements = emptyList(),
                startDate = null,
                endDate = null,
                variant = ProgramVariantType.NON_LINEAR,
            )
        )
        coEvery { getProgramsManager.getPrograms(false) } returns programs

        val result = getRepository().getPrograms(false)

        assertEquals(2, result.size)
        assertEquals(programs, result)
    }

    @Test
    fun `Test successful get courses by id`() = runTest {
        val courseIds = listOf(1L, 2L)
        val course1 = CourseWithModuleItemDurations(courseId = 1L, courseName = "Course 1")
        val course2 = CourseWithModuleItemDurations(courseId = 2L, courseName = "Course 2")

        coEvery { horizonGetCoursesManager.getProgramCourses(1L, false) } returns DataResult.Success(course1)
        coEvery { horizonGetCoursesManager.getProgramCourses(2L, false) } returns DataResult.Success(course2)

        val result = getRepository().getCoursesById(courseIds, false)

        assertEquals(2, result.size)
        assertEquals(course1, result[0])
        assertEquals(course2, result[1])
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed get courses by id throws exception`() = runTest {
        val courseIds = listOf(1L)
        coEvery { horizonGetCoursesManager.getProgramCourses(1L, false) } returns DataResult.Fail()

        getRepository().getCoursesById(courseIds, false)
    }

    private fun getRepository(): LearnRepository {
        return LearnRepository(horizonGetCoursesManager, getProgramsManager, apiPrefs)
    }
}
