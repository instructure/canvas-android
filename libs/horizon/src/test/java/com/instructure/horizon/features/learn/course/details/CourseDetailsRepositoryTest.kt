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
package com.instructure.horizon.features.learn.course.details

import com.instructure.canvasapi2.apis.ExternalToolAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CourseDetailsRepositoryTest {
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)
    private val externalToolApi: ExternalToolAPI.ExternalToolInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val testUser = User(id = 789L)
    private val testCourse = CourseWithProgress(
        courseId = 1L,
        courseName = "Test Course",
        courseImageUrl = "https://example.com/course.png",
        progress = 50.0,
        courseSyllabus = "This is the course syllabus"
    )
    private val testPrograms = listOf(
        Program(
            id = "prog1",
            name = "Program 1",
            description = "Program 1 description",
            startDate = null,
            endDate = null,
            variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
            courseCompletionCount = 0,
            sortedRequirements = listOf(ProgramRequirement(id = "req1", progressId = "prog1", courseId = 1L, required = true))
        ),
        Program(
            id = "prog2",
            name = "Program 2",
            description = "Program 2 description",
            startDate = null,
            endDate = null,
            variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
            courseCompletionCount = 0,
            sortedRequirements = listOf(ProgramRequirement(id = "req2", progressId = "prog2", courseId = 2L, required = true))
        )
    )

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
        coEvery { getCoursesManager.getCourseWithProgressById(any(), any(), any()) } returns DataResult.Success(testCourse)
        coEvery { getProgramsManager.getPrograms(any()) } returns testPrograms
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Success(emptyList())
    }

    @Test
    fun `getCourse returns course with progress`() = runTest {
        val repository = getRepository()
        val result = repository.getCourse(1L, false)

        assertEquals(testCourse, result)
        coVerify { getCoursesManager.getCourseWithProgressById(1L, 789L, false) }
    }

    @Test
    fun `getCourse with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getCourse(1L, true)

        coVerify { getCoursesManager.getCourseWithProgressById(1L, 789L, true) }
    }

    @Test
    fun `getCourse uses -1 when user is null`() = runTest {
        every { apiPrefs.user } returns null
        val repository = getRepository()
        repository.getCourse(1L, false)

        coVerify { getCoursesManager.getCourseWithProgressById(1L, -1L, false) }
    }

    @Test
    fun `getProgramsForCourse returns programs containing the course`() = runTest {
        val repository = getRepository()
        val result = repository.getProgramsForCourse(1L, false)

        assertEquals(1, result.size)
        assertEquals("Program 1", result[0].name)
        coVerify { getProgramsManager.getPrograms(false) }
    }

    @Test
    fun `getProgramsForCourse returns empty list when no programs contain the course`() = runTest {
        val repository = getRepository()
        val result = repository.getProgramsForCourse(999L, false)

        assertEquals(0, result.size)
    }

    @Test
    fun `getProgramsForCourse with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.getProgramsForCourse(1L, true)

        coVerify { getProgramsManager.getPrograms(true) }
    }

    @Test
    fun `hasExternalTools returns true when course has tools`() = runTest {
        val tools = listOf(LTITool(url = "https://tool.example.com"))
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Success(tools)
        val repository = getRepository()

        val result = repository.hasExternalTools(1L, false)

        assertTrue(result)
        coVerify { externalToolApi.getExternalToolsForCourses(listOf("course_1"), any()) }
    }

    @Test
    fun `hasExternalTools returns false when course has no tools`() = runTest {
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Success(emptyList())
        val repository = getRepository()

        val result = repository.hasExternalTools(1L, false)

        assertFalse(result)
    }

    @Test
    fun `hasExternalTools returns false when API returns null`() = runTest {
        coEvery { externalToolApi.getExternalToolsForCourses(any(), any()) } returns DataResult.Fail()
        val repository = getRepository()

        val result = repository.hasExternalTools(1L, false)

        assertFalse(result)
    }

    @Test
    fun `hasExternalTools with forceNetwork true calls API with force network`() = runTest {
        val repository = getRepository()
        repository.hasExternalTools(1L, true)

        coVerify {
            externalToolApi.getExternalToolsForCourses(
                any(),
                match { it.isForceReadFromNetwork }
            )
        }
    }

    @Test
    fun `Multiple programs can contain the same course`() = runTest {
        val multiplePrograms = listOf(
            Program(
                id = "prog1",
                name = "Program 1",
                description = "Program 1 description",
                startDate = null,
                endDate = null,
                variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
                courseCompletionCount = 0,
                sortedRequirements = listOf(ProgramRequirement(id = "req1", progressId = "prog1", courseId = 1L, required = true))
            ),
            Program(
                id = "prog2",
                name = "Program 2",
                description = "Program 2 description",
                startDate = null,
                endDate = null,
                variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
                courseCompletionCount = 0,
                sortedRequirements = listOf(ProgramRequirement(id = "req2", progressId = "prog2", courseId = 1L, required = true))
            )
        )
        coEvery { getProgramsManager.getPrograms(any()) } returns multiplePrograms
        val repository = getRepository()

        val result = repository.getProgramsForCourse(1L, false)

        assertEquals(2, result.size)
    }

    private fun getRepository(): CourseDetailsRepository {
        return CourseDetailsRepository(getCoursesManager, getProgramsManager, externalToolApi, apiPrefs)
    }
}
