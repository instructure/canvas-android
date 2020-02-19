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
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactUserFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertUserPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateUserFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UsersApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(pathPrefix: String = "/api/v1/") : UserAPI.UsersInterface {

        val client = getClient(pathPrefix)
        return client.create(UserAPI.UsersInterface::class.java)
    }

    //
    //region grab user profile info
    //
    val userFieldConfig = PactUserFieldConfig(includeLocaleInfo = true, includePermissions = true)
    val userPath = "/api/v1/users/self"
    val userResponseBody =  LambdaDsl.newJsonBody() { obj ->
        obj.populateUserFields(userFieldConfig)
    }.build()

    @Pact(consumer = "android")
    fun getUserWithPermissionsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user's user info")
                .path(userPath)
                .method("GET")
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(userResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getUserWithPermissionsPact")
    fun `grab user's user info`() {
        val service = createService()

        val userCall = service.getSelfWithPermissions()
        val userResult = userCall.execute()

        assertQueryParamsAndPath(userCall, null, userPath)

        assertNotNull("Expected non-null response body", userResult.body())
        val user = userResult.body()!!

        assertUserPopulated("user info", user, userFieldConfig)
    }

    //endregion


    //
    //region grab user profile info
    //
    val profileFieldConfig = PactUserFieldConfig(includeLocaleInfo = true, includeProfileInfo = true, includeLoginId = true)
    val profilePath = "/api/v1/users/self/profile"
    val profileResponseBody =  LambdaDsl.newJsonBody() { obj ->
        obj.populateUserFields(profileFieldConfig)
    }.build()
    @Pact(consumer = "android")
    fun getProfilePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user's profile")
                .path(profilePath)
                .method("GET")
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(profileResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getProfilePact")
    fun `grab user's profile`() {
        val service = createService()

        val profileCall = service.getSelf()
        val profileResult = profileCall.execute()

        assertQueryParamsAndPath(profileCall, null, profilePath)

        assertNotNull("Expected non-null response body", profileResult.body())
        val profile = profileResult.body()!!

        assertUserPopulated("User profile", profile, profileFieldConfig)
    }
    //endregion

    //
    //region grab people from course
    //
    val peopleCallQuery = "include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&exclude_inactive=true&enrollment_type=student"
    val peopleCallPath = "/api/v1/courses/2/users"
    val peopleFieldConfig = PactUserFieldConfig(includeEnrollment = true, includeProfileInfo = false)
    val peopleResponseBody = LambdaDsl.newJsonArray() { array ->
        array.`object`() { person ->
            person.populateUserFields(peopleFieldConfig)
        }
    }.build()

    @Pact(consumer = "android")
    fun getCoursePeoplePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for course 2's student people")
                .path(peopleCallPath)
                .method("GET")
                .query(peopleCallQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(peopleResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getCoursePeoplePact")
    fun `grab course 2's student people`() {
        val service = createService("/api/v1/courses/")

        val peopleCall = service.getFirstPagePeopleList(2, enrollmentType="student")
        val peopleResult = peopleCall.execute()

        assertQueryParamsAndPath(peopleCall, peopleCallQuery, peopleCallPath)

        assertNotNull("Expected non-null response body", peopleResult.body())

        val people = peopleResult.body()!!
        assertEquals("People list size", 1, people.size)

        val person = people[0]
        assertUserPopulated("CoursePeople person", person, peopleFieldConfig)
    }
    //endregion
}