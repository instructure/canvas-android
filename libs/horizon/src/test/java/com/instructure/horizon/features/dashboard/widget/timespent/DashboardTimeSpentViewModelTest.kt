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
package com.instructure.horizon.features.dashboard.widget.timespent

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.horizon.features.dashboard.DashboardItemState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardTimeSpentViewModelTest {

    private val repository: DashboardTimeSpentRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: DashboardTimeSpentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init loads time spent data successfully`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Course 1", progress = 0.0),
            CourseWithProgress(courseId = 2L, courseName = "Course 2", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, courseName = "Course 1", minutesPerDay = 300),
                TimeSpentDataEntry(courseId = 2L, courseName = "Course 2", minutesPerDay = 450)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(12.5, state.cardState.hours)
        assertEquals(2, state.cardState.courses.size)
        assertEquals("Course 1", state.cardState.courses[0].name)
        assertEquals("Course 2", state.cardState.courses[1].name)
    }

    @Test
    fun `init handles error gracefully`() = runTest {
        coEvery { repository.getCourses(forceNetwork = false) } throws Exception("Network error")

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `calculates hours from minutesPerDay correctly`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Course 1", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, minutesPerDay = 480)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(8.0, state.cardState.hours)
    }

    @Test
    fun `handles empty data`() = runTest {
        val courses = emptyList<CourseWithProgress>()
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(0.0, state.cardState.hours)
    }

    @Test
    fun `filters time spent data by available courses`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Course 1", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, minutesPerDay = 120),
                TimeSpentDataEntry(courseId = 2L, minutesPerDay = 180),
                TimeSpentDataEntry(courseId = 999L, minutesPerDay = 300)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(2.0, state.cardState.hours)
    }

    @Test
    fun `handles null minutesPerDay gracefully`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Course 1", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, minutesPerDay = null)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(0.0, state.cardState.hours)
    }

    @Test
    fun `onCourseSelected updates selected course id`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Math 101", progress = 0.0),
            CourseWithProgress(courseId = 2L, courseName = "Science 201", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, minutesPerDay = 120),
                TimeSpentDataEntry(courseId = 2L, minutesPerDay = 180)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val initialState = viewModel.uiState.value
        assertEquals(null, initialState.cardState.selectedCourseId)

        initialState.cardState.onCourseSelected("Math 101")

        val updatedState = viewModel.uiState.value
        assertEquals(1L, updatedState.cardState.selectedCourseId)
    }

    @Test
    fun `onCourseSelected with null clears selection`() = runTest {
        val courses = listOf(
            CourseWithProgress(courseId = 1L, courseName = "Math 101", progress = 0.0)
        )
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                TimeSpentDataEntry(courseId = 1L, minutesPerDay = 120)
            )
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        val initialState = viewModel.uiState.value
        initialState.cardState.onCourseSelected("Math 101")

        var updatedState = viewModel.uiState.value
        assertEquals(1L, updatedState.cardState.selectedCourseId)

        updatedState.cardState.onCourseSelected(null)

        updatedState = viewModel.uiState.value
        assertEquals(null, updatedState.cardState.selectedCourseId)
    }

    @Test
    fun `refresh calls repository with forceNetwork true`() = runTest {
        val courses = emptyList<CourseWithProgress>()
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { repository.getCourses(forceNetwork = any()) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = any()) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        var refreshCompleted = false
        viewModel.uiState.value.onRefresh {
            refreshCompleted = true
        }

        coVerify { repository.getTimeSpentData(forceNetwork = true) }
        assertEquals(true, refreshCompleted)
    }

    @Test
    fun `refresh handles error and completes`() = runTest {
        val courses = emptyList<CourseWithProgress>()
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { repository.getCourses(forceNetwork = false) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = false) } returns timeSpentData
        coEvery { repository.getCourses(forceNetwork = true) } throws Exception("Network error")

        viewModel = DashboardTimeSpentViewModel(repository)

        var refreshCompleted = false
        viewModel.uiState.value.onRefresh {
            refreshCompleted = true
        }

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
        assertEquals(true, refreshCompleted)
    }

    @Test
    fun `refresh updates state to loading then success`() = runTest {
        val courses = emptyList<CourseWithProgress>()
        val timeSpentData = TimeSpentWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { repository.getCourses(forceNetwork = any()) } returns courses
        coEvery { repository.getTimeSpentData(courseId = null, forceNetwork = any()) } returns timeSpentData

        viewModel = DashboardTimeSpentViewModel(repository)

        viewModel.uiState.value.onRefresh {}

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
    }
}
