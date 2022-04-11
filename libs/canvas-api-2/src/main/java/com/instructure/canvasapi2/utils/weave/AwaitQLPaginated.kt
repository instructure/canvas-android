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
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Suspends execution and awaits a paginated GraphQL query. The first page is immediately requested upon
 * invocation of [awaitQLPaginated], and subsequent pages may be requested by calling [WeaveCoroutine.next].
 * Execution resumes once all pages have been exhausted.
 *
 * Use [onRequest { callback -> }][PaginationQLConfig.onRequest] for initiating API calls
 * and [onResponse { payload -> }][PaginationQLConfig.onResponse] to handle the response for each page. The onResponse
 * block should parse and return the end cursor of the paginated data, or return null if there are no more pages.
 * See [PaginationQLConfig] for additional configuration options.
 */
suspend inline fun <reified DATA> WeaveCoroutine.awaitQLPaginated(crossinline configure: PaginationQLConfig<DATA>.() -> Unit) {
    return suspendCancellableCoroutine { continuation ->
        val config = PaginationQLConfig<DATA>()
        config.configure()
        addAndStartStitcher(WeaveQLPager(config, PaginationQLCallback(), continuation, this))
    }
}

/**
 * Holds callbacks for configuring the behavior of [WeaveQLPager]
 */
class PaginationQLConfig<DATA> {
    var requestBlock: ApolloManagerCall<DATA>? = null
    fun PaginationQLConfig<DATA>.onRequest(block: ApolloManagerCall<DATA>) { requestBlock = block }

    var responseBlock: (payload: DATA) -> String? = { throw NotImplementedError("onResponse{} is not specified") }
    fun PaginationQLConfig<DATA>.onResponse(block: (payload: DATA) -> String?) { responseBlock = block }

    var completeBlock: () -> Unit = {}
    fun PaginationQLConfig<DATA>.onComplete(block: () -> Unit) { completeBlock = block }

    var errorBlock: ApolloErrorCall? = null
    fun PaginationQLConfig<DATA>.onError(block: ApolloErrorCall) { errorBlock = block }
}

/**
 * A [Stitcher] which fulfills GraphQL pagination requests
 */
class WeaveQLPager<DATA>(
    private val config: PaginationQLConfig<DATA>,
    private val pageCallback: PaginationQLCallback<DATA>,
    override var continuation: CancellableContinuation<Unit>,
    private val weaveCoroutine: WeaveCoroutine
) : Stitcher {

    override var onRelease: () -> Unit = {}

    private var isCanceled = false

    init {
        requireNotNull(config.requestBlock)

        /* Listen for cancellation */
        continuation.invokeOnCancellation { cancel() }

        /* Set up response logic */
        pageCallback.responseCallback = responseBlock@ { response ->
            if (isCanceled) return@responseBlock
            weaveCoroutine.onUI {
                if (response.hasErrors()) {
                    val message = response.errors!!.first().message
                    pageCallback.errorCallback(ApolloException(message))
                } else if (response.data == null) {
                    pageCallback.errorCallback(ApolloException("Response data is null!"))
                } else {
                    pageCallback.nextCursor = config.responseBlock(response.data!!)
                    if (pageCallback.nextCursor.isNullOrBlank()) {
                        config.completeBlock()
                        onRelease()
                        continuation.resumeSafely(Unit)
                    }
                }
            }
        }

        /* Set up failure logic */
        pageCallback.errorCallback = errorBlock@ { error ->
            if (isCanceled) return@errorBlock
            weaveCoroutine.onUI {
                if (config.errorBlock != null) {
                    config.errorBlock?.invoke(error)
                    onRelease()
                    continuation.resumeSafely(Unit)
                } else {
                    onRelease()
                    continuation.resumeSafelyWithException(error)
                }
            }
        }
    }

    override fun next() {
        if (pageCallback.isExecuting) return
        weaveCoroutine.onUI { config.requestBlock?.invoke(pageCallback) }
    }

    fun cancel() {
        isCanceled = true
        pageCallback.cancel()
    }

}

class PaginationQLCallback<DATA> : QLCallback<DATA>() {

    lateinit var responseCallback: (Response<DATA>) -> Unit
    lateinit var errorCallback: (e: ApolloException) -> Unit

    override fun onResponse(response: Response<DATA>, type: ApiType) {
        responseCallback(response)
    }

    override fun onFail(e: ApolloException) {
        errorCallback(e)
    }
}
