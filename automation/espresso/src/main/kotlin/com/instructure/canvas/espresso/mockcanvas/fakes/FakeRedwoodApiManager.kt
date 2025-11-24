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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import java.util.Date

class FakeRedwoodApiManager : RedwoodApiManager {
    private val notes = mutableListOf<QueryNotesQuery.Node>()
    private var noteIdCounter = 1

    override suspend fun getNotes(
        filter: NoteFilterInput?,
        firstN: Int?,
        lastN: Int?,
        after: String?,
        before: String?,
        orderBy: OrderByInput?
    ): QueryNotesQuery.Notes {
        val edges = notes.map { note ->
            QueryNotesQuery.Edge(
                cursor = note.id,
                node = note
            )
        }

        val pageInfo = QueryNotesQuery.PageInfo(
            hasNextPage = false,
            hasPreviousPage = false,
            startCursor = edges.firstOrNull()?.cursor,
            endCursor = edges.lastOrNull()?.cursor
        )

        return QueryNotesQuery.Notes(
            edges = edges,
            pageInfo = pageInfo
        )
    }

    override suspend fun createNote(
        courseId: String,
        objectId: String,
        objectType: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?
    ) {
        val note = QueryNotesQuery.Node(
            id = "note_${noteIdCounter++}",
            rootAccountUuid = "test-root-account",
            userId = "test-user",
            courseId = courseId,
            objectId = objectId,
            objectType = objectType,
            userText = userText,
            reaction = notebookType?.let { listOf(notebookType) },
            highlightData = highlightData,
            createdAt = Date(),
            updatedAt = Date()
        )
        notes.add(note)
    }

    override suspend fun updateNote(
        id: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?
    ) {
        val index = notes.indexOfFirst { it.id == id }
        if (index != -1) {
            val existingNote = notes[index]
            notes[index] = existingNote.copy(
                userText = userText,
                reaction = notebookType?.let { listOf(it) },
                updatedAt = Date()
            )
        }
    }

    override suspend fun deleteNote(noteId: String) {
        notes.removeAll { it.id == noteId }
    }

    fun reset() {
        notes.clear()
        noteIdCounter = 1
    }
}
