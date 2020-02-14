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
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.core.model.RequestResponsePact
import com.instructure.canvasapi2.apis.CanvaDocsAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.models.DocSession
import com.instructure.canvasapi2.utils.weave.awaitApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

class GetSessionPact : DocViewerPact() {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        val body = PactDslJsonBody()
                .stringType("document_id", "some_document_id")
                .`object`("urls")
                .stringType("pdf_download", "some_pdf_download_url")
                .closeObject()
                .asBody()

        return builder
                .given("a redirect url")
                .uponReceiving("a request to get a session")
                .path("/1/sessions/some_session_id") // Must have leading slash.
                .method("GET")
                .willRespondWith()
                .headers(mapOf("Content-Type" to "application/json; charset=utf-8")) //MBL-12312
                .status(200)
                .body(body)
                .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        runBlocking {
            val session = awaitApi<DocSession> {
                val adapter = RestBuilder(it)
                CanvaDocsAPI.getCanvaDoc("1/sessions/some_session_id", adapter, getParams(mockServer), it) // Must NOT have leading slash.
            }

            assertEquals("some_document_id", session.documentId)
            assertEquals("some_pdf_download_url", session.annotationUrls.pdfDownload)
        }
    }
}
