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

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.network.okHttpClient
import com.instructure.canvasapi2.type.DateTime
import com.instructure.canvasapi2.type.GraphQLID
import com.instructure.canvasapi2.type.URL
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import okhttp3.OkHttpClient
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class QLClientConfig {

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

//    var cachePolicy: HttpCachePolicy.Policy = cacheFirstPolicy

    /** Builds a new [ApolloClient] based on the current config values. */
    fun buildClient(): ApolloClient {
        val builder = ApolloClient.Builder()
            .serverUrl(url)
            .okHttpClient(httpClient)
            .addCustomScalarAdapter(DateTime.type, timeAdapter)
            .addCustomScalarAdapter(URL.type, stringAdapter)
            .addCustomScalarAdapter(GraphQLID.type, stringAdapter)
        //.httpCache(cache)
        //.defaultHttpCachePolicy(cachePolicy)

//        val builder = ApolloClient.builder()
//            .serverUrl(url)
//            .okHttpClient(httpClient)
//            .addCustomTypeAdapter(CustomType.DATETIME, timeAdapter)
//            .addCustomTypeAdapter(CustomType.URL, stringAdapter)
//            .addCustomTypeAdapter(CustomType.ID, stringAdapter)
//            .httpCache(cache)
//            .defaultHttpCachePolicy(cachePolicy)


        return builder.build()
    }

    companion object {

        /** Default GraphQL endpoint */
        internal const val GRAPHQL_ENDPOINT = "/api/graphql/"
        internal const val GRAPHQL_PAGE_SIZE = 20

        /** Cache configuration */
        private const val CACHE_SIZE = 10L * 1024 * 1024 // 10MB disk cache
        private val cacheFile = File(ContextKeeper.appContext.cacheDir, "apolloCache/")
//        private val cache = ApolloHttpCache(DiskLruHttpCacheStore(cacheFile, CACHE_SIZE))
//        private val cacheFirstPolicy: HttpCachePolicy.ExpirePolicy = HttpCachePolicy.CACHE_FIRST.expireAfter(1, TimeUnit.HOURS)

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
//            if (forceNetwork) config.cachePolicy = HttpCachePolicy.NETWORK_ONLY
            config.block()
            val result = config.buildClient().query(query).execute()
            return result
        }

        suspend fun <DATA : Mutation.Data, T : Mutation<DATA>> enqueueMutation(
            mutation: T,
            block: QLClientConfig.() -> Unit = {}
        ): ApolloResponse<DATA> {
            val config = QLClientConfig()
            config.block()
            val result = config.buildClient().mutation(mutation).execute()
            return result
        }
    }
}
