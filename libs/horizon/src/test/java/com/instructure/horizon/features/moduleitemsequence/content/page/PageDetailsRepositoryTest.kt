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

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.redwood.QueryNotesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class PageDetailsRepositoryTest {
    private val pageApi: PageAPI.PagesInterface = mockk(relaxed = true)
    private val oAuthInterface: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val redwoodApi: RedwoodApiManager = mockk(relaxed = true)

    private lateinit var repository: PageDetailsRepository

    private val testPage = Page(
        id = 1L,
        url = "test-page",
        title = "Test Page",
        body = "<p>Page content</p>"
    )

    private val testNotes = QueryNotesQuery.Notes(
        pageInfo = QueryNotesQuery.PageInfo(
            hasNextPage = false,
            hasPreviousPage = false,
            startCursor = null,
            endCursor = null
        ),
        edges = listOf(
            QueryNotesQuery.Edge(
                cursor = "",
                node = QueryNotesQuery.Node(
                    id = "1",
                    objectId = "1",
                    objectType = "Page",
                    userText = "comment 1",
                    rootAccountUuid = "1",
                    userId = "1",
                    courseId = "1",
                    reaction = listOf("Important"),
                    highlightData = "",
                    createdAt = Date(),
                    updatedAt = Date(),
                )
            ),
            QueryNotesQuery.Edge(
                cursor = "",
                node = QueryNotesQuery.Node(
                    id = "2",
                    objectId = "1",
                    objectType = "Page",
                    userText = "comment 2",
                    rootAccountUuid = "1",
                    userId = "1",
                    courseId = "1",
                    reaction = listOf("Important"),
                    highlightData = "",
                    createdAt = Date(),
                    updatedAt = Date(),
                )
            )
        )
    )

    @Before
    fun setup() {
        repository = PageDetailsRepository(pageApi, oAuthInterface, redwoodApi)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getPageDetails returns page successfully`() = runTest {
        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns DataResult.Success(testPage)

        val result = repository.getPageDetails(courseId = 1L, pageId = "test-page", forceNetwork = false)

        assertEquals("Test Page", result.title)
        assertEquals("<p>Page content</p>", result.body)
        coVerify { pageApi.getDetailedPage("courses", 1L, "test-page", any()) }
    }

    @Test
    fun `getPageDetails with forceNetwork true`() = runTest {
        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns DataResult.Success(testPage)

        repository.getPageDetails(courseId = 1L, pageId = "test-page", forceNetwork = true)

        coVerify { pageApi.getDetailedPage(any(), any(), any(), match { it.isForceReadFromNetwork }) }
    }

    @Test
    fun `getPageDetails with forceNetwork false`() = runTest {
        coEvery { pageApi.getDetailedPage(any(), any(), any(), any()) } returns DataResult.Success(testPage)

        repository.getPageDetails(courseId = 1L, pageId = "test-page", forceNetwork = false)

        coVerify { pageApi.getDetailedPage(any(), any(), any(), match { !it.isForceReadFromNetwork }) }
    }

    @Test
    fun `authenticateUrl returns authenticated URL`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://authenticated.url")
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        val result = repository.authenticateUrl("https://example.com/page")

        assertEquals("https://authenticated.url", result)
        coVerify { oAuthInterface.getAuthenticatedSession("https://example.com/page", any()) }
    }

    @Test
    fun `authenticateUrl returns original URL on failure`() = runTest {
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Fail()

        val result = repository.authenticateUrl("https://example.com/page")

        assertEquals("https://example.com/page", result)
    }

    @Test
    fun `authenticateUrl returns original URL when session URL is null`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://example.com/page/authenticated")
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        val result = repository.authenticateUrl("https://example.com/page")

        assertEquals("https://example.com/page/authenticated", result)
    }

    @Test
    fun `getNotes returns notes list`() = runTest {
        coEvery { redwoodApi.getNotes(any(), any(), any()) } returns testNotes

        val result = repository.getNotes(courseId = 1L, pageId = 100L)

        assertEquals(2, result.size)
        assertEquals("comment 1", result.first().userText)
        coVerify { redwoodApi.getNotes(any(), null, null) }
    }

    @Test
    fun `getNotes with different page ID`() = runTest {
        coEvery { redwoodApi.getNotes(any(), any(), any()) } returns testNotes

        repository.getNotes(courseId = 5L, pageId = 200L)

        coVerify { redwoodApi.getNotes(any(), null, null) }
    }

    @Test
    fun `getNotes returns empty list`() = runTest {
        coEvery { redwoodApi.getNotes(any(), any(), any()) } returns QueryNotesQuery.Notes(
            pageInfo = QueryNotesQuery.PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = null,
                endCursor = null
            ),
            edges = emptyList()
        )

        val result = repository.getNotes(courseId = 1L, pageId = 100L)

        assertEquals(0, result.size)
    }

    @Test
    fun `authenticateUrl always uses forceNetwork`() = runTest {
        val session = AuthenticatedSession(sessionUrl = "https://authenticated.url")
        coEvery { oAuthInterface.getAuthenticatedSession(any(), any()) } returns DataResult.Success(session)

        repository.authenticateUrl("https://example.com")

        coVerify { oAuthInterface.getAuthenticatedSession(any(), match { it.isForceReadFromNetwork }) }
    }
}
