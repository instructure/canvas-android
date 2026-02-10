/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.conference

import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.conference.ConferenceRepository
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.data.repository.group.GroupRepository
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadLiveConferencesUseCaseTest {

    private val conferenceRepository: ConferenceRepository = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val groupRepository: GroupRepository = mockk(relaxed = true)
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist = mockk(relaxed = true)

    private lateinit var useCase: LoadLiveConferencesUseCase

    @Before
    fun setUp() {
        useCase = LoadLiveConferencesUseCase(
            conferenceRepository,
            courseRepository,
            groupRepository,
            conferenceDashboardBlacklist
        )

        every { conferenceDashboardBlacklist.conferenceDashboardBlacklist } returns emptySet()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute returns conference items with course context`() = runTest {
        val course = Course(id = 100, name = "Biology 101")
        val conference = Conference(
            id = 1,
            title = "Conference 1",
            contextType = "Course",
            contextId = 100,
            joinUrl = "https://example.com/join"
        )

        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Success(listOf(conference))
        coEvery { courseRepository.getCourses(true) } returns DataResult.Success(listOf(course))
        coEvery { groupRepository.getGroups(true) } returns DataResult.Success(emptyList())

        val result = useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Biology 101", result[0].subtitle)
        assertEquals("https://example.com/join", result[0].joinUrl)
    }

    @Test
    fun `execute returns conference items with group context`() = runTest {
        val group = Group(id = 200, name = "Study Group")
        val conference = Conference(
            id = 2,
            title = "Group Meeting",
            contextType = "Group",
            contextId = 200,
            joinUrl = null
        )

        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Success(listOf(conference))
        coEvery { courseRepository.getCourses(true) } returns DataResult.Success(emptyList())
        coEvery { groupRepository.getGroups(true) } returns DataResult.Success(listOf(group))

        val result = useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))

        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
        assertEquals("Study Group", result[0].subtitle)
    }

    @Test
    fun `execute filters out blacklisted conferences`() = runTest {
        val course = Course(id = 100, name = "Biology 101")
        val conferences = listOf(
            Conference(id = 1, title = "Conference 1", contextType = "Course", contextId = 100),
            Conference(id = 2, title = "Conference 2", contextType = "Course", contextId = 100),
            Conference(id = 3, title = "Conference 3", contextType = "Course", contextId = 100)
        )

        every { conferenceDashboardBlacklist.conferenceDashboardBlacklist } returns setOf("1", "3")
        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Success(conferences)
        coEvery { courseRepository.getCourses(true) } returns DataResult.Success(listOf(course))
        coEvery { groupRepository.getGroups(true) } returns DataResult.Success(emptyList())

        val result = useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))

        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
    }

    @Test
    fun `execute returns empty list when no conferences`() = runTest {
        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Success(emptyList())

        val result = useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `execute uses conference title as subtitle when course not found`() = runTest {
        val conference = Conference(
            id = 1,
            title = "Conference Title",
            contextType = "Course",
            contextId = 999
        )

        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Success(listOf(conference))
        coEvery { courseRepository.getCourses(true) } returns DataResult.Success(emptyList())
        coEvery { groupRepository.getGroups(true) } returns DataResult.Success(emptyList())

        val result = useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))

        assertEquals(1, result.size)
    }

    @Test(expected = Exception::class)
    fun `execute throws exception when repository fails`() = runTest {
        coEvery { conferenceRepository.getLiveConferences(true) } returns DataResult.Fail()

        useCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))
    }
}