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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.datasource.ProgramDetailsLocalDataSource
import com.instructure.horizon.data.datasource.ProgramDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.ProgramLocalDataSource
import com.instructure.horizon.data.datasource.ProgramNetworkDataSource
import com.instructure.journey.type.ProgramVariantType
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ProgramRepositoryTest {
    private val networkDataSource: ProgramNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: ProgramLocalDataSource = mockk(relaxed = true)
    private val programDetailsNetworkDataSource: ProgramDetailsNetworkDataSource = mockk(relaxed = true)
    private val programDetailsLocalDataSource: ProgramDetailsLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val testPrograms = listOf(
        createTestProgram(id = "program1", name = "Software Engineering",
            requirements = listOf(createTestProgramRequirement(courseId = 1L))),
        createTestProgram(id = "program2", name = "Data Science",
            requirements = listOf(createTestProgramRequirement(courseId = 2L, progress = 50.0)))
    )

    private val testCourses = listOf(
        createTestCourse(courseId = 1L, courseName = "Intro to Programming"),
        createTestCourse(courseId = 2L, courseName = "Data Analysis"),
        createTestCourse(courseId = 3L, courseName = "Machine Learning")
    )

    @Before
    fun setup() {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getPrograms(any()) } returns testPrograms
        coEvery { programDetailsNetworkDataSource.getProgramDetails(any(), any()) } returns testPrograms[0]
        coEvery { programDetailsNetworkDataSource.getCoursesById(any(), any()) } returns testCourses
    }

    @Test
    fun `getPrograms returns programs from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getPrograms(false)

        assertEquals(2, result.size)
        assertEquals("Software Engineering", result[0].name)
        coVerify { networkDataSource.getPrograms(false) }
    }

    @Test
    fun `getPrograms returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { localDataSource.getPrograms() } returns testPrograms

        val repository = getRepository()
        val result = repository.getPrograms(false)

        assertEquals(2, result.size)
        coVerify { localDataSource.getPrograms() }
    }

    @Test
    fun `getProgramDetails returns program from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getProgramDetails("program1", false)

        assertEquals("program1", result.id)
        assertEquals("Software Engineering", result.name)
        coVerify { programDetailsNetworkDataSource.getProgramDetails("program1", false) }
    }

    @Test
    fun `getProgramDetails with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getProgramDetails("program1", true)

        coVerify { programDetailsNetworkDataSource.getProgramDetails("program1", true) }
    }

    @Test
    fun `getProgramDetails returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { programDetailsLocalDataSource.getProgramDetails(any()) } returns testPrograms[0]

        val repository = getRepository()
        val result = repository.getProgramDetails("program1", false)

        assertEquals("program1", result.id)
        coVerify { programDetailsLocalDataSource.getProgramDetails("program1") }
    }

    @Test
    fun `getCoursesById returns courses from network data source when online`() = runTest {
        coEvery { programDetailsNetworkDataSource.getCoursesById(listOf(1L, 2L, 3L), false) } returns testCourses

        val repository = getRepository()
        val result = repository.getCoursesById(listOf(1L, 2L, 3L), false)

        assertEquals(3, result.size)
        assertEquals("Intro to Programming", result[0].courseName)
        assertEquals("Data Analysis", result[1].courseName)
        assertEquals("Machine Learning", result[2].courseName)
        coVerify { programDetailsNetworkDataSource.getCoursesById(listOf(1L, 2L, 3L), false) }
    }

    @Test
    fun `getCoursesById with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getCoursesById(listOf(1L, 2L), true)

        coVerify { programDetailsNetworkDataSource.getCoursesById(listOf(1L, 2L), true) }
    }

    @Test
    fun `getCoursesById returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { programDetailsLocalDataSource.getCoursesById(any()) } returns testCourses

        val repository = getRepository()
        val result = repository.getCoursesById(listOf(1L, 2L, 3L), false)

        assertEquals(3, result.size)
        coVerify { programDetailsLocalDataSource.getCoursesById(listOf(1L, 2L, 3L)) }
    }

    @Test
    fun `enrollCourse returns success result`() = runTest {
        coEvery { programDetailsNetworkDataSource.enrollCourse(any()) } returns DataResult.Success(Unit)

        val repository = getRepository()
        val result = repository.enrollCourse("progress123")

        assertTrue(result.isSuccess)
        coVerify { programDetailsNetworkDataSource.enrollCourse("progress123") }
    }

    @Test
    fun `enrollCourse returns failure result`() = runTest {
        coEvery { programDetailsNetworkDataSource.enrollCourse(any()) } returns DataResult.Fail()

        val repository = getRepository()
        val result = repository.enrollCourse("progress123")

        assertTrue(result.isFail)
        coVerify { programDetailsNetworkDataSource.enrollCourse("progress123") }
    }

    private fun getRepository(): ProgramRepository {
        return ProgramRepository(
            networkDataSource,
            localDataSource,
            programDetailsNetworkDataSource,
            programDetailsLocalDataSource,
            networkStateProvider,
            featureFlagProvider
        )
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
