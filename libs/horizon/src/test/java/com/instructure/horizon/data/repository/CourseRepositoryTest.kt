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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.data.datasource.CourseDetailsLocalDataSource
import com.instructure.horizon.data.datasource.CourseDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.CourseProgressLocalDataSource
import com.instructure.horizon.data.datasource.CourseProgressNetworkDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.features.offline.sync.HtmlParsingResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
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

class CourseRepositoryTest {
    private val courseDetailsNetworkDataSource: CourseDetailsNetworkDataSource = mockk(relaxed = true)
    private val courseDetailsLocalDataSource: CourseDetailsLocalDataSource = mockk(relaxed = true)
    private val courseProgressNetworkDataSource: CourseProgressNetworkDataSource = mockk(relaxed = true)
    private val courseProgressLocalDataSource: CourseProgressLocalDataSource = mockk(relaxed = true)
    private val htmlParser: HtmlParser = mockk(relaxed = true)
    private val fileSyncRepository: HorizonFileSyncRepository = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

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
    private val testModules = listOf(
        ModuleObject(
            id = 1L,
            name = "Module 1",
            position = 1,
            items = listOf(
                ModuleItem(id = 101L, title = "Assignment 1", type = "Assignment"),
                ModuleItem(id = 102L, title = "Quiz 1", type = "Quiz")
            )
        ),
        ModuleObject(
            id = 2L,
            name = "Module 2",
            position = 2,
            items = listOf(
                ModuleItem(id = 201L, title = "Page 1", type = "Page")
            )
        )
    )

    @Before
    fun setup() {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult(
            htmlWithLocalFileLinks = "parsed html",
            internalFileIds = emptySet(),
            externalFileUrls = emptySet(),
            studioMediaIds = emptySet(),
        )
        coEvery { courseDetailsNetworkDataSource.getCourse(any(), any()) } returns testCourse
        coEvery { courseDetailsNetworkDataSource.getProgramsForCourse(any(), any()) } returns testPrograms.filter { prog ->
            prog.sortedRequirements.any { it.courseId == 1L }
        }
        coEvery { courseDetailsNetworkDataSource.hasExternalTools(any(), any()) } returns false
        coEvery { courseProgressNetworkDataSource.getModuleItems(any(), any()) } returns testModules
    }

    @Test
    fun `getCourse returns course from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getCourse(1L, false)

        assertEquals(testCourse, result)
        coVerify { courseDetailsNetworkDataSource.getCourse(1L, false) }
    }

    @Test
    fun `getCourse with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getCourse(1L, true)

        coVerify { courseDetailsNetworkDataSource.getCourse(1L, true) }
    }

    @Test
    fun `getCourse parses syllabus HTML and saves parsed version when syncing`() = runTest {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val parsedHtml = "<p>parsed <img src=\"file:///local/image.jpg\"></p>"
        coEvery { htmlParser.createHtmlStringWithLocalFiles(any(), any()) } returns HtmlParsingResult(
            htmlWithLocalFileLinks = parsedHtml,
            internalFileIds = emptySet(),
            externalFileUrls = emptySet(),
            studioMediaIds = emptySet(),
        )

        val repository = getRepository()
        repository.getCourse(1L, false)

        coVerify { htmlParser.createHtmlStringWithLocalFiles(testCourse.courseSyllabus, testCourse.courseId) }
        coVerify { courseDetailsLocalDataSource.saveCourseDetails(testCourse.copy(courseSyllabus = parsedHtml), any()) }
        coVerify { fileSyncRepository.syncHtmlFiles(testCourse.courseId, any()) }
    }

    @Test
    fun `getCourse returns course from local data source when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseDetailsLocalDataSource.getCourse(any()) } returns testCourse

        val repository = getRepository()
        val result = repository.getCourse(1L, false)

        assertEquals(testCourse, result)
        coVerify { courseDetailsLocalDataSource.getCourse(1L) }
    }

    @Test
    fun `getProgramsForCourse returns programs from network data source when online`() = runTest {
        val repository = getRepository()
        val result = repository.getProgramsForCourse(1L, false)

        assertEquals(1, result.size)
        assertEquals("Program 1", result[0].name)
        coVerify { courseDetailsNetworkDataSource.getProgramsForCourse(1L, false) }
    }

    @Test
    fun `getProgramsForCourse with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getProgramsForCourse(1L, true)

        coVerify { courseDetailsNetworkDataSource.getProgramsForCourse(1L, true) }
    }

    @Test
    fun `getProgramsForCourse returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseDetailsLocalDataSource.getProgramsForCourse(any()) } returns testPrograms

        val repository = getRepository()
        val result = repository.getProgramsForCourse(1L, false)

        coVerify { courseDetailsLocalDataSource.getProgramsForCourse(1L) }
        assertEquals(2, result.size)
    }

    @Test
    fun `hasExternalTools returns true when network data source returns true`() = runTest {
        coEvery { courseDetailsNetworkDataSource.hasExternalTools(any(), any()) } returns true
        val repository = getRepository()

        val result = repository.hasExternalTools(1L, false)

        assertTrue(result)
        coVerify { courseDetailsNetworkDataSource.hasExternalTools(1L, false) }
    }

    @Test
    fun `hasExternalTools returns false when network data source returns false`() = runTest {
        coEvery { courseDetailsNetworkDataSource.hasExternalTools(any(), any()) } returns false
        val repository = getRepository()

        val result = repository.hasExternalTools(1L, false)

        assertFalse(result)
    }

    @Test
    fun `hasExternalTools returns false when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        val repository = getRepository()
        val result = repository.hasExternalTools(1L, false)

        assertFalse(result)
    }

    @Test
    fun `hasExternalTools with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.hasExternalTools(1L, true)

        coVerify { courseDetailsNetworkDataSource.hasExternalTools(1L, true) }
    }

    @Test
    fun `getModuleItems returns list of modules with items from network when online`() = runTest {
        val repository = getRepository()
        val result = repository.getModuleItems(1L, false)

        assertEquals(2, result.size)
        assertEquals("Module 1", result[0].name)
        assertEquals(2, result[0].items.size)
        assertEquals("Module 2", result[1].name)
        assertEquals(1, result[1].items.size)
        coVerify { courseProgressNetworkDataSource.getModuleItems(1L, false) }
    }

    @Test
    fun `getModuleItems with forceRefresh true passes it to network data source`() = runTest {
        val repository = getRepository()
        repository.getModuleItems(1L, true)

        coVerify { courseProgressNetworkDataSource.getModuleItems(1L, true) }
    }

    @Test
    fun `getModuleItems returns local data when offline`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseProgressLocalDataSource.getModuleItems(any()) } returns testModules

        val repository = getRepository()
        val result = repository.getModuleItems(1L, false)

        assertEquals(2, result.size)
        coVerify { courseProgressLocalDataSource.getModuleItems(1L) }
    }

    @Test
    fun `getModuleItems returns empty list when no modules`() = runTest {
        coEvery { courseProgressNetworkDataSource.getModuleItems(any(), any()) } returns emptyList()
        val repository = getRepository()
        val result = repository.getModuleItems(1L, false)

        assertEquals(0, result.size)
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
        coEvery { courseDetailsNetworkDataSource.getProgramsForCourse(any(), any()) } returns multiplePrograms

        val repository = getRepository()
        val result = repository.getProgramsForCourse(1L, false)

        assertEquals(2, result.size)
    }

    private fun getRepository(): CourseRepository {
        return CourseRepository(
            courseDetailsNetworkDataSource,
            courseDetailsLocalDataSource,
            courseProgressNetworkDataSource,
            courseProgressLocalDataSource,
            htmlParser,
            fileSyncRepository,
            networkStateProvider,
            featureFlagProvider
        )
    }
}
