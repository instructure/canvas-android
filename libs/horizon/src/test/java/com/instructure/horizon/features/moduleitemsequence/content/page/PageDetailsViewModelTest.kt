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
package com.instructure.horizon.features.moduleitemsequence.content.page

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.NoteObjectType
import com.instructure.canvasapi2.models.Page
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.notebook.addedit.add.AddNoteRepository
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
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
class PageDetailsViewModelTest {
    private val repository: PageDetailsRepository = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val addNoteRepository: AddNoteRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseId = 1L
    private val pageUrl = "test-page"
    private val testPage = Page(
        id = 100L,
        url = pageUrl,
        title = "Test Page",
        body = "<p>Test content</p>"
    )

    private val testNotes = listOf(
        Note(
            id = "1",
            objectId = "1",
            objectType = NoteObjectType.PAGE,
            userText = "comment 1",
            highlightedText = NoteHighlightedData(
                selectedText = "highlighted text 1",
                range = NoteHighlightedDataRange(1, 5, "start", "end"),
                textPosition = NoteHighlightedDataTextPosition(1, 5)
            ),
            type = NotebookType.Important,
            updatedAt = Date(),
            courseId = 1,
        ),
        Note(
            id = "2",
            objectId = "1",
            objectType = NoteObjectType.PAGE,
            userText = "comment 2",
            highlightedText = NoteHighlightedData(
                selectedText = "highlighted text 2",
                range = NoteHighlightedDataRange(10, 15, "start", "end"),
                textPosition = NoteHighlightedDataTextPosition(10, 15)
            ),
            type = NotebookType.Confusing,
            updatedAt = Date(),
            courseId = 1,
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns courseId
        every { savedStateHandle.get<String>(ModuleItemContent.Page.PAGE_URL) } returns pageUrl
        coEvery { repository.getPageDetails(any(), any()) } returns testPage
        coEvery { repository.getNotes(any(), any()) } returns testNotes
        coEvery { repository.authenticateUrl(any()) } returns "https://authenticated.url"
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any()) } answers { firstArg() }
        coEvery { addNoteRepository.addNote(any(), any(), any(), any(), any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel loads page details`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertEquals("<p>Test content</p>", viewModel.uiState.value.pageHtmlContent)
        assertEquals(100L, viewModel.uiState.value.pageId)
        assertEquals(pageUrl, viewModel.uiState.value.pageUrl)
        coVerify { repository.getPageDetails(courseId, pageUrl) }
    }

    @Test
    fun `Test HTML content is formatted`() = runTest {
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any()) } returns "formatted html"

        val viewModel = getViewModel()

        assertEquals("formatted html", viewModel.uiState.value.pageHtmlContent)
        coVerify { htmlContentFormatter.formatHtmlWithIframes("<p>Test content</p>") }
    }

    @Test
    fun `Test notes are loaded`() = runTest {
        val viewModel = getViewModel()

        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("comment 1", viewModel.uiState.value.notes.first().userText)
        coVerify { repository.getNotes(courseId, 100L) }
    }

    @Test
    fun `Test notes loading failure does not fail page load`() = runTest {
        coEvery { repository.getNotes(any(), any()) } throws Exception("Notes error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertNotNull(viewModel.uiState.value.pageHtmlContent)
    }

    @Test
    fun `Test LTI button pressed authenticates URL`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.ltiButtonPressed?.invoke("https://lti.url")

        assertEquals("https://authenticated.url", viewModel.uiState.value.urlToOpen)
        coVerify { repository.authenticateUrl("https://lti.url") }
    }

    @Test
    fun `Test LTI authentication failure returns original URL`() = runTest {
        coEvery { repository.authenticateUrl(any()) } throws Exception("Auth error")

        val viewModel = getViewModel()

        viewModel.uiState.value.ltiButtonPressed?.invoke("https://lti.url")

        assertEquals("https://lti.url", viewModel.uiState.value.urlToOpen)
    }

    @Test
    fun `Test URL opened clears URL to open`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.ltiButtonPressed?.invoke("https://lti.url")

        viewModel.uiState.value.onUrlOpened()

        assertNull(viewModel.uiState.value.urlToOpen)
    }

    @Test
    fun `Test add note creates note and refreshes`() = runTest {
        val viewModel = getViewModel()
        val userComment = "This is a user comment"
        val highlightedData = NoteHighlightedData(
            selectedText = "highlighted text",
            range = NoteHighlightedDataRange(1, 5, "start", "end"),
            textPosition = NoteHighlightedDataTextPosition(1, 5)
        )

        viewModel.uiState.value.addNote(highlightedData, userComment)

        coVerify { addNoteRepository.addNote(
            courseId = courseId.toString(),
            objectId = "100",
            objectType = "Page",
            highlightedData = highlightedData,
            userComment = userComment,
            type = any()
        ) }
        coVerify(atLeast = 2) { repository.getNotes(courseId, 100L) }
    }

    @Test
    fun `Test refresh notes updates state`() = runTest {
        val updatedNotes = testNotes + testNotes.last().copy(userText = "New note")
        coEvery { repository.getNotes(any(), any()) } returns testNotes andThen updatedNotes

        val viewModel = getViewModel()
        assertEquals(1, viewModel.uiState.value.notes.size)

        viewModel.refreshNotes()

        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("New note", viewModel.uiState.value.notes.last().userText)
    }

    @Test
    fun `Test refresh notes handles error`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getNotes(any(), any()) } throws Exception("Error")

        // Should not crash
        viewModel.refreshNotes()
    }

    @Test
    fun `Test page load error sets error state`() = runTest {
        coEvery { repository.getPageDetails(any(), any()) } throws Exception("Error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertTrue(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `Test course ID is set in UI state`() = runTest {
        val viewModel = getViewModel()

        assertEquals(courseId, viewModel.uiState.value.courseId)
    }

    private fun getViewModel(): PageDetailsViewModel {
        return PageDetailsViewModel(
            repository,
            htmlContentFormatter,
            addNoteRepository,
            savedStateHandle
        )
    }
}
