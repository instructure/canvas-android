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
import com.instructure.horizon.domain.usecase.GetCourseDetailsUseCase
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetProgramsForCourseUseCase
import com.instructure.horizon.domain.usecase.HasExternalToolsUseCase
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.offline.HorizonOfflineState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailsViewModelTest {
    private val getCourseDetailsUseCase: GetCourseDetailsUseCase = mockk(relaxed = true)
    private val getProgramsForCourseUseCase: GetProgramsForCourseUseCase = mockk(relaxed = true)
    private val hasExternalToolsUseCase: HasExternalToolsUseCase = mockk(relaxed = true)
    private val offlineState: HorizonOfflineState = mockk(relaxed = true)
    private val isOfflineFlow = MutableStateFlow(false)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
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
        every { offlineState.isOffline } returns isOfflineFlow
        coEvery { getCourseDetailsUseCase(any()) } returns testCourse
        coEvery { getProgramsForCourseUseCase(any()) } returns testPrograms
        coEvery { hasExternalToolsUseCase(any()) } returns false
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
        coVerify { getCourseDetailsUseCase(GetCourseDetailsUseCase.Params(testCourseId, false)) }
    }

    @Test
    fun `Initial state loads parent programs`() {
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(1, state.parentPrograms.size)
        assertEquals("Test Program", state.parentPrograms[0].name)
        coVerify { getProgramsForCourseUseCase(GetProgramsForCourseUseCase.Params(testCourseId, false)) }
    }

    @Test
    fun `Initial state includes all tabs when course has external tools`() {
        coEvery { hasExternalToolsUseCase(any()) } returns true
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
        coEvery { hasExternalToolsUseCase(any()) } returns false
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
    fun `Loading state shows error when use case fails`() {
        coEvery { getCourseDetailsUseCase(any()) } throws Exception("Network error")
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `fetchData with forceRefresh true calls use cases with force network`() = runTest {
        val viewModel = getViewModel(testCourseId)

        viewModel.fetchData(forceRefresh = true)

        coVerify { getCourseDetailsUseCase(GetCourseDetailsUseCase.Params(testCourseId, true)) }
        coVerify { getProgramsForCourseUseCase(GetProgramsForCourseUseCase.Params(testCourseId, true)) }
        coVerify { hasExternalToolsUseCase(HasExternalToolsUseCase.Params(testCourseId, true)) }
    }

    @Test
    fun `Course ID is extracted from SavedStateHandle`() {
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(testCourseId, state.courseId)
        coVerify { getCourseDetailsUseCase(match { it.courseId == testCourseId }) }
    }

    @Test
    fun `Course with null syllabus uses empty string`() {
        val courseWithNullSyllabus = testCourse.copy(courseSyllabus = null)
        coEvery { getCourseDetailsUseCase(any()) } returns courseWithNullSyllabus
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals("", state.courseSyllabus)
    }

    @Test
    fun `Empty parent programs list handled correctly`() {
        coEvery { getProgramsForCourseUseCase(any()) } returns emptyList()
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
        coEvery { getProgramsForCourseUseCase(any()) } returns multiplePrograms
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(2, state.parentPrograms.size)
    }

    @Test
    fun `Invalid course ID defaults to -1`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)

        coVerify { getCourseDetailsUseCase(match { it.courseId == -1L }) }
    }

    @Test
    fun `Course with 0 progress handled correctly`() {
        val courseWithZeroProgress = testCourse.copy(progress = 0.0)
        coEvery { getCourseDetailsUseCase(any()) } returns courseWithZeroProgress
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(0.0, state.courseProgress)
    }

    @Test
    fun `Course with 100 progress handled correctly`() {
        val courseWithFullProgress = testCourse.copy(progress = 100.0)
        coEvery { getCourseDetailsUseCase(any()) } returns courseWithFullProgress
        val viewModel = getViewModel(testCourseId)

        val state = viewModel.state.value
        assertEquals(100.0, state.courseProgress)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle): CourseDetailsViewModel {
        return CourseDetailsViewModel(
            savedStateHandle,
            getCourseDetailsUseCase,
            getProgramsForCourseUseCase,
            hasExternalToolsUseCase,
            offlineState,
            getLastSyncedAtUseCase,
        )
    }

    private fun getViewModel(courseId: Long): CourseDetailsViewModel {
        return createViewModel(
            SavedStateHandle(mapOf(LearnRoute.LearnCourseDetailsScreen.courseIdAttr to courseId))
        )
    }
}
