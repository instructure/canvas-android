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
package com.instructure.horizon.features.learn.course.list

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnCourseListViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: LearnCourseListRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCourses = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Introduction to Programming",
            courseImageUrl = "https://example.com/prog.png",
            progress = 0.0,
            courseSyllabus = "Learn programming basics"
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Advanced Mathematics",
            courseImageUrl = "https://example.com/math.png",
            progress = 50.0,
            courseSyllabus = "Advanced math topics"
        ),
        CourseWithProgress(
            courseId = 3L,
            courseName = "Web Development",
            courseImageUrl = "https://example.com/web.png",
            progress = 100.0,
            courseSyllabus = "Build modern websites"
        ),
        CourseWithProgress(
            courseId = 4L,
            courseName = "Data Science",
            courseImageUrl = "https://example.com/data.png",
            progress = 25.0,
            courseSyllabus = "Analyze data"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCoursesWithProgress(any()) } returns testCourses
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads courses successfully`() {
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(4, state.coursesToDisplay.size)
        coVerify { repository.getCoursesWithProgress(false) }
    }

    @Test
    fun `Initial state sets default filter to All`() {
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(LearnCourseFilterOption.All, state.selectedFilterValue)
    }

    @Test
    fun `Initial state sets visible item count to 10`() {
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals(10, state.visibleItemCount)
    }

    @Test
    fun `Initial state has empty search query`() {
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertEquals("", state.searchQuery.text)
    }

    @Test
    fun `Loading state shows error when repository fails`() {
        coEvery { repository.getCoursesWithProgress(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `Filter by NotStarted shows only courses with 0 progress`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.NotStarted)

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Introduction to Programming", state.coursesToDisplay[0].courseName)
        assertEquals(0.0, state.coursesToDisplay[0].progress)
    }

    @Test
    fun `Filter by InProgress shows only courses with 0-100 progress`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.InProgress)

        val state = viewModel.state.value
        assertEquals(2, state.coursesToDisplay.size)
        assertTrue(state.coursesToDisplay.any { it.courseName == "Advanced Mathematics" })
        assertTrue(state.coursesToDisplay.any { it.courseName == "Data Science" })
    }

    @Test
    fun `Filter by Completed shows only courses with 100 progress`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.Completed)

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Web Development", state.coursesToDisplay[0].courseName)
        assertEquals(100.0, state.coursesToDisplay[0].progress)
    }

    @Test
    fun `Filter by All shows all courses`() {
        val viewModel = getViewModel()
        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.NotStarted)

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.All)

        val state = viewModel.state.value
        assertEquals(4, state.coursesToDisplay.size)
    }

    @Test
    fun `Search query filters courses by name case-insensitive`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateSearchQuery(TextFieldValue("programming"))

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Introduction to Programming", state.coursesToDisplay[0].courseName)
    }

    @Test
    fun `Search query with partial match filters correctly`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateSearchQuery(TextFieldValue("dev"))

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Web Development", state.coursesToDisplay[0].courseName)
    }

    @Test
    fun `Search query with no match returns empty list`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateSearchQuery(TextFieldValue("NonExistentCourse"))

        val state = viewModel.state.value
        assertEquals(0, state.coursesToDisplay.size)
    }

    @Test
    fun `Search query trims whitespace`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateSearchQuery(TextFieldValue("  mathematics  "))

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Advanced Mathematics", state.coursesToDisplay[0].courseName)
    }

    @Test
    fun `Combined filter and search works correctly`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.InProgress)
        viewModel.state.value.updateSearchQuery(TextFieldValue("math"))

        val state = viewModel.state.value
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("Advanced Mathematics", state.coursesToDisplay[0].courseName)
    }

    @Test
    fun `Combined filter and search with no match returns empty list`() {
        val viewModel = getViewModel()

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.Completed)
        viewModel.state.value.updateSearchQuery(TextFieldValue("programming"))

        val state = viewModel.state.value
        assertEquals(0, state.coursesToDisplay.size)
    }

    @Test
    fun `increaseVisibleItemCount increases count by 10`() {
        val viewModel = getViewModel()
        val initialCount = viewModel.state.value.visibleItemCount

        viewModel.state.value.increaseVisibleItemCount()

        val state = viewModel.state.value
        assertEquals(initialCount + 10, state.visibleItemCount)
    }

    @Test
    fun `Multiple increaseVisibleItemCount calls accumulate`() {
        val viewModel = getViewModel()

        viewModel.state.value.increaseVisibleItemCount()
        viewModel.state.value.increaseVisibleItemCount()

        val state = viewModel.state.value
        assertEquals(30, state.visibleItemCount)
    }

    @Test
    fun `Refresh calls repository with forceNetwork true`() {
        val viewModel = getViewModel()

        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.getCoursesWithProgress(true) }
    }

    @Test
    fun `Refresh updates courses list`() {
        val viewModel = getViewModel()
        val updatedCourses = listOf(
            CourseWithProgress(
                courseId = 5L,
                courseName = "New Course",
                courseImageUrl = "https://example.com/new.png",
                progress = 0.0,
                courseSyllabus = "New syllabus"
            )
        )
        coEvery { repository.getCoursesWithProgress(true) } returns updatedCourses

        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isRefreshing)
        assertEquals(1, state.coursesToDisplay.size)
        assertEquals("New Course", state.coursesToDisplay[0].courseName)
    }

    @Test
    fun `Refresh on error shows snackbar message`() {
        val viewModel = getViewModel()
        coEvery { repository.getCoursesWithProgress(true) } throws Exception("Network error")

        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isRefreshing)
        assertTrue(state.loadingState.snackbarMessage != null)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() {
        val viewModel = getViewModel()
        coEvery { repository.getCoursesWithProgress(true) } throws Exception("Network error")
        viewModel.state.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.value.loadingState.onSnackbarDismiss()

        val state = viewModel.state.value
        assertNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `Courses are mapped correctly to LearnCourseState`() {
        val viewModel = getViewModel()

        val state = viewModel.state.value
        val firstCourse = state.coursesToDisplay[0]
        assertEquals(1L, firstCourse.courseId)
        assertEquals("Introduction to Programming", firstCourse.courseName)
        assertEquals("https://example.com/prog.png", firstCourse.imageUrl)
        assertEquals(0.0, firstCourse.progress)
    }

    @Test
    fun `Changing filter resets to filtered view of all loaded courses`() {
        val viewModel = getViewModel()
        viewModel.state.value.updateSearchQuery(TextFieldValue("programming"))

        viewModel.state.value.updateFilterValue(LearnCourseFilterOption.InProgress)

        val state = viewModel.state.value
        assertEquals(0, state.coursesToDisplay.size)
    }

    @Test
    fun `Empty courses list loads successfully`() {
        coEvery { repository.getCoursesWithProgress(any()) } returns emptyList()
        val viewModel = getViewModel()

        val state = viewModel.state.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(0, state.coursesToDisplay.size)
    }

    private fun getViewModel(): LearnCourseListViewModel {
        return LearnCourseListViewModel(context, repository)
    }
}
