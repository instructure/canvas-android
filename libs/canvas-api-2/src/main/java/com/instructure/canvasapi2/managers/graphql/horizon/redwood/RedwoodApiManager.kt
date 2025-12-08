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

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.di.RedwoodApolloClient
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.redwood.CreateNoteMutation
import com.instructure.redwood.DeleteNoteMutation
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.UpdateNoteMutation
import com.instructure.redwood.type.CreateNoteInput
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import com.instructure.redwood.type.UpdateNoteInput
import java.util.Date
import javax.inject.Inject

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

class RedwoodApiManagerImpl @Inject constructor(
    @RedwoodApolloClient private val redwoodClient: ApolloClient
) : RedwoodApiManager {
    override suspend fun getNotes(
        filter: NoteFilterInput?,
        firstN: Int?,
        lastN: Int?,
        after: String?,
        before: String?,
        orderBy: OrderByInput?,
        forceNetwork: Boolean
    ): QueryNotesQuery.Notes {
        val query = QueryNotesQuery(
            filter = Optional.presentIfNotNull(filter),
            first = Optional.presentIfNotNull(firstN?.toDouble()),
            last = Optional.presentIfNotNull(lastN?.toDouble()),
            after = Optional.presentIfNotNull(after),
            before = Optional.presentIfNotNull(before),
            orderBy = Optional.presentIfNotNull(orderBy),
        )
        val result = redwoodClient
            .enqueueQuery(query, forceNetwork)
            .dataAssertNoErrors.notes

        return result
    }

    override suspend fun createNote(
        courseId: String,
        objectId: String,
        objectType: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?
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
                highlightData = Optional.presentIfNotNull(getHighlightDataAsJson(highlightData))
            )
        )

        redwoodClient
            .enqueueMutation(mutation)
            .dataAssertNoErrors
    }

    override suspend fun updateNote(
        id: String,
        userText: String?,
        notebookType: String?,
        highlightData: NoteHighlightedData?
    ) {
        val reaction = if (notebookType == null) {
            Optional.absent()
        } else {
            Optional.present(listOf(notebookType))
        }

        val mutation = UpdateNoteMutation(
            id = id,
            input = UpdateNoteInput(
                userText = Optional.presentIfNotNull(userText),
                reaction = reaction,
                highlightData = Optional.presentIfNotNull(getHighlightDataAsJson(highlightData))
            )
        )

        redwoodClient
            .enqueueMutation(mutation)
            .dataAssertNoErrors
    }

    override suspend fun deleteNote(noteId: String) {
        val mutation = DeleteNoteMutation(noteId)

        redwoodClient
            .enqueueMutation(mutation)
            .dataAssertNoErrors
    }

    private fun getHighlightDataAsJson(highlightedData: NoteHighlightedData?): Map<String, Any>? {
        return highlightedData?.let {
            val gson = Gson()
            val jsonTree = gson.toJsonTree(highlightedData)
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(jsonTree, mapType)
        }
    }
}