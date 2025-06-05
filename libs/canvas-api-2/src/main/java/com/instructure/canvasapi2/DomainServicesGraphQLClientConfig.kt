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

import com.apollographql.apollo.cache.http.HttpFetchPolicy
import okhttp3.OkHttpClient
import javax.inject.Inject

abstract class DomainServicesGraphQLClientConfig(
    private val fetchPolicy: HttpFetchPolicy = HttpFetchPolicy.NetworkOnly,
    private var url: String = "",
    private var httpClient: OkHttpClient = QLClientConfig().httpClient
) {
    fun createClientConfigBlock(): QLClientConfig.() -> Unit {
        return {
            fetchPolicy = this@DomainServicesGraphQLClientConfig.fetchPolicy
            url = this@DomainServicesGraphQLClientConfig.url
            httpClient = this@DomainServicesGraphQLClientConfig.httpClient
        }
    }
}

class PineGraphQLClientConfig @Inject constructor(
    adapter: PineAdapter
): DomainServicesGraphQLClientConfig(
    url = BuildConfig.PINE_BASE_URL + "/graphql",
    httpClient = adapter.buildOHttpClient()
)

class CedarGraphQLClientConfig @Inject constructor(
    adapter: CedarAdapter
): DomainServicesGraphQLClientConfig(
    url = BuildConfig.CEDAR_BASE_URL + "/graphql",
    httpClient = adapter.buildOHttpClient()
)

class RedwoodGraphQLClientConfig @Inject constructor(
    adapter: RedwoodAdapter
): DomainServicesGraphQLClientConfig(
    url = BuildConfig.REDWOOD_BASE_URL + "/graphql",
    httpClient = adapter.buildOHttpClient()
)