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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.data.repository.dashboardcourses.DashboardCoursesData
import com.instructure.pandautils.data.repository.dashboardcourses.DashboardCoursesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadVisibleCoursesUseCaseTest {

    private val dashboardCoursesRepository: DashboardCoursesRepository = mockk()
    private val loadDashboardCardsUseCase: LoadDashboardCardsUseCase = mockk()

    private lateinit var useCase: LoadVisibleCoursesUseCase

    @Before
    fun setup() {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(emptyList())
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()
        useCase = LoadVisibleCoursesUseCase(dashboardCoursesRepository, loadDashboardCardsUseCase)
    }

    @Test
    fun `visible courses match dashboard cards sorted by position`() = runTest {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(
            listOf(Course(id = 1), Course(id = 2), Course(id = 3))
        )
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(
            DashboardCard(id = 3, position = 0),
            DashboardCard(id = 1, position = 1),
            DashboardCard(id = 2, position = 2)
        )

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(listOf(3L, 1L, 2L), result.visibleCourses.map { it.id })
    }

    @Test
    fun `only dashboard card courses are visible`() = runTest {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(
            listOf(Course(id = 1), Course(id = 2))
        )
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(
            DashboardCard(id = 1, position = 0)
        )

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(1, result.visibleCourses.size)
        assertEquals(1L, result.visibleCourses[0].id)
        assertEquals(2, result.allCourses.size)
    }

    @Test
    fun `empty dashboard cards returns empty visible courses`() = runTest {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(
            listOf(Course(id = 1), Course(id = 2))
        )
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertTrue(result.visibleCourses.isEmpty())
        assertEquals(2, result.allCourses.size)
    }

    @Test
    fun `dashboard card nickname overrides course name`() = runTest {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(
            listOf(Course(id = 1, name = "Original Name"))
        )
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(
            DashboardCard(id = 1, position = 0, shortName = "Nickname")
        )

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        val course = result.visibleCourses[0]
        assertEquals("Nickname", course.name)
        assertEquals("Original Name", course.originalName)
    }

    @Test
    fun `announcements map is passed through from the repository`() = runTest {
        val announcements = mapOf(1L to listOf(DiscussionTopicHeader(id = 10, title = "Hello", announcement = true)))
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(
            courses = listOf(Course(id = 1)),
            announcementsMap = announcements
        )
        coEvery { loadDashboardCardsUseCase(any()) } returns listOf(DashboardCard(id = 1, position = 0))

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(announcements, result.announcementsMap)
    }

    @Test
    fun `forceRefresh is propagated to repository and dashboard cards`() = runTest {
        useCase(LoadVisibleCoursesUseCase.Params(forceRefresh = true))

        coVerify { dashboardCoursesRepository.getCourses(forceRefresh = true) }
        coVerify { loadDashboardCardsUseCase(LoadDashboardCardsUseCase.Params(forceRefresh = true)) }
    }

    @Test
    fun `empty courses returns empty result`() = runTest {
        coEvery { dashboardCoursesRepository.getCourses(any()) } returns DashboardCoursesData(emptyList())
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertTrue(result.visibleCourses.isEmpty())
        assertTrue(result.allCourses.isEmpty())
        assertTrue(result.announcementsMap.isEmpty())
    }
}
