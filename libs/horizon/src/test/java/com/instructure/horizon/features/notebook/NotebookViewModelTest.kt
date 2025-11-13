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
package com.instructure.horizon.features.notebook

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotebookViewModelTest {
    private val repository: NotebookRepository = mockk(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testNotes = QueryNotesQuery.Notes(
        edges = listOf(
            QueryNotesQuery.Edge(
                cursor = "cursor1",
                node = QueryNotesQuery.Node(
                    id = "note1",
                    userText = "Test note 1",
                    createdAt = Date(),
                    updatedAt = Date(),
                    rootAccountUuid = "",
                    userId = "1",
                    courseId = "1",
                    objectId = "1",
                    objectType = "Assignment",
                    reaction = listOf(""),
                    highlightData = "test"
                )
            )
        ),
        pageInfo = QueryNotesQuery.PageInfo(
            hasNextPage = true,
            hasPreviousPage = false,
            endCursor = "endCursor1",
            startCursor = "startCursor1"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) } returns testNotes
        coEvery { repository.getCourses(any()) } returns DataResult.Success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads successfully on init`() {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        coVerify { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test notes are loaded`() {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.notes.isNotEmpty())
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
    }

    @Test
    fun `Test pagination info is updated`() {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.hasNextPage)
    }

    @Test
    fun `Test filter selection updates state and reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterSelected(NotebookType.Important)

        assertEquals(NotebookType.Important, viewModel.uiState.value.selectedFilter)
        coVerify(atLeast = 2) { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test filter selection with null clears filter`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onFilterSelected(null)

        assertEquals(null, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `Test load next page uses end cursor`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadNextPage()

        coVerify { repository.getNotes(after = "endCursor1", before = null, any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test update course id reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.updateFilters(123L)
        viewModel.updateFilters(1234L)
        viewModel.updateFilters(123L)

        coVerify(exactly = 2) { repository.getNotes(any(), any(), any(), any(), 123L, any(), any()) }
    }

    @Test
    fun `Test loadCourses success populates courses`() = runTest {
        val mockCourses = listOf(
            mockk<CourseWithProgress>(relaxed = true),
            mockk<CourseWithProgress>(relaxed = true)
        )
        coEvery { repository.getCourses(any()) } returns DataResult.Success(mockCourses)

        val viewModel = getViewModel()

        assertEquals(mockCourses, viewModel.uiState.value.courses)
    }

    @Test
    fun `Test loadCourses failure sets empty courses`() = runTest {
        coEvery { repository.getCourses(any()) } returns DataResult.Fail()

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.courses.isEmpty())
    }

    @Test
    fun `Test onCourseSelected updates state and reloads data`() = runTest {
        val mockCourse = mockk<CourseWithProgress>(relaxed = true) {
            every { courseId } returns 123L
        }
        val viewModel = getViewModel()

        viewModel.uiState.value.onCourseSelected(mockCourse)

        assertEquals(mockCourse, viewModel.uiState.value.selectedCourse)
        coVerify(atLeast = 1) { repository.getNotes(any(), any(), any(), any(), 123L, any(), any()) }
    }

    @Test
    fun `Test onCourseSelected with null clears course`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onCourseSelected(null)

        assertNull(viewModel.uiState.value.selectedCourse)
    }

    @Test
    fun `Test updateScreenState changes visibility flags`() = runTest {
        val viewModel = getViewModel()

        viewModel.updateScreenState(
            showNoteTypeFilter = false,
            showCourseFilter = true,
            showTopBar = true
        )

        assertFalse(viewModel.uiState.value.showNoteTypeFilter)
        assertTrue(viewModel.uiState.value.showCourseFilter)
        assertTrue(viewModel.uiState.value.showTopBar)
    }

    @Test
    fun `Test updateContent with different courseId reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateContent(456L, Pair("Assignment", "123"))

        coVerify(atLeast = 1) { repository.getNotes(any(), any(), any(), any(), 456L, Pair("Assignment", "123"), any()) }
    }

    @Test
    fun `Test updateContent with same courseId does not reload data`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateContent(null, null)

        coVerify(exactly = 1) { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test loadNextPage triggers with valid next page`() = runTest {
        coEvery { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) } returns testNotes.copy(
            pageInfo = testNotes.pageInfo.copy(hasNextPage = true)
        )
        val viewModel = getViewModel()

        viewModel.uiState.value.loadNextPage()

        coVerify(atLeast = 2) { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test loadNextPage does not trigger when no next page`() = runTest {
        val notesWithoutNext = testNotes.copy(
            pageInfo = testNotes.pageInfo.copy(hasNextPage = false)
        )
        coEvery { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) } returns notesWithoutNext

        val viewModel = getViewModel()

        viewModel.uiState.value.loadNextPage()

        coVerify(exactly = 1) { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test course filter is hidden when courseId is present`() = runTest {
        savedStateHandle["courseId"] = "123"

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.showCourseFilter)
    }

    private fun getViewModel(): NotebookViewModel {
        return NotebookViewModel(repository, savedStateHandle)
    }
}
