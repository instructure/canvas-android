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
package com.instructure.horizon.offline.sync

import com.instructure.horizon.data.datasource.NotebookLocalDataSource
import com.instructure.horizon.data.datasource.NotebookNetworkDataSource
import com.instructure.horizon.database.entity.HorizonNoteEntity
import com.instructure.redwood.QueryNotesQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class NotebookSyncerTest {
    private val networkDataSource: NotebookNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: NotebookLocalDataSource = mockk(relaxed = true)

    private fun edge(id: String) = QueryNotesQuery.Edge(
        cursor = id,
        node = QueryNotesQuery.Node(
            id = id,
            rootAccountUuid = "",
            userId = "1",
            courseId = "10",
            objectId = "1",
            objectType = "Page",
            userText = "text-$id",
            reaction = listOf("Important"),
            highlightData = null,
            createdAt = Date(),
            updatedAt = Date(),
        ),
    )

    @Test
    fun `syncNotes replaces local notes with mapped entities`() = runTest {
        val edges = listOf(edge("n1"), edge("n2"))
        coEvery { networkDataSource.getAllNotesForCourse(10L) } returns edges

        val courseSlot = slot<Long>()
        val entitiesSlot = slot<List<HorizonNoteEntity>>()
        coEvery { localDataSource.replaceNotesForCourse(capture(courseSlot), capture(entitiesSlot)) } returns Unit

        NotebookSyncer(networkDataSource, localDataSource).syncNotes(10L)

        assertEquals(10L, courseSlot.captured)
        assertEquals(listOf("n1", "n2"), entitiesSlot.captured.map { it.id })
        coVerify(exactly = 1) { networkDataSource.getAllNotesForCourse(10L) }
    }

    @Test
    fun `syncNotes with no notes still clears existing data`() = runTest {
        coEvery { networkDataSource.getAllNotesForCourse(10L) } returns emptyList()

        NotebookSyncer(networkDataSource, localDataSource).syncNotes(10L)

        coVerify { localDataSource.replaceNotesForCourse(10L, emptyList()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `syncNotes propagates network errors`() = runTest {
        coEvery { networkDataSource.getAllNotesForCourse(10L) } throws IllegalStateException("boom")

        NotebookSyncer(networkDataSource, localDataSource).syncNotes(10L)
    }
}
