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
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.domain.usecase.GetPageDetailsUseCase
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.notebook.addedit.add.AddNoteRepository
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
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
class PageDetailsViewModelTest {
    private val getPageDetailsUseCase: GetPageDetailsUseCase = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val redwoodApi: RedwoodApiManager = mockk(relaxed = true)
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

    private fun makeNoteNode(id: String, userText: String) = QueryNotesQuery.Node(
        id = id,
        userText = userText,
        createdAt = Date(),
        updatedAt = Date(),
        rootAccountUuid = "",
        userId = "1",
        courseId = courseId.toString(),
        objectId = testPage.id.toString(),
        objectType = "Page",
        reaction = listOf("Important"),
        highlightData = ""
    )

    private val testNotesResponse = QueryNotesQuery.Notes(
        edges = listOf(
            QueryNotesQuery.Edge(cursor = "cursor1", node = makeNoteNode("1", "comment 1")),
            QueryNotesQuery.Edge(cursor = "cursor2", node = makeNoteNode("2", "comment 2")),
        ),
        pageInfo = QueryNotesQuery.PageInfo(
            hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns courseId
        every { savedStateHandle.get<String>(ModuleItemContent.Page.PAGE_URL) } returns pageUrl
        coEvery { getPageDetailsUseCase(any()) } returns testPage
        coEvery { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) } returns testNotesResponse
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns
            DataResult.Success(AuthenticatedSession(sessionUrl = "https://authenticated.url"))
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any(), any()) } answers { firstArg() }
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
        coVerify { getPageDetailsUseCase(GetPageDetailsUseCase.Params(courseId, pageUrl)) }
    }

    @Test
    fun `Test HTML content is formatted`() = runTest {
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any(), any()) } returns "formatted html"

        val viewModel = getViewModel()

        assertEquals("formatted html", viewModel.uiState.value.pageHtmlContent)
        coVerify { htmlContentFormatter.formatHtmlWithIframes("<p>Test content</p>", courseId) }
    }

    @Test
    fun `Test notes are loaded`() = runTest {
        val viewModel = getViewModel()

        assertEquals(2, viewModel.uiState.value.notes.size)
        assertEquals("comment 1", viewModel.uiState.value.notes.first().userText)
        coVerify { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test notes loading failure does not fail page load`() = runTest {
        coEvery { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) } throws Exception("Notes error")

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
        coVerify { oAuthApi.getAuthenticatedSession("https://lti.url", any()) }
    }

    @Test
    fun `Test LTI authentication failure returns original URL`() = runTest {
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } throws Exception("Auth error")

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
        val highlightedData = com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData(
            selectedText = "highlighted text",
            range = com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange(1, 5, "start", "end"),
            textPosition = com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition(1, 5)
        )

        viewModel.uiState.value.addNote(highlightedData, "Important")

        coVerify { addNoteRepository.addNote(
            courseId = courseId.toString(),
            objectId = testPage.id.toString(),
            objectType = "Page",
            highlightedData = highlightedData,
            userComment = "",
            type = com.instructure.horizon.features.notebook.common.model.NotebookType.Important
        ) }

        coVerify(atLeast = 2) { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test refresh notes updates state`() = runTest {
        val updatedNotesResponse = QueryNotesQuery.Notes(
            edges = testNotesResponse.edges.orEmpty() + QueryNotesQuery.Edge(
                cursor = "cursor3",
                node = makeNoteNode("3", "New note")
            ),
            pageInfo = QueryNotesQuery.PageInfo(
                hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null
            )
        )
        coEvery { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) } returnsMany
            listOf(testNotesResponse, updatedNotesResponse)

        val viewModel = getViewModel()
        assertEquals(2, viewModel.uiState.value.notes.size)

        viewModel.refreshNotes()

        assertEquals(3, viewModel.uiState.value.notes.size)
        assertEquals("New note", viewModel.uiState.value.notes.last().userText)
    }

    @Test
    fun `Test refresh notes handles error`() = runTest {
        val viewModel = getViewModel()
        coEvery { redwoodApi.getNotes(any(), any(), any(), any(), any(), any(), any()) } throws Exception("Error")

        viewModel.refreshNotes()
    }

    @Test
    fun `Test page load error sets error state`() = runTest {
        coEvery { getPageDetailsUseCase(any()) } throws Exception("Error")

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
            getPageDetailsUseCase,
            htmlContentFormatter,
            oAuthApi,
            redwoodApi,
            addNoteRepository,
            savedStateHandle
        )
    }
}
