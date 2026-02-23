/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.program.list

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LearnProgramListRepositoryTest {
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)

    private val testPrograms = listOf(
        createTestProgram(
            id = "program1",
            name = "Software Engineering",
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 0.0),
                createTestProgramRequirement(courseId = 2L, progress = 50.0)
            )
        ),
        createTestProgram(
            id = "program2",
            name = "Data Science",
            requirements = listOf(
                createTestProgramRequirement(courseId = 3L, progress = 100.0)
            )
        )
    )

    private val testCourses = listOf(
        createTestCourse(courseId = 1L, courseName = "Intro to Programming"),
        createTestCourse(courseId = 2L, courseName = "Advanced Algorithms"),
        createTestCourse(courseId = 3L, courseName = "Machine Learning")
    )

    @Before
    fun setup() {
        coEvery { getProgramsManager.getPrograms(any()) } returns testPrograms
        coEvery { getCoursesManager.getProgramCourses(any(), any()) } returns DataResult.Success(testCourses[0])
    }

    @Test
    fun `getPrograms returns list of programs`() = runTest {
        val repository = getRepository()
        val result = repository.getPrograms(false)

        assertEquals(2, result.size)
        assertEquals(testPrograms, result)
        coVerify { getProgramsManager.getPrograms(false) }
    }

    @Test
    fun `getPrograms with forceRefresh true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getPrograms(true)

        coVerify { getProgramsManager.getPrograms(true) }
    }

    @Test
    fun `getPrograms returns empty list when no programs`() = runTest {
        coEvery { getProgramsManager.getPrograms(any()) } returns emptyList()
        val repository = getRepository()
        val result = repository.getPrograms(false)

        assertEquals(0, result.size)
    }

    @Test
    fun `getCoursesById fetches courses for all provided IDs`() = runTest {
        coEvery { getCoursesManager.getProgramCourses(1L, false) } returns DataResult.Success(testCourses[0])
        coEvery { getCoursesManager.getProgramCourses(2L, false) } returns DataResult.Success(testCourses[1])
        coEvery { getCoursesManager.getProgramCourses(3L, false) } returns DataResult.Success(testCourses[2])

        val repository = getRepository()
        val result = repository.getCoursesById(listOf(1L, 2L, 3L), false)

        assertEquals(3, result.size)
        assertEquals("Intro to Programming", result[0].courseName)
        assertEquals("Advanced Algorithms", result[1].courseName)
        assertEquals("Machine Learning", result[2].courseName)
        coVerify { getCoursesManager.getProgramCourses(1L, false) }
        coVerify { getCoursesManager.getProgramCourses(2L, false) }
        coVerify { getCoursesManager.getProgramCourses(3L, false) }
    }

    @Test
    fun `getCoursesById with forceNetwork true calls API with force network`() = runTest {
        coEvery { getCoursesManager.getProgramCourses(1L, true) } returns DataResult.Success(testCourses[0])

        val repository = getRepository()
        repository.getCoursesById(listOf(1L), true)

        coVerify { getCoursesManager.getProgramCourses(1L, true) }
    }

    private fun getRepository(): LearnProgramListRepository {
        return LearnProgramListRepository(getProgramsManager, getCoursesManager)
    }

    private fun createTestProgram(
        id: String = "testProgram",
        name: String = "Test Program",
        requirements: List<ProgramRequirement> = emptyList()
    ): Program = Program(
        id = id,
        name = name,
        description = "Test description",
        startDate = null,
        endDate = null,
        variant = ProgramVariantType.LINEAR,
        courseCompletionCount = null,
        sortedRequirements = requirements
    )

    private fun createTestProgramRequirement(
        courseId: Long = 1L,
        progress: Double = 0.0
    ): ProgramRequirement = ProgramRequirement(
        id = "requirement$courseId",
        progressId = "progress$courseId",
        courseId = courseId,
        required = true,
        progress = progress,
        enrollmentStatus = null
    )

    private fun createTestCourse(
        courseId: Long = 1L,
        courseName: String = "Test Course"
    ): CourseWithModuleItemDurations = CourseWithModuleItemDurations(
        courseId = courseId,
        courseName = courseName,
        moduleItemsDuration = listOf("PT1H"),
        startDate = null,
        endDate = null
    )
}
