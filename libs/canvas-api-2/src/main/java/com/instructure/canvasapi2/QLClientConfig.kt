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
 */
package com.instructure.canvasapi2

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.cache.http.HttpFetchPolicy
import com.apollographql.apollo.cache.http.httpCache
import com.apollographql.apollo.cache.http.httpExpireTimeout
import com.apollographql.apollo.cache.http.httpFetchPolicy
import com.apollographql.apollo.network.okHttpClient
import com.instructure.canvasapi2.type.DateTime
import com.instructure.canvasapi2.type.GraphQLID
import com.instructure.canvasapi2.type.URL
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

open class QLClientConfig {

    /** The GraphQL endpoint. Defaults to "<fullDomain>/api/graphql/" */
    var url: String = ApiPrefs.fullDomain + GRAPHQL_ENDPOINT

    /** The [OkHttpClient] to use for this request. Defaults to the client obtained from [CanvasRestAdapter.getOkHttpClient]
     * with a supplementary interceptor to add an additional header. It is recommended to use this default client as it
     * has several useful behaviors such as request logging, read timeouts, and auth/user-agent/referrer header injection. */
    var httpClient: OkHttpClient = CanvasRestAdapter.okHttpClient
        .newBuilder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().addHeader("GraphQL-Metrics", "true").build())
        }
        .build()

    var fetchPolicy: HttpFetchPolicy = HttpFetchPolicy.CacheFirst

    /** Builds a new [ApolloClient] based on the current config values. */
    fun buildClient(): ApolloClient {
        val builder = ApolloClient.Builder()
            .serverUrl(url)
            .okHttpClient(httpClient)
            .addCustomScalarAdapter(DateTime.type, timeAdapter)
            .addCustomScalarAdapter(URL.type, stringAdapter)
            .addCustomScalarAdapter(GraphQLID.type, stringAdapter)
            .httpCache(cacheFile, CACHE_SIZE)
            .httpFetchPolicy(fetchPolicy)
            .httpExpireTimeout(TimeUnit.HOURS.toMillis(1))

        return builder.build()
    }

    companion object {

        /** Default GraphQL endpoint */
        internal const val GRAPHQL_ENDPOINT = "/api/graphql/"
        internal const val GRAPHQL_PAGE_SIZE = 20

        /** Cache configuration */
        private const val CACHE_SIZE = 10L * 1024 * 1024 // 10MB disk cache
        private val cacheFile = File(ContextKeeper.appContext.cacheDir, "apolloCache/")

        /** Type adapter for Dates */
        private val timeAdapter: Adapter<Date?> = object : Adapter<Date?> {
            override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date? =
                if (reader.peek() == JsonReader.Token.NULL) {
                    reader.nextNull()
                    null
                } else {
                    reader.nextString().toDate()
                }

            override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date?) {
                value?.let { writer.value(it.toApiString()) } ?: writer.nullValue()
            }
        }

        /** Type adapter for fields that should be kept as Strings (e.g. Urls and IDs) */
        private val stringAdapter: Adapter<String?> = object : Adapter<String?> {
            override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters) =
                if (reader.peek() == JsonReader.Token.NULL) {
                    reader.nextNull()
                    null
                } else {
                    reader.nextString()
                }

            override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: String?) {
                value?.let { writer.value(it) } ?: writer.nullValue()
            }
        }

        suspend fun <DATA : Query.Data, T : Query<DATA>> enqueueQuery(
            query: T,
            forceNetwork: Boolean = false,
            block: QLClientConfig.() -> Unit = {}
        ): ApolloResponse<DATA> {
            val config = QLClientConfig()
            if (forceNetwork) config.fetchPolicy = HttpFetchPolicy.NetworkOnly
            config.block()
            // Since we handle errors with exceptions, we keep the compat call of execute because the new doesn't throw exceptions
            val result = config.buildClient().query(query).executeV3()
            return result
        }

        suspend fun <DATA : Mutation.Data, T : Mutation<DATA>> enqueueMutation(
            mutation: T,
            block: QLClientConfig.() -> Unit = {}
        ): ApolloResponse<DATA> {
            val config = QLClientConfig()
            config.block()
            // Since we handle errors with exceptions, we keep the compat call of execute because the new doesn't throw exceptions
            val result = config.buildClient().mutation(mutation).executeV3()
            return result
        }

        fun clearCacheDirectory(): Boolean {
            return try {
                cacheFile.deleteRecursively()
            } catch (e: Exception) {
                Logger.e("Could not delete cache $e")
                false
            }
        }
    }
}
