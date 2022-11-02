/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response

/**
 * Starts a single API call and returns its future [DataResult] payload as an implementation of [Deferred].
 *
 * This function does not throw [StatusCallbackError]. Any errors that occurred while retrieving the payload
 * will return as an instance of [DataResult.Fail].
 *
 * @param managerCall Block in which the API call should be started using the provided [StatusCallback]
 */
fun <T> apiAsync(managerCall: ManagerCall<T>): Deferred<DataResult<T>> {
    val deferred = CompletableDeferred<DataResult<T>>()

    val callback = object : StatusCallback<T>() {

        override fun onResponse(response: Response<T>, linkHeaders: LinkHeaders, type: ApiType) {
            @Suppress("UNCHECKED_CAST")
            deferred.complete(DataResult.Success(response.body() as T, linkHeaders, type))
        }

        override fun onFail(call: Call<T>?, error: Throwable, response: Response<*>?) {
            val failure = if (response?.code() == 401) {
                Failure.Authorization(response.message())
            } else {
                Failure.Network(response?.message())
            }
            deferred.complete(DataResult.Fail(failure))
        }
    }

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            callback.cancel()
        }
    }

    managerCall(callback)

    return deferred
}

/**
 * A convenience function which awaits a successful result and returns its payload, or throws an exception
 * if the result is not successful.
 */
suspend fun <T> Deferred<DataResult<T>>.awaitOrThrow(): T {
    val result = await()
    if (result.isSuccess) {
        return result.dataOrThrow
    } else {
        throw RuntimeException(result.toString())
    }
}
