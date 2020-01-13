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

import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactVerification
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.pact.canvas.objects.PactUserFieldInfo
import com.instructure.canvasapi2.pact.canvas.objects.assertUserPopulated
import com.instructure.canvasapi2.pact.canvas.objects.populateUserFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UsersApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService() : UserAPI.UsersInterface {

        val client = getClient()
        return client.create(UserAPI.UsersInterface::class.java)
    }

    //
    //region grab user profile info
    //
    //val userFieldInfo = PactUserFieldInfo(id = 1, populateFully = true, isProfile = false)
    val userResponseBody =  LambdaDsl.newJsonBody() { obj ->
        obj
                .id("id", 1L)
                .stringType("name")
                .stringType("sortable_name")
                .stringType("short_name")
                .stringType("locale")
                .stringType("effective_locale")
                .`object`("permissions") { permissions ->
                    permissions.booleanType("can_update_name")
                    permissions.booleanType("can_update_avatar")
                }
        //obj.populateUserFields(userFieldInfo)
    }.build()
    @Pact(consumer = "mobile")
    fun getUserWithPermissionsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user 1's  user object")
                .path("/api/v1/users/self")
                .method("GET")
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(userResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getUserWithPermissionsPact")
    fun `grab user 1's user info`() {
        val service = createService()

        val userCall = service.getSelfWithPermissions()
        val userResult = userCall.execute()

        assertNotNull("Expected non-null response body", userResult.body())
        val user = userResult.body()!!

        assertNotNull("id", user.id)
        assertEquals("id", 1L, user.id)
        assertNotNull("name",  user.name)
        assertNotNull("shortName",  user.shortName)
        assertNotNull("sortableName",  user.sortableName)
        assertNotNull("locale",  user.locale)
        assertNotNull("effective_locale",  user.effective_locale)
        assertNotNull("permissions",  user.permissions)
        assertNotNull("permissions.canUpdateAvatar",  user.permissions!!.canUpdateAvatar)
        assertNotNull("permissions.canUpdateName",  user.permissions!!.canUpdateName)
    }

    //endregion


    //
    //region grab user profile info
    //
    val profileResponseBody =  LambdaDsl.newJsonBody() { obj ->
        obj
                .id("id", 1)
                .stringType("name")
                .stringType("sortable_name")
                .stringType("short_name")
                .stringType("bio")
                .stringType("primary_email")
                .stringType("login_id")
                .stringType("locale")
                .stringType("effective_locale")
    }.build()
    @Pact(consumer = "mobile")
    fun getProfilePact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given(MAIN_PROVIDER_STATE)

                .uponReceiving("A request for user 1's profile")
                .path("/api/v1/users/self/profile")
                .method("GET")
                // TODO: Headers

                .willRespondWith()
                .status(200)
                .body(profileResponseBody)
                // TODO: Headers

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getProfilePact")
    fun `grab user 1's profile`() {
        val service = createService()

        val profileCall = service.getSelf()
        val profileResult = profileCall.execute()

        assertNotNull("Expected non-null response body", profileResult.body())
        val profile = profileResult.body()!!

        assertNotNull("id", profile.id)
        assertEquals("id", 1L, profile.id)
        assertNotNull("name",  profile.name)
        assertNotNull("shortName",  profile.shortName)
        assertNotNull("sortableName",  profile.sortableName)
        assertNotNull("bio",  profile.bio)
        assertNotNull("primaryEmail",  profile.primaryEmail)
        assertNotNull("loginId",  profile.loginId)
        assertNotNull("locale",  profile.locale)
        assertNotNull("effective_locale",  profile.effective_locale)
    }

    //endregion
}