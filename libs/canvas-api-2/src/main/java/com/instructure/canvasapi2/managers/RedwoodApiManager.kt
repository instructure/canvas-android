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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.RedwoodGraphQLClientConfig
import com.instructure.redwood.CreateNoteMutation
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.CreateNoteInput
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import java.util.Date
import javax.inject.Inject

enum class NoteObjectType(val value: String) {
    Assignment("Assignment"),
    Quiz("Quiz"),
    PAGE("Page");

    companion object {
        fun fromValue(value: String): NoteObjectType? {
            return values().find { it.value == value }
        }
    }
}

enum class NoteReaction(val value: String) {
    Important("Important"),
    Confusing("Confusing");

    companion object {
        fun fromValue(value: String): NoteReaction? {
            return values().find { it.value == value }
        }
    }
}

data class NoteHighlightedData(
    val selectedText: String,
    val textPosition: NoteHighlightedDataTextPosition
)

data class NoteHighlightedDataTextPosition(
    val start: Int,
    val end: Int
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

class RedwoodApiManager @Inject constructor(
    private val redwoodClient: RedwoodGraphQLClientConfig,
) {
    suspend fun getNotes(
        filter: NoteFilterInput? = null,
        firstN: Int? = null,
        lastN: Int? = null,
        after: String? = null,
        before: String? = null,
        orderBy: OrderByInput? = null,
    ): QueryNotesQuery.Notes {
        val query = QueryNotesQuery(
            filter = Optional.presentIfNotNull(filter),
            first = Optional.presentIfNotNull(firstN?.toDouble()),
            last = Optional.presentIfNotNull(lastN?.toDouble()),
            after = Optional.presentIfNotNull(after),
            before = Optional.presentIfNotNull(before),
            orderBy = Optional.presentIfNotNull(orderBy),
        )
        val result = QLClientConfig
            .enqueueQuery(query, block = redwoodClient.createClientConfigBlock())
            .dataAssertNoErrors.notes

        return result
    }

    suspend fun createNote(
        courseId: String,
        objectId: String,
        objectType: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData? = null
    ) {
        val reaction = if (notebookType == null) {
            Optional.absent()
        } else {
            Optional.present(listOf(notebookType))
        }
        val mutation = CreateNoteMutation(
            CreateNoteInput(
                courseId = courseId,
                objectId = objectId,
                objectType = objectType,
                userText = Optional.presentIfNotNull(userText),
                reaction = reaction,
                highlightData = Optional.presentIfNotNull(highlightData)
            )
        )

        QLClientConfig
            .enqueueMutation(mutation, block = redwoodClient.createClientConfigBlock())
            .dataAssertNoErrors
    }
}