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

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.features.learn.navigation.LearnRoute
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
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
class CourseDetailsViewModelTest {
    private val repository: CourseDetailsRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCourseId = 123L
    private val testCourse = CourseWithProgress(
        courseId = testCourseId,
        courseName = "Test Course",
        courseImageUrl = "https://example.com/course.png",
        progress = 75.0,
        courseSyllabus = "This is the course syllabus"
    )
    private val testPrograms = listOf(
        Program(
            id = "prog1",
            name = "Test Program",
            description = "Test Program description",
            startDate = null,
            endDate = null,
            variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
            courseCompletionCount = 0,
            sortedRequirements = listOf(ProgramRequirement(id = "req1", progressId = "prog1", courseId = testCourseId, required = true))
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getCourse(any(), any()) } returns testCourse
        coEvery { repository.getProgramsForCourse(any(), any()) } returns testPrograms
        coEvery { repository.hasExternalTools(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads course data successfully`() {
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals("Test Course", state.courseName)
        assertEquals(75.0, state.courseProgress)
        assertEquals(testCourseId, state.courseId)
        assertEquals("This is the course syllabus", state.courseSyllabus)
        coVerify { repository.getCourse(testCourseId, false) }
    }

    @Test
    fun `Initial state loads parent programs`() {
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(1, state.parentPrograms.size)
        assertEquals("Test Program", state.parentPrograms[0].name)
        coVerify { repository.getProgramsForCourse(testCourseId, false) }
    }

    @Test
    fun `Initial state includes all tabs when course has external tools`() {
        coEvery { repository.hasExternalTools(any(), any()) } returns true
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(5, state.availableTabs.size)
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Overview))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.MyProgress))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Scores))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Notes))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Tools))
    }

    @Test
    fun `Initial state excludes Tools tab when course has no external tools`() {
        coEvery { repository.hasExternalTools(any(), any()) } returns false
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(4, state.availableTabs.size)
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Overview))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.MyProgress))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Scores))
        assertTrue(state.availableTabs.contains(CourseDetailsTab.Notes))
        assertFalse(state.availableTabs.contains(CourseDetailsTab.Tools))
    }

    @Test
    fun `Loading state shows error when repository fails`() {
        coEvery { repository.getCourse(any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `fetchData with forceRefresh true calls repository with force network`() = runTest {
        val viewModel = getViewModel(testCourseId)

        viewModel.fetchData(forceRefresh = true)

        coVerify { repository.getCourse(testCourseId, true) }
        coVerify { repository.getProgramsForCourse(testCourseId, true) }
        coVerify { repository.hasExternalTools(testCourseId, true) }
    }

    @Test
    fun `Course ID is extracted from SavedStateHandle`() {
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(testCourseId, state.courseId)
        coVerify { repository.getCourse(testCourseId, any()) }
    }

    @Test
    fun `Course with null syllabus uses empty string`() {
        val courseWithNullSyllabus = testCourse.copy(courseSyllabus = null)
        coEvery { repository.getCourse(any(), any()) } returns courseWithNullSyllabus
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals("", state.courseSyllabus)
    }

    @Test
    fun `Empty parent programs list handled correctly`() {
        coEvery { repository.getProgramsForCourse(any(), any()) } returns emptyList()
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(0, state.parentPrograms.size)
    }

    @Test
    fun `Multiple parent programs handled correctly`() {
        val multiplePrograms = listOf(
            Program(
                id = "prog1",
                name = "Program 1",
                description = "Program 1 description",
                startDate = null,
                endDate = null,
                variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
                courseCompletionCount = 0,
                sortedRequirements = listOf(ProgramRequirement(id = "req1", progressId = "prog1", courseId = testCourseId, required = true))
            ),
            Program(
                id = "prog2",
                name = "Program 2",
                description = "Program 2 description",
                startDate = null,
                endDate = null,
                variant = com.instructure.journey.type.ProgramVariantType.LINEAR,
                courseCompletionCount = 0,
                sortedRequirements = listOf(ProgramRequirement(id = "req2", progressId = "prog2", courseId = testCourseId, required = true))
            )
        )
        coEvery { repository.getProgramsForCourse(any(), any()) } returns multiplePrograms
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(2, state.parentPrograms.size)
    }

    @Test
    fun `Invalid course ID defaults to -1`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = CourseDetailsViewModel(savedStateHandle, repository)

        coVerify { repository.getCourse(-1L, any()) }
    }

    @Test
    fun `Course with 0 progress handled correctly`() {
        val courseWithZeroProgress = testCourse.copy(progress = 0.0)
        coEvery { repository.getCourse(any(), any()) } returns courseWithZeroProgress
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(0.0, state.courseProgress)
    }

    @Test
    fun `Course with 100 progress handled correctly`() {
        val courseWithFullProgress = testCourse.copy(progress = 100.0)
        coEvery { repository.getCourse(any(), any()) } returns courseWithFullProgress
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(100.0, state.courseProgress)
    }

    private fun getViewModel(courseId: Long): CourseDetailsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(
            LearnRoute.LearnCourseDetailsScreen.courseIdAttr to courseId
        ))
        return CourseDetailsViewModel(savedStateHandle, repository)
    }
}
