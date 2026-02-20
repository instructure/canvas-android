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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoadVisibleCoursesUseCaseTest {

    private val loadAllCoursesUseCase: LoadAllCoursesUseCase = mockk()
    private val loadDashboardCardsUseCase: LoadDashboardCardsUseCase = mockk()

    private lateinit var useCase: LoadVisibleCoursesUseCase

    @Before
    fun setup() {
        useCase = LoadVisibleCoursesUseCase(loadAllCoursesUseCase, loadDashboardCardsUseCase)
    }

    @Test
    fun `Courses matched by dashboard cards are sorted by card position`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Course A"),
            Course(id = 2, name = "Course B"),
            Course(id = 3, name = "Course C")
        )
        val dashboardCards = listOf(
            DashboardCard(id = 3, position = 0),
            DashboardCard(id = 1, position = 1),
            DashboardCard(id = 2, position = 2)
        )
        coEvery { loadAllCoursesUseCase(any()) } returns courses
        coEvery { loadDashboardCardsUseCase(any()) } returns dashboardCards

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(listOf(3L, 1L, 2L), result.visibleCourses.map { it.id })
        assertEquals("Course C", result.visibleCourses[0].name)
        assertEquals("Course A", result.visibleCourses[1].name)
        assertEquals("Course B", result.visibleCourses[2].name)
    }

    @Test
    fun `Fabricated Course is created for dashboard cards without matching course data`() = runTest {
        val courses = listOf(Course(id = 1, name = "Course A"))
        val dashboardCards = listOf(
            DashboardCard(id = 1, position = 0),
            DashboardCard(id = 99, shortName = "Unsynced", originalName = "Unsynced Course", courseCode = "UC101", position = 1)
        )
        coEvery { loadAllCoursesUseCase(any()) } returns courses
        coEvery { loadDashboardCardsUseCase(any()) } returns dashboardCards

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(2, result.visibleCourses.size)
        val fabricated = result.visibleCourses[1]
        assertEquals(99L, fabricated.id)
        assertEquals("Unsynced", fabricated.name)
        assertEquals("Unsynced Course", fabricated.originalName)
        assertEquals("UC101", fabricated.courseCode)
    }

    @Test
    fun `Fabricated Course uses originalName when shortName is null`() = runTest {
        val dashboardCards = listOf(
            DashboardCard(id = 1, shortName = null, originalName = "Original Name", position = 0)
        )
        coEvery { loadAllCoursesUseCase(any()) } returns emptyList()
        coEvery { loadDashboardCardsUseCase(any()) } returns dashboardCards

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals("Original Name", result.visibleCourses[0].name)
    }

    @Test
    fun `Fabricated Course uses empty string when both shortName and originalName are null`() = runTest {
        val dashboardCards = listOf(
            DashboardCard(id = 1, shortName = null, originalName = null, position = 0)
        )
        coEvery { loadAllCoursesUseCase(any()) } returns emptyList()
        coEvery { loadDashboardCardsUseCase(any()) } returns dashboardCards

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals("", result.visibleCourses[0].name)
    }

    @Test
    fun `Empty dashboard cards returns empty visible courses`() = runTest {
        val courses = listOf(Course(id = 1, name = "Course A"))
        coEvery { loadAllCoursesUseCase(any()) } returns courses
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(emptyList<Course>(), result.visibleCourses)
        assertEquals(courses, result.allCourses)
    }

    @Test
    fun `allCourses includes courses not on the dashboard`() = runTest {
        val courses = listOf(
            Course(id = 1, name = "Visible"),
            Course(id = 2, name = "Not on dashboard")
        )
        val dashboardCards = listOf(DashboardCard(id = 1, position = 0))
        coEvery { loadAllCoursesUseCase(any()) } returns courses
        coEvery { loadDashboardCardsUseCase(any()) } returns dashboardCards

        val result = useCase(LoadVisibleCoursesUseCase.Params())

        assertEquals(1, result.visibleCourses.size)
        assertEquals(2, result.allCourses.size)
        assertEquals(listOf(1L, 2L), result.allCourses.map { it.id })
    }

    @Test
    fun `forceRefresh is propagated to underlying use cases`() = runTest {
        coEvery { loadAllCoursesUseCase(any()) } returns emptyList()
        coEvery { loadDashboardCardsUseCase(any()) } returns emptyList()

        useCase(LoadVisibleCoursesUseCase.Params(forceRefresh = true))

        coVerify { loadAllCoursesUseCase(LoadAllCoursesUseCase.Params(forceRefresh = true)) }
        coVerify { loadDashboardCardsUseCase(LoadDashboardCardsUseCase.Params(forceRefresh = true)) }
    }
}