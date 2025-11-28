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
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardCourseListViewModelTest {
    private val repository: DashboardCourseListRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCourses = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Course 1",
            courseSyllabus = "",
            progress = 0.0
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Course 2",
            courseSyllabus = "",
            progress = 50.0
        ),
        CourseWithProgress(
            courseId = 3L,
            courseName = "Course 3",
            courseSyllabus = "",
            progress = 100.0
        )
    )

    private val testPrograms = listOf(
        Program(
            id = "program-1",
            name = "Program 1",
            description = null,
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
            description = null,
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR,
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

    private fun createManyCourses(count: Int): List<CourseWithProgress> {
        return (1..count).map { index ->
            CourseWithProgress(
                courseId = index.toLong(),
                courseName = "Course $index",
                courseSyllabus = "",
                progress = (index * 10).toDouble() % 100
            )
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCourses(any()) } returns testCourses
        coEvery { repository.getPrograms(any()) } returns testPrograms
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test successful data load updates UI state`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value

        assertFalse(state.loadingState.isLoading)
        assertEquals(3, state.courses.size)
        assertEquals(10, state.visibleCourseCount)
    }

    @Test
    fun `Test courses are loaded from repository`() = runTest {
        val viewModel = getViewModel()

        coVerify { repository.getCourses(false) }
        assertEquals(3, viewModel.uiState.value.courses.size)
    }

    @Test
    fun `Test programs are loaded from repository`() = runTest {
        val viewModel = getViewModel()

        coVerify { repository.getPrograms(false) }
    }

    @Test
    fun `Test courses are sorted with active courses first`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value

        assertEquals(2L, state.courses[0].courseId)
        assertEquals(1L, state.courses[1].courseId)
        assertEquals(3L, state.courses[2].courseId)
    }

    @Test
    fun `Test parent programs are mapped correctly`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value

        val course1 = state.courses.find { it.courseId == 1L }
        assertEquals(1, course1?.parentPrograms?.size)
        assertEquals("Program 1", course1?.parentPrograms?.get(0)?.programName)

        val course2 = state.courses.find { it.courseId == 2L }
        assertEquals(1, course2?.parentPrograms?.size)
        assertEquals("Program 2", course2?.parentPrograms?.get(0)?.programName)

        val course3 = state.courses.find { it.courseId == 3L }
        assertEquals(0, course3?.parentPrograms?.size)
    }

    @Test
    fun `Test filter All shows all courses`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterOptionSelected(DashboardCourseListFilterOption.All)

        val state = viewModel.uiState.value
        assertEquals(DashboardCourseListFilterOption.All, state.selectedFilterOption)
        assertEquals(3, state.courses.size)
    }

    @Test
    fun `Test filter NotStarted shows only courses with 0 progress`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterOptionSelected(DashboardCourseListFilterOption.NotStarted)

        val state = viewModel.uiState.value
        assertEquals(DashboardCourseListFilterOption.NotStarted, state.selectedFilterOption)
        assertEquals(1, state.courses.size)
        assertEquals(0.0, state.courses[0].progress)
    }

    @Test
    fun `Test filter InProgress shows only courses with progress between 0 and 100`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterOptionSelected(DashboardCourseListFilterOption.InProgress)

        val state = viewModel.uiState.value
        assertEquals(DashboardCourseListFilterOption.InProgress, state.selectedFilterOption)
        assertEquals(1, state.courses.size)
        assertEquals(50.0, state.courses[0].progress)
    }

    @Test
    fun `Test filter Completed shows only courses with 100 progress`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterOptionSelected(DashboardCourseListFilterOption.Completed)

        val state = viewModel.uiState.value
        assertEquals(DashboardCourseListFilterOption.Completed, state.selectedFilterOption)
        assertEquals(1, state.courses.size)
        assertEquals(100.0, state.courses[0].progress)
    }

    @Test
    fun `Test initial visible course count is 10`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(10, state.visibleCourseCount)
    }

    @Test
    fun `Test onShowMoreCourses increases visible count by 10`() = runTest {
        coEvery { repository.getCourses(any()) } returns createManyCourses(30)

        val viewModel = getViewModel()

        assertEquals(10, viewModel.uiState.value.visibleCourseCount)

        viewModel.uiState.value.onShowMoreCourses()

        assertEquals(20, viewModel.uiState.value.visibleCourseCount)

        viewModel.uiState.value.onShowMoreCourses()

        assertEquals(30, viewModel.uiState.value.visibleCourseCount)
    }

    @Test
    fun `Test filter change resets visible course count to 10`() = runTest {
        coEvery { repository.getCourses(any()) } returns createManyCourses(30)

        val viewModel = getViewModel()

        viewModel.uiState.value.onShowMoreCourses()
        viewModel.uiState.value.onShowMoreCourses()

        assertEquals(30, viewModel.uiState.value.visibleCourseCount)

        viewModel.uiState.value.onFilterOptionSelected(DashboardCourseListFilterOption.InProgress)

        assertEquals(10, viewModel.uiState.value.visibleCourseCount)
    }

    @Test
    fun `Test refresh resets visible course count to 10`() = runTest {
        coEvery { repository.getCourses(any()) } returns createManyCourses(30)

        val viewModel = getViewModel()

        viewModel.uiState.value.onShowMoreCourses()
        viewModel.uiState.value.onShowMoreCourses()

        assertEquals(30, viewModel.uiState.value.visibleCourseCount)

        viewModel.uiState.value.loadingState.onRefresh()

        assertEquals(10, viewModel.uiState.value.visibleCourseCount)
    }

    @Test
    fun `Test failed data load sets loading to false`() = runTest {
        coEvery { repository.getCourses(any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Test refresh calls repository with forceRefresh true`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { repository.getCourses(true) }
        coVerify { repository.getPrograms(true) }
    }

    @Test
    fun `Test refresh updates isRefreshing state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
    }

    @Test
    fun `Test empty courses list does not crash`() = runTest {
        coEvery { repository.getCourses(any()) } returns emptyList()

        val viewModel = getViewModel()

        assertEquals(0, viewModel.uiState.value.courses.size)
        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    private fun getViewModel(): DashboardCourseListViewModel {
        return DashboardCourseListViewModel(repository)
    }
}
