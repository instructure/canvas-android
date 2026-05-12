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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.NoteFilterInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class NotebookNetworkDataSourceTest {
    private val redwood: RedwoodApiManager = mockk(relaxed = true)
    private val horizonCourses: HorizonGetCoursesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private fun edge(id: String) = QueryNotesQuery.Edge(
        cursor = id,
        node = QueryNotesQuery.Node(
            id = id,
            rootAccountUuid = "",
            userId = "1",
            courseId = "100",
            objectId = "1",
            objectType = "Page",
            userText = "",
            reaction = listOf("Important"),
            highlightData = null,
            createdAt = Date(),
            updatedAt = Date(),
        )
    )

    private fun page(edges: List<QueryNotesQuery.Edge>, cursor: String?, hasNext: Boolean) = QueryNotesQuery.Notes(
        edges = edges,
        pageInfo = QueryNotesQuery.PageInfo(hasNext, false, null, cursor),
    )

    @Test
    fun `getAllNotesForCourse depaginates until hasNextPage is false`() = runTest {
        val first = page(listOf(edge("a"), edge("b")), "c1", true)
        val second = page(listOf(edge("c")), "c2", true)
        val third = page(listOf(edge("d")), null, false)

        val responses = mutableListOf(first, second, third)
        coEvery {
            redwood.getNotes(
                filter = any<NoteFilterInput>(),
                firstN = any(),
                after = any(),
                forceNetwork = any(),
            )
        } answers { responses.removeAt(0) }

        val result = dataSource().getAllNotesForCourse(100L)

        assertEquals(listOf("a", "b", "c", "d"), result.map { it.node.id })
        coVerify(exactly = 3) {
            redwood.getNotes(
                filter = any<NoteFilterInput>(),
                firstN = NotebookNetworkDataSource.SYNC_PAGE_SIZE,
                after = any(),
                forceNetwork = true,
            )
        }
    }

    @Test
    fun `getAllNotesForCourse returns empty when first page has no next and empty edges`() = runTest {
        coEvery {
            redwood.getNotes(
                filter = any<NoteFilterInput>(),
                firstN = any(),
                after = any(),
                forceNetwork = any(),
            )
        } returns page(emptyList(), null, false)

        val result = dataSource().getAllNotesForCourse(1L)

        assertEquals(0, result.size)
    }

    private fun dataSource() = NotebookNetworkDataSource(redwood, horizonCourses, apiPrefs)
}
