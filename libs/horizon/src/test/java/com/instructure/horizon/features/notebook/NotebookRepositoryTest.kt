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
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.LearningObjectFilter
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderDirection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotebookRepositoryTest {
    private val redwoodApiManager: RedwoodApiManager = mockk(relaxed = true)
    private val horizonGetCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = 123L)
    }

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

    @Test(expected = Exception::class)
    fun `Test error handling for notes retrieval`() = runTest {
        coEvery { redwoodApiManager.getNotes(any(), any(), any(), any(), any(), any()) } throws Exception("Network error")

        getRepository().getNotes()
    }

    @Test
    fun `Test getCourses returns success`() = runTest {
        val mockCourses = listOf(
            mockk<CourseWithProgress>(relaxed = true)
        )
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(mockCourses)

        val result = getRepository().getCourses()

        assertTrue(result is DataResult.Success)
        assertEquals(mockCourses, (result as DataResult.Success).data)
    }

    @Test
    fun `Test getCourses with forceNetwork true`() = runTest {
        val mockCourses = listOf(mockk<CourseWithProgress>(relaxed = true))
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(mockCourses)

        getRepository().getCourses(forceNetwork = true)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(userId = 123L, forceNetwork = true) }
    }

    @Test
    fun `Test getCourses with forceNetwork false`() = runTest {
        val mockCourses = listOf(mockk<CourseWithProgress>(relaxed = true))
        coEvery { horizonGetCoursesManager.getCoursesWithProgress(any(), any()) } returns DataResult.Success(mockCourses)

        getRepository().getCourses(forceNetwork = false)

        coVerify { horizonGetCoursesManager.getCoursesWithProgress(userId = 123L, forceNetwork = false) }
    }

    private fun getRepository(): NotebookRepository {
        return NotebookRepository(redwoodApiManager, horizonGetCoursesManager, apiPrefs)
    }
}
