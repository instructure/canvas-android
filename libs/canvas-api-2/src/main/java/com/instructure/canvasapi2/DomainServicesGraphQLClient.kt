/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2

import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.cache.http.HttpFetchPolicy
import okhttp3.OkHttpClient
import javax.inject.Inject

abstract class DomainServicesGraphQLClient: QLClientConfig() {
    override var fetchPolicy = HttpFetchPolicy.NetworkOnly // We don't need to cache AI responses

    suspend fun <DATA : Query.Data, T : Query<DATA>> enqueueQuery(
        query: T,
        forceNetwork: Boolean = false,
    ): ApolloResponse<DATA> {
        val originalFetchPolicy = fetchPolicy
        if (forceNetwork) fetchPolicy = HttpFetchPolicy.NetworkOnly
        val result = buildClient().query(query).execute()
        fetchPolicy = originalFetchPolicy

        return result
    }

    suspend fun <DATA : Mutation.Data, T : Mutation<DATA>> enqueueMutation(
        mutation: T,
    ): ApolloResponse<DATA> {
        val result = buildClient().mutation(mutation).execute()
        return result
    }
}

class PineGraphQLClient @Inject constructor(
    adapter: PineAdapter
): DomainServicesGraphQLClient() {
    override var httpClient: OkHttpClient = adapter.buildOHttpClient()
    override var url = BuildConfig.PINE_BASE_URL + "/graphql"
}

class CedarGraphQLClient @Inject constructor(
    adapter: CedarAdapter
): DomainServicesGraphQLClient() {
    override var httpClient: OkHttpClient = adapter.buildOHttpClient()
    override var url = BuildConfig.CEDAR_BASE_URL + "/graphql"
}

class RedwoodGraphQLClient @Inject constructor(
    adapter: RedwoodAdapter
): DomainServicesGraphQLClient() {
    override var httpClient: OkHttpClient = adapter.buildOHttpClient()
    override var url = BuildConfig.REDWOOD_BASE_URL + "/graphql"
}