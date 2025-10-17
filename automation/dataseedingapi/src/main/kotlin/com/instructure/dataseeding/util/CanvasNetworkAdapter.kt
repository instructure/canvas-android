//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.util

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object CanvasNetworkAdapter {

    val canvasDomain = "mobileqa.beta.instructure.com"
    val baseUrl = "https://$canvasDomain/api/v1/"
    val redirectUri = "urn:ietf:wg:oauth:2.0:oob"
    val adminToken = DATA_SEEDING_ADMIN_TOKEN
    val clientId = DATA_SEEDING_CLIENT_ID
    val clientSecret = DATA_SEEDING_CLIENT_SECRET

    private var log = true
    private val TIMEOUT_IN_SECONDS = 60L

    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        if (log) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        return loggingInterceptor
    }

    private val adminOkHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthRequestInterceptor(adminToken)

        OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(authInterceptor)
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(RestRetryInterceptor)
                .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .build()
    }

    private fun okHttpClientWithToken(token: String): OkHttpClient {
        val authInterceptor = AuthRequestInterceptor(token)

        return OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(authInterceptor)
                .addInterceptor(getLoggingInterceptor())
                .addInterceptor(RestRetryInterceptor)
                .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .build()
    }

    private fun okHttpClientForApollo(token: String): OkHttpClient {
        val authInterceptor = AuthRequestInterceptor(token)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(getLoggingInterceptor())
            .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    fun getApolloClient(token: String): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://mobileqa.beta.instructure.com/api/graphql/")
            .okHttpClient(okHttpClientForApollo(token))
            .addCustomScalarAdapter(com.instructure.dataseedingapi.type.DateTime.type, DateTimeAdapter())
            .build()
    }

    private class DateTimeAdapter : com.apollographql.apollo.api.Adapter<java.util.Date> {
        override fun fromJson(reader: com.apollographql.apollo.api.json.JsonReader, customScalarAdapters: com.apollographql.apollo.api.CustomScalarAdapters): java.util.Date {
            val dateString = reader.nextString()
            return java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(dateString) ?: throw IllegalArgumentException("Invalid date: $dateString")
        }

        override fun toJson(writer: com.apollographql.apollo.api.json.JsonWriter, customScalarAdapters: com.apollographql.apollo.api.CustomScalarAdapters, value: java.util.Date) {
            val dateString = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(value)
            writer.value(dateString)
        }
    }

    private val noAuthOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(getLoggingInterceptor())
            .addInterceptor(RestRetryInterceptor)
            .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    val adminRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(adminOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    fun createAdminRetrofitClient(domain: String) : Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://$domain/api/v1/")
                .client(adminOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    fun retrofitWithToken(token: String): Retrofit =
            Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClientWithToken(token))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

    val noAuthRetrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(noAuthOkHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}