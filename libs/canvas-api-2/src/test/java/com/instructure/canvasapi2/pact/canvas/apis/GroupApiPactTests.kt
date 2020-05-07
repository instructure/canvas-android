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
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactGroupFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertGroupPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateGroupFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupApiPactTests : ApiPactTestBase() {

    val _PROVIDER_STATE_NAME="mobile courses with groups"

    // Common logic
    private fun createService(): GroupAPI.GroupInterface {
        val client = getClient()
        return client.create(GroupAPI.GroupInterface::class.java)
    }

    //
    // region request a user's groups
    //

    val getAccountGroupsQuery = "include[]=favorites&include[]=can_access"
    val getAccountGroupsPath = "/api/v1/users/self/groups"
    val getAccountGroupsConfig = PactGroupFieldConfig.fromQueryString(getAccountGroupsQuery)
    val getAccountGroupsResponseBody = LambdaDsl.newJsonArray { arr ->
        // We should get a group back for each course in which the user is enrolled
        arr.`object` { obj ->
            obj.populateGroupFields(fieldConfig = getAccountGroupsConfig)
        }
        arr.`object` { obj ->
            obj.populateGroupFields(fieldConfig = getAccountGroupsConfig)
        }
    }.build()
    @Pact(consumer = "android")
    fun getAccountGroupsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given(_PROVIDER_STATE_NAME)

                .uponReceiving("A request for user's groups")
                .path(getAccountGroupsPath)
                .method("GET")
                .query(getAccountGroupsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getAccountGroupsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAccountGroupsPact")
    fun `get user's groups`() {
        val service = createService()

        val getGroupsCall = service.getFirstPageGroups()
        val getGroupsResult = getGroupsCall.execute()

        assertQueryParamsAndPath(getGroupsCall, getAccountGroupsQuery, getAccountGroupsPath)

        val body = getGroupsResult.body()
        assertNotNull("Expected non-null response body", body)
        assertTrue("Expected at least one group returned, got ${body!!.size}", body.size >= 1 )
        for(group in body) {
            assertGroupPopulated("group[?]", group, getAccountGroupsConfig)
        }
    }
    // endregion

    //
    // region request a course's groups
    //

    val getCourseGroupsQuery = null
    val getCourseGroupsPath = "/api/v1/courses/3/groups"
    val getCourseGroupsConfig = PactGroupFieldConfig.fromQueryString(getCourseGroupsQuery)
    val getCourseGroupsResponseBody = LambdaDsl.newJsonArray() { arr ->
        arr.`object` { obj ->
            obj.populateGroupFields(fieldConfig = getCourseGroupsConfig)
        }
    }.build()

    @Pact(consumer = "android")
    fun getCourseGroupsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given(_PROVIDER_STATE_NAME)

                .uponReceiving("A request for course's groups")
                .path(getCourseGroupsPath)
                .method("GET")
                .query(getCourseGroupsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getCourseGroupsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getCourseGroupsPact")
    fun `get course's groups`() {
        val service = createService()

        val getGroupsCall = service.getFirstPageCourseGroups(3)
        val getGroupsResult = getGroupsCall.execute()

        assertQueryParamsAndPath(getGroupsCall, getCourseGroupsQuery, getCourseGroupsPath)

        val body = getGroupsResult.body()
        assertNotNull("Expected non-null response body", body)
        assertTrue("Expected at least one group returned, got ${body!!.size}", body.size >= 1 )
        for(group in body) {
            assertGroupPopulated("group[?]", group, getCourseGroupsConfig)
        }
    }
    // endregion

    //
    // region request detailed group info
    //

    val getGroupDetailsQuery = "include[]=permissions&include[]=favorites"
    val getGroupDetailsPath = "/api/v1/groups/1"
    val getGroupDetailsConfig = PactGroupFieldConfig.fromQueryString(getGroupDetailsQuery)
    val getGroupDetailsResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateGroupFields(fieldConfig = getGroupDetailsConfig)
    }.build()
    @Pact(consumer = "android")
    fun getGroupDetailsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given(_PROVIDER_STATE_NAME)

                .uponReceiving("A request for a group's details")
                .path(getGroupDetailsPath)
                .method("GET")
                .query(getGroupDetailsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getGroupDetailsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getGroupDetailsPact")
    fun `get group details`() {
        val service = createService()

        val getGroupDetailsCall = service.getDetailedGroup(1) // valid group id?
        val getGroupDetailsResult = getGroupDetailsCall.execute()

        assertQueryParamsAndPath(getGroupDetailsCall, getGroupDetailsQuery, getGroupDetailsPath)

        val body = getGroupDetailsResult.body()
        assertNotNull("Expected non-null response body", body)
        assertGroupPopulated("Response", body!!, getGroupDetailsConfig)
    }
    // endregion
}