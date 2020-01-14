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

import au.com.dius.pact.consumer.PactProviderRuleMk2
import au.com.dius.pact.model.PactSpecVersion
import com.instructure.canvasapi2.apis.CourseAPI
import org.junit.Rule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ApiPactTestBase {
    @Rule
    @JvmField
    val provider = PactProviderRuleMk2("Canvas LMS API", PactSpecVersion.V2, this)

    fun getClient(pathPrefix: String = "/api/v1/") : Retrofit {
        val client = Retrofit.Builder()
                .baseUrl(provider.url + pathPrefix)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        println("PACT: Provider URL: ${provider.url} client base url = ${client.baseUrl()}")
        return client
    }

    val MAIN_PROVIDER_STATE = "User 1, 4 courses, 2 favorited"
}