/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.pact.docviewer.teacher

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.google.gson.GsonBuilder
import com.instructure.canvasapi2.apis.CanvaDocsAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.canvadocs.CanvaDocAnnotationResponse
import com.instructure.canvasapi2.utils.weave.awaitApi
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.Assert.*

class GetNoteAnnotationPact : DocViewerPact() {

    private val expectedAnnotation = CanvaDocAnnotation(
            annotationId = "97a1395c-6fc5-482e-be53-42c2a45ea6f5",
            page = 0,
            isEditable = true,
            rect = arrayListOf(arrayListOf(496.33f, 750.0f), arrayListOf(505.67f, 763.33f)),
            color = "#008EE2")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        @Language("JSON")
        val body = """
            { "data": [ ${GsonBuilder().create().toJson(expectedAnnotation)} ] }
            """

        return builder
                .given("a session id")
                .uponReceiving("a request for annotations")
                .path("/$versionApi/sessions/some_session_id/annotations") // Must have leading slash.
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(body)
                .toPact()
    }

    override fun runTest(mockServer: MockServer) {
        runBlocking {
            val response = awaitApi<CanvaDocAnnotationResponse> {
                val adapter = RestBuilder(it)
                CanvaDocsAPI.getAnnotations("some_session_id", adapter, getParams(mockServer), it) // Must NOT have leading slash.
            }

            assertNotNull(response.data)
            assertNotNull(response.data.first())

            val annotation = response.data.first()
            assertEquals(expectedAnnotation, annotation)
        }
    }

}
