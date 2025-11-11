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

import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
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
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotebookViewModelTest {
    private val repository: NotebookRepository = mockk(relaxed = true)
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
        assertFalse(viewModel.uiState.value.hasPreviousPage)
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
    fun `Test load previous page uses start cursor`() = runTest {
        val notesWithPrevious = testNotes.copy(
            pageInfo = testNotes.pageInfo.copy(hasPreviousPage = true)
        )
        coEvery { repository.getNotes(any(), any(), any(), any(), any(), any(), any()) } returns notesWithPrevious

        val viewModel = getViewModel()

        viewModel.uiState.value.loadPreviousPage()

        coVerify { repository.getNotes(after = null, before = "startCursor1", any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test update course id reloads data`() = runTest {
        val viewModel = getViewModel()

        viewModel.updateCourseId(123L)
        viewModel.updateCourseId(1234L)
        viewModel.updateCourseId(123L)

        coVerify(exactly = 2) { repository.getNotes(any(), any(), any(), any(), 123L, any(), any()) }
    }

    @Test
    fun `Test update content with course id hides top bar`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateContent(123L, null)

        assertFalse(viewModel.uiState.value.showTopBar)
        assertTrue(viewModel.uiState.value.showNoteTypeFilter)
    }

    @Test
    fun `Test update content with object type hides filters`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateContent(123L, Pair("Assignment", "456"))

        assertFalse(viewModel.uiState.value.showTopBar)
        assertFalse(viewModel.uiState.value.showNoteTypeFilter)
    }

    @Test
    fun `Test update content without course id shows top bar and filters`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateContent(null, null)

        assertTrue(viewModel.uiState.value.showTopBar)
        assertTrue(viewModel.uiState.value.showNoteTypeFilter)
    }

    private fun getViewModel(): NotebookViewModel {
        return NotebookViewModel(repository)
    }
}
