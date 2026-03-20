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
package com.instructure.canvasapi2.managers.graphql.horizon.redwood

import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import java.util.Date

enum class NoteObjectType(val value: String) {
    Assignment("Assignment"),
    Quiz("Quiz"),
    PAGE("Page");

    companion object {
        fun fromValue(value: String): NoteObjectType? {
            return NoteObjectType.entries.find { it.value == value }
        }
    }
}

enum class NoteReaction(val value: String) {
    Important("Important"),
    Confusing("Confusing");

    companion object {
        fun fromValue(value: String): NoteReaction? {
            return NoteReaction.entries.find { it.value == value }
        }
    }
}

data class NoteHighlightedData(
    val selectedText: String,
    val range: NoteHighlightedDataRange,
    val textPosition: NoteHighlightedDataTextPosition
)

data class NoteHighlightedDataRange(
    val startOffset: Int,
    val endOffset: Int,
    val startContainer: String,
    val endContainer: String,
)

data class NoteHighlightedDataTextPosition(
    val start: Int = 0,
    val end: Int = 0
)

data class NoteItem(
    val id: String,
    val rootAccountUuid: String,
    val userId: String,
    val courseId: String,
    val objectId: String,
    val objectType: NoteObjectType?,
    val userText: String,
    val reactions: List<NoteReaction>,
    val highlightedData: NoteHighlightedData?,
    val createdAt: Date?,
    val updatedAt: Date?,
)

interface RedwoodApiManager {
    suspend fun getNotes(
        filter: NoteFilterInput? = null,
        firstN: Int? = null,
        lastN: Int? = null,
        after: String? = null,
        before: String? = null,
        orderBy: OrderByInput? = null,
        forceNetwork: Boolean = false
    ): QueryNotesQuery.Notes

    suspend fun createNote(
        courseId: String,
        objectId: String,
        objectType: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData? = null
    )

    suspend fun updateNote(
        id: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData? = null
    )

    suspend fun deleteNote(noteId: String)
}