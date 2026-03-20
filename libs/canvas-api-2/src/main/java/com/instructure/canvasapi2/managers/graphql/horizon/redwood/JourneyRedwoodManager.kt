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
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.api.json.buildJsonString
import com.apollographql.apollo.api.json.jsonReader
import com.apollographql.apollo.api.json.writeObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.instructure.canvasapi2.di.JourneyApolloClient
import com.instructure.canvasapi2.enqueueMutation
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.journey.ExecuteRedwoodQueryMutation
import com.instructure.journey.type.RedwoodQueryInput
import com.instructure.redwood.CreateNoteMutation
import com.instructure.redwood.DeleteNoteMutation
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.UpdateNoteMutation
import com.instructure.redwood.type.CreateNoteInput
import com.instructure.redwood.type.NoteFilterInput
import com.instructure.redwood.type.OrderByInput
import com.instructure.redwood.type.UpdateNoteInput
import okio.Buffer
import java.util.Date
import javax.inject.Inject

class JourneyRedwoodManagerImpl @Inject constructor(
    @JourneyApolloClient private val journeyClient: ApolloClient
) : RedwoodApiManager {

    private val gson = Gson()

    private val dateTimeAdapter: Adapter<Date?> = object : Adapter<Date?> {
        override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date? =
            if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull()
                null
            } else {
                reader.nextString().toDate()
            }

        override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date?) {
            value?.let { writer.value(it.toApiString()) } ?: writer.nullValue()
        }
    }

    private val customScalarAdapters = CustomScalarAdapters.Builder()
        .add(com.instructure.redwood.type.DateTime.type, dateTimeAdapter)
        .build()

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

        val result = executeRedwoodQuery(query)
        return result.notes
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

        executeRedwoodMutation(mutation)
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

        executeRedwoodMutation(mutation)
    }

    override suspend fun deleteNote(noteId: String) {
        val mutation = DeleteNoteMutation(noteId)
        executeRedwoodMutation(mutation)
    }

    private suspend fun <D : Query.Data> executeRedwoodQuery(
        operation: Query<D>
    ): D {
        val queryString = operation.document()
        val variables = serializeVariables(operation)

        val input = RedwoodQueryInput(
            query = queryString,
            variables = Optional.presentIfNotNull(variables),
            operationName = Optional.presentIfNotNull(operation.name())
        )

        val mutation = ExecuteRedwoodQueryMutation(input)
        val response = journeyClient.enqueueMutation(mutation).dataAssertNoErrors

        return parseResponse(response.executeRedwoodQuery.data, operation)
    }

    private suspend fun <D : Mutation.Data> executeRedwoodMutation(
        operation: Mutation<D>
    ): D {
        val queryString = operation.document()
        val variables = serializeVariables(operation)

        val input = RedwoodQueryInput(
            query = queryString,
            variables = Optional.presentIfNotNull(variables),
            operationName = Optional.presentIfNotNull(operation.name())
        )

        val mutation = ExecuteRedwoodQueryMutation(input)
        val response = journeyClient.enqueueMutation(mutation).dataAssertNoErrors

        return parseResponse(response.executeRedwoodQuery.data, operation)
    }

    private fun <D : Operation.Data> serializeVariables(
        operation: Operation<D>
    ): Any? {
        val jsonString = buildJsonString {
            writeObject {
                operation.serializeVariables(this, customScalarAdapters, false)
            }
        }

        return if (jsonString == "{}" || jsonString.isEmpty()) {
            null
        } else {
            gson.fromJson(jsonString, Any::class.java)
        }
    }

    private fun <D : Operation.Data> parseResponse(
        data: Any?,
        operation: Operation<D>
    ): D {
        if (data == null) {
            throw IllegalStateException("Redwood query returned null data")
        }

        val jsonString = gson.toJson(data)
        val buffer = Buffer().writeUtf8(jsonString)
        val jsonReader = buffer.jsonReader()

        return operation.adapter().fromJson(jsonReader, customScalarAdapters)
    }

    private fun getHighlightDataAsJson(highlightedData: NoteHighlightedData?): Map<String, Any>? {
        return highlightedData?.let {
            val jsonTree = gson.toJsonTree(highlightedData)
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(jsonTree, mapType)
        }
    }
}
