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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.horizon.data.datasource.LocalNotesPage
import com.instructure.horizon.data.datasource.NotebookLocalDataSource
import com.instructure.horizon.data.datasource.NotebookNetworkDataSource
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.redwood.QueryNotesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotebookRepositoryTest {
    private val networkDataSource: NotebookNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: NotebookLocalDataSource = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private val emptyNetworkPage = QueryNotesQuery.Notes(
        edges = emptyList(),
        pageInfo = QueryNotesQuery.PageInfo(false, false, null, null),
    )

    @Before
    fun setUp() {
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        coEvery { networkDataSource.getNotes(any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyNetworkPage
    }

    @Test
    fun `getNotes online calls network and returns mapped page`() = runTest {
        val result = repository().getNotes(courseId = 1L)

        assertTrue(result.notes.isEmpty())
        assertFalse(result.hasNextPage)
        assertNull(result.endCursor)
        coVerify(exactly = 1) { networkDataSource.getNotes(any(), any(), any(), any(), 1L, any(), any(), any()) }
    }

    @Test
    fun `getNotes online with offline enabled writes through to local`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        repository().getNotes(courseId = 1L)

        coVerify { localDataSource.upsertNotes(any()) }
    }

    @Test
    fun `getNotes online with offline disabled does not write to local`() = runTest {
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        repository().getNotes(courseId = 1L)

        coVerify(exactly = 0) { localDataSource.upsertNotes(any()) }
    }

    @Test
    fun `getNotes offline reads from local data source paginated`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val expectedNotes = listOf(mockk<Note>(relaxed = true))
        coEvery {
            localDataSource.getNotes(1L, any(), any(), any(), 0, 10)
        } returns LocalNotesPage(notes = expectedNotes, hasNextPage = true, nextOffset = 10)

        val result = repository().getNotes(courseId = 1L, itemCount = 10)

        assertEquals(expectedNotes, result.notes)
        assertTrue(result.hasNextPage)
        assertEquals("offline:10", result.endCursor)
        coVerify(exactly = 0) { networkDataSource.getNotes(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getNotes offline last page returns null cursor`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery {
            localDataSource.getNotes(any(), any(), any(), any(), any(), any())
        } returns LocalNotesPage(notes = emptyList(), hasNextPage = false, nextOffset = 5)

        val result = repository().getNotes(courseId = 1L)

        assertFalse(result.hasNextPage)
        assertNull(result.endCursor)
    }

    @Test
    fun `getNotes offline forwards encoded cursor as offset`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery {
            localDataSource.getNotes(any(), any(), any(), any(), any(), any())
        } returns LocalNotesPage(notes = emptyList(), hasNextPage = false, nextOffset = 25)

        repository().getNotes(courseId = 1L, after = "offline:20", itemCount = 5)

        coVerify { localDataSource.getNotes(1L, any(), any(), any(), 20, 5) }
    }

    @Test
    fun `getNotes offline with offline disabled still calls network`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns false

        repository().getNotes(courseId = 1L)

        coVerify { networkDataSource.getNotes(any(), any(), any(), any(), 1L, any(), any(), any()) }
    }

    @Test
    fun `getCourses online uses network`() = runTest {
        val courses = listOf(mockk<CourseWithProgress>(relaxed = true))
        coEvery { networkDataSource.getCourses(any()) } returns courses

        val result = repository().getCourses(forceNetwork = true)

        assertEquals(courses, result)
        coVerify { networkDataSource.getCourses(true) }
    }

    @Test
    fun `getCourses offline uses local`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        val local = listOf(mockk<CourseWithProgress>(relaxed = true))
        coEvery { localDataSource.getCourses() } returns local

        val result = repository().getCourses(forceNetwork = false)

        assertEquals(local, result)
        coVerify(exactly = 0) { networkDataSource.getCourses(any()) }
    }

    @Test
    fun `deleteNote online calls network then local`() = runTest {
        repository().deleteNote("note1")

        coVerify { networkDataSource.deleteNote("note1") }
        coVerify { localDataSource.deleteNote("note1") }
    }

    @Test
    fun `deleteNote offline only deletes locally`() = runTest {
        every { networkStateProvider.isOnline() } returns false
        coEvery { featureFlagProvider.offlineEnabled() } returns true

        repository().deleteNote("note1")

        coVerify(exactly = 0) { networkDataSource.deleteNote(any()) }
        coVerify { localDataSource.deleteNote("note1") }
    }

    private fun repository() = NotebookRepository(
        networkDataSource,
        localDataSource,
        networkStateProvider,
        featureFlagProvider,
    )
}
