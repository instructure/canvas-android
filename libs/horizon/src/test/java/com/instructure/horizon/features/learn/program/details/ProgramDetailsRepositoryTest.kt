/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.learn.program.details

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
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProgramDetailsRepositoryTest {
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)

    private val testPrograms = listOf(
        createTestProgram(
            id = "program1",
            name = "Software Engineering",
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 0.0)
            )
        ),
        createTestProgram(
            id = "program2",
            name = "Data Science",
            requirements = listOf(
                createTestProgramRequirement(courseId = 2L, progress = 50.0)
            )
        )
    )

    private val testCourses = listOf(
        createTestCourse(courseId = 1L, courseName = "Intro to Programming"),
        createTestCourse(courseId = 2L, courseName = "Data Analysis"),
        createTestCourse(courseId = 3L, courseName = "Machine Learning")
    )

    @Before
    fun setup() {
        coEvery { getProgramsManager.getPrograms(any()) } returns testPrograms
        coEvery { getCoursesManager.getProgramCourses(any(), any()) } returns DataResult.Success(testCourses[0])
    }

    @Test
    fun `getProgramDetails returns program for valid ID`() = runTest {
        val repository = getRepository()
        val result = repository.getProgramDetails("program1", false)

        assertEquals("program1", result.id)
        assertEquals("Software Engineering", result.name)
        coVerify { getProgramsManager.getPrograms(false) }
    }

    @Test
    fun `getProgramDetails throws exception for invalid ID`() = runTest {
        val repository = getRepository()

        try {
            repository.getProgramDetails("invalidId", false)
            throw AssertionError("Expected IllegalArgumentException to be thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Program with id invalidId not found") == true)
        }
    }

    @Test
    fun `getProgramDetails with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getProgramDetails("program1", true)

        coVerify { getProgramsManager.getPrograms(true) }
    }

    @Test
    fun `getCoursesById fetches all courses in parallel`() = runTest {
        coEvery { getCoursesManager.getProgramCourses(1L, false) } returns DataResult.Success(testCourses[0])
        coEvery { getCoursesManager.getProgramCourses(2L, false) } returns DataResult.Success(testCourses[1])
        coEvery { getCoursesManager.getProgramCourses(3L, false) } returns DataResult.Success(testCourses[2])

        val repository = getRepository()
        val result = repository.getCoursesById(listOf(1L, 2L, 3L), false)

        assertEquals(3, result.size)
        assertEquals("Intro to Programming", result[0].courseName)
        assertEquals("Data Analysis", result[1].courseName)
        assertEquals("Machine Learning", result[2].courseName)
        coVerify { getCoursesManager.getProgramCourses(1L, false) }
        coVerify { getCoursesManager.getProgramCourses(2L, false) }
        coVerify { getCoursesManager.getProgramCourses(3L, false) }
    }

    @Test
    fun `enrollCourse returns success result`() = runTest {
        coEvery { getProgramsManager.enrollCourse(any()) } returns DataResult.Success(Unit)

        val repository = getRepository()
        val result = repository.enrollCourse("progress123")

        assertTrue(result.isSuccess)
        coVerify { getProgramsManager.enrollCourse("progress123") }
    }

    @Test
    fun `enrollCourse returns failure result`() = runTest {
        coEvery { getProgramsManager.enrollCourse(any()) } returns DataResult.Fail()

        val repository = getRepository()
        val result = repository.enrollCourse("progress123")

        assertTrue(result.isFail)
        coVerify { getProgramsManager.enrollCourse("progress123") }
    }

    private fun getRepository(): ProgramDetailsRepository {
        return ProgramDetailsRepository(getProgramsManager, getCoursesManager)
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
