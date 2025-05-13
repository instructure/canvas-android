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

import com.instructure.canvasapi2.PineGraphQLClientConfig
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.pine.PingQuery
import com.instructure.pine.QueryDocumentMutation
import com.instructure.pine.UpsertDocumentMutation
import com.instructure.pine.type.DocumentUpsertInput
import com.instructure.pine.type.MessageInput
import com.instructure.pine.type.RagQueryInput
import org.json.JSONObject
import javax.inject.Inject

enum class DocumentSource(val apiValue: String) {
    canvas("canvas"),
}

enum class UpsertDocumentType(val apiValue: String) {
    page("page"),
}

class PineApiManager @Inject constructor(
    private val pineClient: PineGraphQLClientConfig
) {
    suspend fun upsertDocument(
        source: DocumentSource,
        type: UpsertDocumentType,
        id: String,
        metadata: Map<String, String>,
        text: String
    ) {
        val mutation = UpsertDocumentMutation(
            DocumentUpsertInput(
                source = source.apiValue,
                sourceType = type.apiValue,
                sourceId = id,
                metadata = JSONObject(metadata).toString(),
                text = text,
            )
        )
        QLClientConfig
            .enqueueMutation(mutation, block = pineClient.createClientConfigBlock())
            .dataAssertNoErrors
    }

    suspend fun queryDocument(
        messages: List<MessageInput>,
        source: DocumentSource,
        metadata: Map<String, String>
    ): String {
        val mutation = QueryDocumentMutation(
            RagQueryInput(
                messages = messages,
                source = source.apiValue,
                metadata = JSONObject(metadata).toString(),
            )
        )
        val result = QLClientConfig
            .enqueueMutation(mutation, block = pineClient.createClientConfigBlock())
            .dataAssertNoErrors.query

        return result.response
    }

    suspend fun ping(): String {
        val query = PingQuery()
        val result = QLClientConfig
            .enqueueQuery(query, block = pineClient.createClientConfigBlock())
            .dataAssertNoErrors.ping

        return result
    }
}