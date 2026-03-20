/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.program.list

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.R
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class LearnProgramListViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnProgramListRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testPrograms = listOf(
        createTestProgram(
            id = "program1",
            name = "Software Engineering",
            requirements = listOf(
                createTestProgramRequirement(courseId = 1L, progress = 0.0)
            )
        ),
        createTestProgram(
            id = "program2",
            name = "Data Science",
            requirements = listOf(
                createTestProgramRequirement(courseId = 2L, progress = 50.0)
            )
        ),
        createTestProgram(
            id = "program3",
            name = "Web Development",
            requirements = listOf(
                createTestProgramRequirement(courseId = 3L, progress = 100.0)
            )
        ),
        createTestProgram(
            id = "program4",
            name = "Machine Learning",
            requirements = listOf(
                createTestProgramRequirement(courseId = 4L, progress = 25.0)
            )
        )
    )

    private val testCourses = listOf(
        createTestCourse(courseId = 1L, courseName = "Intro to Programming"),
        createTestCourse(courseId = 2L, courseName = "Data Analysis"),
        createTestCourse(courseId = 3L, courseName = "React Development"),
        createTestCourse(courseId = 4L, courseName = "Neural Networks")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getString(any()) } returns ""
        every { context.getString(any(), any()) } returns ""
        every { context.getString(any(), any(), any()) } returns ""
        every { resources.getQuantityString(any(), any(), any()) } returns "2 courses"
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), any()) } returns ""
        coEvery { repository.getPrograms(any()) } returns testPrograms
        coEvery { repository.getCoursesById(any(), any()) } returns testCourses
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads programs successfully`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(4, state.filteredPrograms.size)
        coVerify { repository.getPrograms(false) }
    }

    @Test
    fun `Initial state sets default filter to All`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(LearnProgramFilterOption.All, state.selectedFilterValue)
    }

    @Test
    fun `Initial state sets visible item count to 10`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(10, state.visibleItemCount)
    }

    @Test
    fun `Initial state has empty search query`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery.text)
    }

    @Test
    fun `Loading state shows error when repository fails`() {
        coEvery { repository.getPrograms(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `Empty programs list loads successfully`() {
        coEvery { repository.getPrograms(any()) } returns emptyList()
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(0, state.filteredPrograms.size)
    }

    @Test
    fun `Filter by NotStarted shows only programs with 0 progress`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.NotStarted)

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Software Engineering", state.filteredPrograms[0].programName)
        assertEquals(0.0, state.filteredPrograms[0].programProgress)
    }

    @Test
    fun `Filter by InProgress shows only programs with 0-100 progress`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.InProgress)

        val state = viewModel.uiState.value
        assertEquals(2, state.filteredPrograms.size)
        assertTrue(state.filteredPrograms.any { it.programName == "Data Science" })
        assertTrue(state.filteredPrograms.any { it.programName == "Machine Learning" })
    }

    @Test
    fun `Filter by Completed shows only programs with 100 progress`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.Completed)

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Web Development", state.filteredPrograms[0].programName)
        assertEquals(100.0, state.filteredPrograms[0].programProgress)
    }

    @Test
    fun `Filter by All shows all programs`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.NotStarted)

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.All)

        val state = viewModel.uiState.value
        assertEquals(4, state.filteredPrograms.size)
    }

    @Test
    fun `Search query filters programs by name case-insensitive`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("engineering"))

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Software Engineering", state.filteredPrograms[0].programName)
    }

    @Test
    fun `Search query with partial match filters correctly`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("dev"))

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Web Development", state.filteredPrograms[0].programName)
    }

    @Test
    fun `Search query with no match returns empty list`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("NonExistentProgram"))

        val state = viewModel.uiState.value
        assertEquals(0, state.filteredPrograms.size)
    }

    @Test
    fun `Search query trims whitespace`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("  data science  "))

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Data Science", state.filteredPrograms[0].programName)
    }

    @Test
    fun `Combined filter and search works correctly`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.InProgress)
        viewModel.uiState.value.updateSearchQuery(TextFieldValue("data"))

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("Data Science", state.filteredPrograms[0].programName)
    }

    @Test
    fun `Combined filter and search with no match returns empty list`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.Completed)
        viewModel.uiState.value.updateSearchQuery(TextFieldValue("engineering"))

        val state = viewModel.uiState.value
        assertEquals(0, state.filteredPrograms.size)
    }

    @Test
    fun `increaseVisibleItemCount increases count by 10`() {
        val viewModel = getViewModel()
        val initialCount = viewModel.uiState.value.visibleItemCount

        viewModel.uiState.value.increaseVisibleItemCount()

        val state = viewModel.uiState.value
        assertEquals(initialCount + 10, state.visibleItemCount)
    }

    @Test
    fun `Multiple increaseVisibleItemCount calls accumulate`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.increaseVisibleItemCount()
        viewModel.uiState.value.increaseVisibleItemCount()

        val state = viewModel.uiState.value
        assertEquals(30, state.visibleItemCount)
    }

    @Test
    fun `Refresh calls repository with forceNetwork true`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.getPrograms(true) }
    }

    @Test
    fun `Refresh updates programs list`() {
        val viewModel = getViewModel()
        val updatedPrograms = listOf(
            createTestProgram(
                id = "program5",
                name = "New Program",
                requirements = listOf(
                    createTestProgramRequirement(courseId = 5L, progress = 0.0)
                )
            )
        )
        coEvery { repository.getPrograms(true) } returns updatedPrograms
        coEvery { repository.getCoursesById(any(), any()) } returns listOf(
            createTestCourse(courseId = 5L, courseName = "New Course")
        )

        viewModel.uiState.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertEquals(1, state.filteredPrograms.size)
        assertEquals("New Program", state.filteredPrograms[0].programName)
    }

    @Test
    fun `Refresh on error shows snackbar message`() {
        val viewModel = getViewModel()
        every { context.getString(R.string.learnProgramListFailedToLoadMessage) } returns "Failed to load"
        coEvery { repository.getPrograms(true) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertTrue(state.loadingState.snackbarMessage != null)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() {
        val viewModel = getViewModel()
        every { context.getString(R.string.learnProgramListFailedToLoadMessage) } returns "Failed to load"
        coEvery { repository.getPrograms(true) } throws Exception("Network error")
        viewModel.uiState.value.loadingState.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        val state = viewModel.uiState.value
        assertNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `Programs are mapped correctly to LearnProgramState`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstProgram = state.filteredPrograms[0]
        assertEquals("program1", firstProgram.programId)
        assertEquals("Software Engineering", firstProgram.programName)
        assertEquals(0.0, firstProgram.programProgress)
    }

    @Test
    fun `Program chips are created with correct course count and duration`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstProgram = state.filteredPrograms[0]
        assertTrue(firstProgram.programChips.isNotEmpty())
    }

    @Test
    fun `Changing filter resets to filtered view of all loaded programs`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.updateSearchQuery(TextFieldValue("engineering"))

        viewModel.uiState.value.updateFilterValue(LearnProgramFilterOption.InProgress)

        val state = viewModel.uiState.value
        assertEquals(0, state.filteredPrograms.size)
    }

    private fun getViewModel(): LearnProgramListViewModel {
        return LearnProgramListViewModel(context, resources, repository)
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
