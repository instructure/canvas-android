/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvasapi2.managers.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.StudyNotesForPageQuery
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.models.LearningObjectType as StudyNoteLearningObjectType
import com.instructure.canvasapi2.type.LearningObjectType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.UUID

private const val QL_CLIENT_CONFIG_CLASS = "com.instructure.canvasapi2.QLClientConfigKt"

class StudyNoteManagerImplTest {

    private val apolloClient: ApolloClient = mockk()
    private lateinit var manager: StudyNoteManagerImpl

    @Before
    fun setup() {
        mockkStatic(QL_CLIENT_CONFIG_CLASS)
        manager = StudyNoteManagerImpl(apolloClient)
    }

    @After
    fun tearDown() {
        unmockkStatic(QL_CLIENT_CONFIG_CLASS)
    }

    @Test
    fun `getPageStudyNotes returns every note of a single page`() = runTest {
        val response = queryResponse(
            data(nodes = listOf(studyNoteNode("1"), studyNoteNode("2")))
        )
        coEvery {
            apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any())
        } returns response

        val result = manager.getPageStudyNotes(courseId = 100L, pageId = "page-1", forceNetwork = true)

        assertEquals(2, result.size)
        assertEquals(listOf("1", "2"), result.map { it.id })

        val first = result.first()
        assertEquals("Text 1", first.userText)
        assertEquals(listOf("👍"), first.reactions)
        assertEquals(StudyNoteLearningObjectType.WikiPage, first.learningObjectType)

        val highlightData = first.highlightData
        assertEquals("Selected", highlightData?.selectedText)
        assertEquals(1, highlightData?.range?.startOffset)
        assertEquals(5, highlightData?.range?.endOffset)
        assertEquals("p1", highlightData?.range?.startContainer)
        assertEquals("p2", highlightData?.range?.endContainer)
        assertEquals(10, highlightData?.textPosition?.start)
        assertEquals(20, highlightData?.textPosition?.end)
        assertEquals("2026-01-01", highlightData?.pageLastModifiedAt)

        coVerify(exactly = 1) { apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any()) }
    }

    @Test
    fun `getPageStudyNotes accumulates notes across multiple pages`() = runTest {
        val firstPage = queryResponse(
            data(nodes = listOf(studyNoteNode("1")), hasNextPage = true, endCursor = "cursor-1")
        )
        val secondPage = queryResponse(
            data(nodes = listOf(studyNoteNode("2")))
        )
        coEvery {
            apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any())
        } returnsMany listOf(firstPage, secondPage)

        val result = manager.getPageStudyNotes(courseId = 100L, pageId = "page-1", forceNetwork = true)

        assertEquals(listOf("1", "2"), result.map { it.id })
        coVerify(exactly = 1) {
            apolloClient.enqueueQuery(match<StudyNotesForPageQuery> { it.after == Optional.absent() }, any(), any())
        }
        coVerify(exactly = 1) {
            apolloClient.enqueueQuery(match<StudyNotesForPageQuery> { it.after == Optional.Present("cursor-1") }, any(), any())
        }
        coVerify(exactly = 2) { apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any()) }
    }

    @Test
    fun `getPageStudyNotes maps missing or malformed highlight data to null instead of throwing`() = runTest {
        val response = queryResponse(
            data(
                nodes = listOf(
                    studyNoteNode("1", highlightData = null),
                    studyNoteNode("2", highlightData = "not-a-valid-highlight-payload")
                )
            )
        )
        coEvery {
            apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any())
        } returns response

        val result = manager.getPageStudyNotes(courseId = 100L, pageId = "page-1", forceNetwork = true)

        assertEquals(2, result.size)
        assertNull(result[0].highlightData)
        assertNull(result[1].highlightData)
    }

    @Test
    fun `getPageStudyNotes maps a note whose highlightData has a null range to non-null highlightData with a null range`() = runTest {
        // Distinct from the malformed/missing case above: this is valid JSON (matches a real note
        // web itself can produce for a selection spanning too many elements to anchor to a DOM
        // range) where highlightData itself parses fine but its nested "range" key is null.
        val response = queryResponse(
            data(nodes = listOf(studyNoteNode("1", highlightData = validHighlightData(range = null))))
        )
        coEvery {
            apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any())
        } returns response

        val result = manager.getPageStudyNotes(courseId = 100L, pageId = "page-1", forceNetwork = true)

        assertEquals("Selected", result.single().highlightData?.selectedText)
        assertNull(result.single().highlightData?.range)
        assertEquals(10, result.single().highlightData?.textPosition?.start)
    }

    @Test
    fun `getPageStudyNotes returns an empty list when the connection has no nodes`() = runTest {
        val response = queryResponse(data(nodes = emptyList()))
        coEvery {
            apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any())
        } returns response

        val result = manager.getPageStudyNotes(courseId = 100L, pageId = "page-1", forceNetwork = true)

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { apolloClient.enqueueQuery(any<StudyNotesForPageQuery>(), any(), any()) }
    }

    private fun studyNoteNode(
        id: String,
        userText: String? = "Text $id",
        reactions: List<String> = listOf("👍"),
        highlightData: Any? = validHighlightData(),
        learningObjectId: String = "page-1",
        learningObjectType: LearningObjectType = LearningObjectType.WikiPage,
        createdAt: Date? = Date(),
        updatedAt: Date? = Date(),
    ) = StudyNotesForPageQuery.Node(
        _id = id,
        userText = userText,
        reactions = reactions,
        highlightData = highlightData,
        learningObjectId = learningObjectId,
        learningObjectType = learningObjectType,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun validHighlightData(range: Map<String, Any?>? = mapOf(
        "startOffset" to 1,
        "endOffset" to 5,
        "startContainer" to "p1",
        "endContainer" to "p2"
    )): Map<String, Any?> = mapOf(
        "selectedText" to "Selected",
        "range" to range,
        "textPosition" to mapOf(
            "start" to 10,
            "end" to 20
        ),
        "pageLastModifiedAt" to "2026-01-01"
    )

    private fun data(
        nodes: List<StudyNotesForPageQuery.Node?>,
        hasNextPage: Boolean = false,
        endCursor: String? = null,
    ) = StudyNotesForPageQuery.Data(
        studyNotesConnection = StudyNotesForPageQuery.StudyNotesConnection(
            nodes = nodes,
            pageInfo = StudyNotesForPageQuery.PageInfo(hasNextPage = hasNextPage, endCursor = endCursor)
        )
    )

    private fun queryResponse(data: StudyNotesForPageQuery.Data?): ApolloResponse<StudyNotesForPageQuery.Data> =
        ApolloResponse.Builder(
            operation = StudyNotesForPageQuery(
                courseId = "100",
                pageId = "page-1",
                pageSize = 20,
                after = Optional.absent()
            ),
            requestUuid = UUID.randomUUID()
        ).data(data).build()
}
