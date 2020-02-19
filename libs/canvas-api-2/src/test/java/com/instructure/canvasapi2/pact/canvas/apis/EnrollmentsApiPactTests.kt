/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactEnrollmentFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertEnrollmentPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateEnrollmentFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EnrollmentsApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService() : EnrollmentAPI.EnrollmentInterface {

        val client = getClient()
        return client.create(EnrollmentAPI.EnrollmentInterface::class.java)
    }

    //
    //region grab user's enrollments
    //

    val selfEnrollmentsFieldInfo = listOf(
            PactEnrollmentFieldConfig(courseId = 2, userId = 8, populateFully = true, includeGrades = true),
            PactEnrollmentFieldConfig(courseId = 3, userId = 8, populateFully = true, includeGrades = true)
    )
    val selfEnrollmentsPath = "/api/v1/users/self/enrollments"
    val selfEnrollmentsResponseBody =  LambdaDsl.newJsonArray { array ->
        for(fieldInfo in selfEnrollmentsFieldInfo) {
            array.`object` { obj ->
                obj.populateEnrollmentFields(fieldInfo)
            }
        }
    }.build()
    @Pact(consumer = "android")
    fun getSelfEnrollmentsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user's enrollments")
                .path(selfEnrollmentsPath)
                .method("GET")
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(selfEnrollmentsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }
    @Test
    @PactVerification(fragment = "getSelfEnrollmentsPact")
    fun `grab user's enrollments`() {
        val service = createService()

        val selfEnrollmentsCall = service.getFirstPageSelfEnrollments(types=null,states=null)
        val selfEnrollmentResult = selfEnrollmentsCall.execute()

        assertQueryParamsAndPath(selfEnrollmentsCall, null, selfEnrollmentsPath)

        assertNotNull("Expected non-null response body", selfEnrollmentResult.body())
        val enrollments = selfEnrollmentResult.body()!!
        assertEquals("returned list size", 2, enrollments.count())

        for(index in 0..enrollments.size-1) {
            val enrollment = enrollments[index]
            val fieldInfo = selfEnrollmentsFieldInfo[index]

            assertEnrollmentPopulated(description = "enrollment $index", enrollment = enrollment, fieldConfig = fieldInfo)
        }
    }
    //endregion
}