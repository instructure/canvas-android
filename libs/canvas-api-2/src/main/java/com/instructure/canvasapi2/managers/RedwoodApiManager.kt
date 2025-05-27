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
import com.google.gson.Gson
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.RedwoodGraphQLClientConfig
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import java.util.Date
import javax.inject.Inject

enum class NoteObjectType(val value: String) {
    ASSIGNMENT("assignment"),
    ANNOUNCEMENT("announcement"),
    DISCUSSION_TOPIC("discussion_topic"),
    QUIZ("quiz"),
    PAGE("page"),
    SYLLABUS("syllabus"),
    SYLLABUS_ENTRY("syllabus_entry"),
    COURSE("course"),
    USER("user");

    companion object {
        fun fromValue(value: String): NoteObjectType? {
            return values().find { it.value == value }
        }
    }
}

enum class NoteReaction(val value: String) {
    LIKE("like"),
    HELPFUL("helpful");

    companion object {
        fun fromValue(value: String): NoteReaction? {
            return values().find { it.value == value }
        }
    }
}

data class NoteHighlightedData(
    val start: Int,
    val end: Int,
    val color: String
)

data class Note(
    val id: String,
    val rootAccountUuid: String,
    val userId: String,
    val courseId: String,
    val objectId: String,
    val objectType: NoteObjectType,
    val userText: String,
    val reactions: List<NoteReaction>,
    val highlightedData: NoteHighlightedData?,
    val createdAt: Date,
    val updatedAt: Date,
)

class RedwoodApiManager @Inject constructor(
    private val redwoodClient: RedwoodGraphQLClientConfig,
) {
    suspend fun getNotes(
        filter: NoteFilterInput? = null,
        firstN: Int? = null,
        lastN: Int? = null,
        after: Date? = null,
        before: Date? = null,
        orderBy: OrderByInput? = null,
    ): List<Note> {
        val query = QueryNotesQuery(
            filter = Optional.presentIfNotNull(filter),
            first = Optional.presentIfNotNull(firstN?.toDouble()),
            last = Optional.presentIfNotNull(lastN?.toDouble()),
            after = Optional.presentIfNotNull(after?.toApiString()),
            before = Optional.presentIfNotNull(before?.toApiString()),
            orderBy = Optional.presentIfNotNull(orderBy),
        )
        val result = QLClientConfig
            .enqueueQuery(query, block = redwoodClient.createClientConfigBlock())
            .dataAssertNoErrors.notes
            .nodes?.map {
                Note(
                    id = it.id,
                    rootAccountUuid = it.rootAccountUuid,
                    userId = it.userId,
                    courseId = it.courseId,
                    objectId = it.objectId,
                    objectType = NoteObjectType.fromValue(it.objectType) ?: NoteObjectType.COURSE,
                    userText = it.userText ?: "",
                    reactions = it.reaction?.mapNotNull { reaction -> NoteReaction.fromValue(reaction) } ?: emptyList(),
                    highlightedData = parseHighlightedData(it.highlightData),
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }.orEmpty()

        return result
    }

    private fun parseHighlightedData(highlightData: Any?): NoteHighlightedData? {
        val result = try {
            Gson().fromJson(highlightData.toString(), NoteHighlightedData::class.java)
        } catch (e: Exception) {
            null
        }

        return result
    }
}