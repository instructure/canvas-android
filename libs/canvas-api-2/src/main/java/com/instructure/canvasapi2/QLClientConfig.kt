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
import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.CustomTypeValue
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.type.CustomType
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

    var cachePolicy: HttpCachePolicy.Policy = cacheFirstPolicy

    /** Whether the request should be made without the authentication header. Default it false. */
    @Suppress("MemberVisibilityCanBePrivate")
    var ignoreAuthToken = false

    /** Builds a new [ApolloClient] based on the current config values. */
    fun buildClient(): ApolloClient {
        val builder = ApolloClient.builder()
            .serverUrl(url)
            .okHttpClient(httpClient)
            .addCustomTypeAdapter(CustomType.DATETIME, timeAdapter)
            .addCustomTypeAdapter(CustomType.URL, stringAdapter)
            .addCustomTypeAdapter(CustomType.ID, stringAdapter)
            .httpCache(cache)
            .defaultHttpCachePolicy(cachePolicy)


        /* The default httpClient has a request interceptor which automatically adds the authentication header, but
        this behavior can be disabled if a RestParams object attached to the request specifies otherwise. */
        if (ignoreAuthToken) builder.callFactory {
            val restParams = RestParams(shouldIgnoreToken = ignoreAuthToken)
            val request = it.newBuilder().tag(restParams).build()
            httpClient.newCall(request)
        }

        return builder.build()
    }

    companion object {

        /** Default GraphQL endpoint */
        internal const val GRAPHQL_ENDPOINT = "/api/graphql/"
        internal const val GRAPHQL_PAGE_SIZE = 20

        /** Cache configuration */
        private const val CACHE_SIZE = 10L * 1024 * 1024 // 10MB disk cache
        private val cacheFile = File(ContextKeeper.appContext.cacheDir, "apolloCache/")
        private val cache = ApolloHttpCache(DiskLruHttpCacheStore(cacheFile, CACHE_SIZE))
        private val cacheFirstPolicy: HttpCachePolicy.ExpirePolicy = HttpCachePolicy.CACHE_FIRST.expireAfter(1, TimeUnit.HOURS)

        /** Type adapter for Dates */
        private val timeAdapter: CustomTypeAdapter<Date?> = object : CustomTypeAdapter<Date?> {
            override fun encode(value: Date?): CustomTypeValue<*> = value?.let { CustomTypeValue.GraphQLString(it.toApiString().orEmpty()) } ?: CustomTypeValue.GraphQLNull
            override fun decode(value: CustomTypeValue<*>) = value.value.toString().toDate()
        }

        /** Type adapter for fields that should be kept as Strings (e.g. Urls and IDs) */
        private val stringAdapter: CustomTypeAdapter<String?> = object : CustomTypeAdapter<String?> {
            override fun encode(value: String?) = value?.let { CustomTypeValue.GraphQLString(it) } ?: CustomTypeValue.GraphQLNull
            override fun decode(value: CustomTypeValue<*>) = value.value.toString()
        }

    }

}

/**
 * Calls the specified function [block] with a [QLClientConfig] instance as its receiver and enqueues the provided [Query] request.
 */
fun <DATA, T : Query<*, DATA, *>> QLCallback<DATA>.enqueueQuery(query: T, block: QLClientConfig.() -> Unit = {} ) {
    val config = QLClientConfig()
    config.block()
    val call = config.buildClient().query(query)
    addCall(call).enqueue(this)
}

/**
 * Calls the specified function [block] with a [QLClientConfig] instance as its receiver and enqueues the provided [Mutation] request.
 */
fun <DATA, T : Mutation<*, DATA, *>> QLCallback<DATA>.enqueueMutation(mutation: T, block: QLClientConfig.() -> Unit = {} ) {
    val builder = QLClientConfig()
    builder.block()
    val call = builder.buildClient().mutate(mutation)
    addCall(call).enqueue(this)
}
