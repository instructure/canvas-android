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
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response

class ContinuationCallback<T>(private val continuation: CancellableContinuation<*>, private val onSuccess: SuccessCall<T>, var onError: ErrorCall = {}) : StatusCallback<T>() {

    override fun onResponse(response: Response<T>, linkHeaders: LinkHeaders, type: ApiType) {
        synchronized(continuation) {
            if (continuation.isCancelled) return
            @Suppress("UNCHECKED_CAST")
            onSuccess(response.body() as T)
        }
    }

    override fun onFail(call: Call<T>?, error: Throwable, response: Response<*>?) {
        synchronized(continuation) {
            if (continuation.isCancelled) return
            onError(StatusCallbackError(call, error, response))
        }
    }
}

suspend inline fun <reified A, reified B> awaitApis(
        crossinline callerA: (StatusCallback<A>) -> Unit,
        crossinline callerB: (StatusCallback<B>) -> Unit
): Pair<A, B> {
    val originStackTrace = Thread.currentThread().stackTrace
    return suspendCancellableCoroutine { continuation ->

        val payloads = arrayOfNulls<Any?>(2)

        val setPayload: (position: Int, payload: Any?) -> Unit = { position, payload ->
            payloads[position] = payload
            if (payloads.count { it == null } == 0) {
                continuation.resumeSafely(
                        Pair(payloads[0] as A, payloads[1] as B)
                )
            }
        }

        val callbackA = ContinuationCallback<A>(continuation, { setPayload(0, it) })
        val callbackB = ContinuationCallback<B>(continuation, { setPayload(1, it) })

        val onError: ErrorCall = {
            callbackA.cancel()
            callbackB.cancel()
            continuation.resumeSafelyWithException(it, originStackTrace)
        }

        callbackA.onError = onError
        callbackB.onError = onError

        continuation.invokeOnCancellation {
            callbackA.cancel()
            callbackB.cancel()
        }

        callerA(callbackA)
        callerB(callbackB)
    }
}

suspend inline fun <reified A, reified B, reified C> awaitApis(
        crossinline callerA: (StatusCallback<A>) -> Unit,
        crossinline callerB: (StatusCallback<B>) -> Unit,
        crossinline callerC: (StatusCallback<C>) -> Unit
): Triple<A, B, C> {
    val originStackTrace = Thread.currentThread().stackTrace
    return suspendCancellableCoroutine { continuation ->

        val payloads = arrayOfNulls<Any?>(3)

        val setPayload: (position: Int, payload: Any?) -> Unit = { position, payload ->
            payloads[position] = payload
            if (payloads.count { it == null } == 0) {
                continuation.resumeSafely(
                        Triple(payloads[0] as A, payloads[1] as B, payloads[2] as C)
                )
            }
        }

        val callbackA = ContinuationCallback<A>(continuation, { setPayload(0, it) })
        val callbackB = ContinuationCallback<B>(continuation, { setPayload(1, it) })
        val callbackC = ContinuationCallback<C>(continuation, { setPayload(2, it) })

        val onError: ErrorCall = {
            callbackA.cancel()
            callbackB.cancel()
            callbackC.cancel()
            continuation.resumeSafelyWithException(it, originStackTrace)
        }

        callbackA.onError = onError
        callbackB.onError = onError
        callbackC.onError = onError

        continuation.invokeOnCancellation {
            callbackA.cancel()
            callbackB.cancel()
            callbackC.cancel()
        }

        callerA(callbackA)
        callerB(callbackB)
        callerC(callbackC)
    }
}
