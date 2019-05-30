/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response

/**
 * Awaits a single API call and returns the result.
 *
 * @param managerCall Block in which the API call should be Started using the provided [StatusCallback]
 * @throws StatusCallbackError if there was an API error
 */
suspend fun <T> awaitApi(managerCall: ManagerCall<T>): T {
    val originStackTrace = Thread.currentThread().stackTrace
    return suspendCancellableCoroutine { continuation ->
        val callback = object : StatusCallback<T>() {

            override fun onResponse(response: Response<T>, linkHeaders: LinkHeaders, type: ApiType) {
                @Suppress("UNCHECKED_CAST")
                continuation.resumeSafely(response.body() as T)
            }

            override fun onFail(call: Call<T>?, error: Throwable, response: Response<*>?) {
                continuation.resumeSafelyWithException(StatusCallbackError(call, error, response), originStackTrace)
            }
        }
        continuation.invokeOnCancellation { callback.cancel() }
        managerCall(callback)
    }
}

/**
 * Awaits a single API call and returns the resulting [Response]. Use this when you need to obtain response data in
 * addition to the payload, such as the status code or response headers.
 *
 * @param managerCall Block in which the API call should be Started using the provided [StatusCallback]
 * @throws StatusCallbackError if there was an API error
 */
suspend fun <T> awaitApiResponse(managerCall: ManagerCall<T>): Response<T> {
    return suspendCancellableCoroutine { continuation ->
        val callback = object : StatusCallback<T>() {

            override fun onResponse(response: Response<T>, linkHeaders: LinkHeaders, type: ApiType) {
                continuation.resumeSafely(response)
            }

            override fun onFail(call: Call<T>?, error: Throwable, response: Response<*>?) {
                continuation.resumeSafelyWithException(StatusCallbackError(call, error, response))
            }
        }
        continuation.invokeOnCancellation { callback.cancel() }
        managerCall(callback)
    }
}
