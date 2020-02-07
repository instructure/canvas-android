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

import au.com.dius.pact.consumer.junit.PactProviderRule
import au.com.dius.pact.core.model.PactSpecVersion
import com.instructure.canvasapi2.PactRequestInterceptor
import com.instructure.canvasapi2.apis.CourseAPI
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Rule
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class ApiPactTestBase {
    @Rule
    @JvmField
    val provider = PactProviderRule("Canvas LMS API", PactSpecVersion.V2, this)

    fun getClient(pathPrefix: String = "/api/v1/") : Retrofit {

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(PactRequestInterceptor("Student1"))
                .addInterceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()
                    builder.addHeader("Content-Type", "application/json")
                    chain.proceed(builder.build())
                }
                .build()
        val client = Retrofit.Builder()
                .baseUrl(provider.url + pathPrefix)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        //println("PACT: Provider URL: ${provider.url} client base url = ${client.baseUrl()}")
        return client
    }

    fun <T> assertQueryParamsAndPath(call: Call<T>, expectedQuery: String?, expectedPath: String?)
    {
        assertEquals("Call Query Params", call.request().url().query(), expectedQuery)
        assertEquals("Call Path", call.request().url().url().path, expectedPath)
    }

    val DEFAULT_REQUEST_HEADERS = mapOf(
            "Authorization" to "Bearer some_token",
            "Auth-User" to "Student1",
            "Content-Type" to "application/json"
    )

    val DEFAULT_RESPONSE_HEADERS = mapOf(
            "Content-Type" to "application/json; charset=utf-8"
    )

    val MAIN_PROVIDER_STATE = "User 1, 4 courses, 2 favorited"
}