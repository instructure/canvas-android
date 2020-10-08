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
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING", "unused")

package com.instructure.canvasapi2.utils.weave

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.instructure.canvasapi2.QLCallback
import com.instructure.canvasapi2.utils.ApiType
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Type alias for the call to begin a GraphQL API request
 */
internal typealias ApolloManagerCall<T> = (callback: QLCallback<T>) -> Unit

/**
 * Type alias for the call invoke on API failure
 * */
internal typealias ApolloErrorCall = (error: ApolloException) -> Unit

/**
 * Awaits a single GraphQL API call and returns the result.
 *
 * @param managerCall Block in which the API call should be Started using the provided [QLCallback]
 * @throws ApolloException if there was an error executing the query
 */
suspend fun <DATA> awaitQL(managerCall: ApolloManagerCall<DATA>): DATA {
    return suspendCancellableCoroutine { continuation ->
        val callback = object : QLCallback<DATA>() {
            override fun onResponse(response: Response<DATA>, type: ApiType) {
                when {
                    response.hasErrors() -> onFail(ApolloException(response.errors!!.first().message))
                    response.data == null -> onFail(ApolloException("Response data is null!"))
                    else -> continuation.resumeSafely(response.data!!)
                }
            }

            override fun onFail(e: ApolloException) {
                continuation.resumeSafelyWithException(e)
            }
        }
        continuation.invokeOnCancellation { callback.cancel() }
        managerCall(callback)
    }
}
