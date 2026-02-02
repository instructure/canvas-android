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
package com.instructure.horizon.features.dashboard.widget.course.list

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DashboardCourseListRepositoryTest {
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val getCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)

    private val userId = 123L
    private val testUser = User(id = userId, name = "Test User")

    private val testCourses = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Course 1",
            courseImageUrl = null,
            courseSyllabus = "Syllabus 1",
            progress = 50.0
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Course 2",
            courseImageUrl = null,
            courseSyllabus = "Syllabus 2",
            progress = 75.0
        ),
        CourseWithProgress(
            courseId = 3L,
            courseName = "Course 3",
            courseImageUrl = null,
            courseSyllabus = "Syllabus 3",
            progress = 100.0
        )
    )

    private val testPrograms = listOf(
        Program(
            id = "program-1",
            name = "Program 1",
            description = "Description 1",
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR,
            sortedRequirements = listOf(
                ProgramRequirement(
                    id = "req-1",
                    progressId = "progress-1",
                    courseId = 1L,
                    required = true
                )
            )
        ),
        Program(
            id = "program-2",
            name = "Program 2",
            description = "Description 2",
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.NON_LINEAR,
            sortedRequirements = listOf(
                ProgramRequirement(
                    id = "req-2",
                    progressId = "progress-2",
                    courseId = 2L,
                    required = true
                )
            )
        )
    )

    @Before
    fun setup() {
        every { apiPrefs.user } returns testUser
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Test successful courses retrieval without forceRefresh`() = runTest {
        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Success(testCourses)

        val result = getRepository().getCourses(false)

        assertEquals(3, result.size)
        assertEquals(testCourses, result)
        coVerify { getCoursesManager.getCoursesWithProgress(userId, false) }
    }

    @Test
    fun `Test successful courses retrieval with forceRefresh`() = runTest {
        coEvery { getCoursesManager.getCoursesWithProgress(userId, true) } returns DataResult.Success(testCourses)

        val result = getRepository().getCourses(true)

        assertEquals(3, result.size)
        assertEquals(testCourses, result)
        coVerify { getCoursesManager.getCoursesWithProgress(userId, true) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed courses retrieval throws exception`() = runTest {
        coEvery { getCoursesManager.getCoursesWithProgress(userId, false) } returns DataResult.Fail()

        getRepository().getCourses(false)
    }

    @Test
    fun `Test successful programs retrieval without forceRefresh`() = runTest {
        coEvery { getProgramsManager.getPrograms(false) } returns testPrograms

        val result = getRepository().getPrograms(false)

        assertEquals(2, result.size)
        assertEquals(testPrograms, result)
        coVerify { getProgramsManager.getPrograms(false) }
    }

    @Test
    fun `Test successful programs retrieval with forceRefresh`() = runTest {
        coEvery { getProgramsManager.getPrograms(true) } returns testPrograms

        val result = getRepository().getPrograms(true)

        assertEquals(2, result.size)
        assertEquals(testPrograms, result)
        coVerify { getProgramsManager.getPrograms(true) }
    }

    @Test(expected = Exception::class)
    fun `Test failed programs retrieval throws exception`() = runTest {
        coEvery { getProgramsManager.getPrograms(false) } throws Exception("Network error")

        getRepository().getPrograms(false)
    }

    private fun getRepository(): DashboardCourseListRepository {
        return DashboardCourseListRepository(apiPrefs, getCoursesManager, getProgramsManager)
    }
}
