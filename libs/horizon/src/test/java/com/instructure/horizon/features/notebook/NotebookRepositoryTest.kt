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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.managers.RedwoodApiManager
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.LearningObjectFilter
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderDirection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotebookRepositoryTest {
    private val redwoodApiManager: RedwoodApiManager = mockk(relaxed = true)

    @Test
    fun `Test successful notes retrieval with filter`() = runTest {
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        val result = getRepository().getNotes(
            filterType = NotebookType.Important,
            courseId = 1L,
            orderDirection = OrderDirection.descending
        )

        assertNotNull(result)
        assertEquals(mockNotes, result)
    }

    @Test
    fun `Test notes retrieval with pagination after cursor`() = runTest {
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = true, hasPreviousPage = false, endCursor = "cursor123", startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(after = "cursor123")

        coVerify { redwoodApiManager.getNotes(filter = any(), firstN = 10, lastN = any(), after = "cursor123", before = any(), orderBy = any()) }
    }

    @Test
    fun `Test notes retrieval with pagination before cursor`() = runTest {
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = true, endCursor = null, startCursor = "cursor456")
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(before = "cursor456")

        coVerify { redwoodApiManager.getNotes(filter = any(), firstN = any(), lastN = 10, after = any(), before = "cursor456", orderBy = any()) }
    }

    @Test
    fun `Test notes retrieval with course filter`() = runTest {
        val courseId = 123L
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(courseId = courseId)

        coVerify { redwoodApiManager.getNotes(NoteFilterInput(
            courseId = Optional.present(courseId.toString()),
        ), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test notes retrieval with learning object filter`() = runTest {
        val objectTypeAndId = Pair("Assignment", "456")
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(objectTypeAndId = objectTypeAndId)

        coVerify { redwoodApiManager.getNotes(NoteFilterInput(
            learningObject = Optional.present(LearningObjectFilter(
                type = objectTypeAndId.first,
                id = objectTypeAndId.second
            ))
        ), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Test notes retrieval with custom item count`() = runTest {
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(itemCount = 25)

        coVerify { redwoodApiManager.getNotes(filter = any(), firstN = 25, lastN = any(), after = any(), before = any(), orderBy = any()) }
    }

    @Test
    fun `Test notes retrieval with reaction filter`() = runTest {
        val mockNotes = QueryNotesQuery.Notes(
            edges = listOf(),
            pageInfo = QueryNotesQuery.PageInfo(hasNextPage = false, hasPreviousPage = false, endCursor = null, startCursor = null)
        )
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } returns mockNotes

        getRepository().getNotes(filterType = NotebookType.Confusing)

        coVerify { redwoodApiManager.getNotes(NoteFilterInput(
            reactions = Optional.present(listOf("Confusing"))
        ), any(), any(), any(), any(), any()) }
    }

    private fun getRepository(): NotebookRepository {
        return NotebookRepository(redwoodApiManager)
    }
}
