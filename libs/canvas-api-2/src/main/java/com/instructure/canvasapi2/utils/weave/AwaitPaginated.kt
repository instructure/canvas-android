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
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response

/**
 * Begins a [WeaveCoroutine] in the context of [awaitPaginated]
 */
inline fun <reified T> weavePaginated(crossinline configure: PaginationConfig<T>.() -> Unit) = weave { awaitPaginated(configure) }

/**
 * Suspends execution and awaits a paginated API. The first page is immediately requested upon
 * invocation of [awaitPaginated], and subsequent pages may be requested by calling [WeaveCoroutine.next].
 * Execution resumes once all pages have been exhausted.
 *
 * Use [onRequest { callback -> }][PaginationConfig.onRequest] for initiating API calls
 * and [onResponse { payload -> }][PaginationConfig.onResponse] to handle the response for each page.
 * For APIs that require a separate call for the first page, use [onRequestFirst { callback -> }][PaginationConfig.onRequestFirst]
 * for the first page and [onRequestNext { nextUrl, callback -> }][PaginationConfig.onRequestNext] for
 * subsequent pages. See [PaginationConfig] for additional configuration options.
 */
@OptIn(InternalCoroutinesApi::class)
suspend inline fun <reified T> WeaveCoroutine.awaitPaginated(crossinline configure: PaginationConfig<T>.() -> Unit) {
    val originStackTrace = Thread.currentThread().stackTrace
    return suspendCancellableCoroutine { continuation ->
        val config = PaginationConfig<T>()
        config.configure()
        addAndStartStitcher(WeavePager(config, PaginationCallback(), continuation, originStackTrace))
    }
}

/**
 * Holds callbacks for configuring the behavior of [WeavePager]
 */
class PaginationConfig<T> {
    var requestBlock: ManagerCall<T>? = null
    fun PaginationConfig<T>.onRequest(block: ManagerCall<T>) { requestBlock = block }

    var requestFirstBlock: ManagerCall<T>? = null
    fun PaginationConfig<T>.onRequestFirst(block: ManagerCall<T>) { requestFirstBlock = block }

    var requestNextBlock: ((String, StatusCallback<T>) -> Unit)? = null
    fun PaginationConfig<T>.onRequestNext(block: (nextUrl: String, callback: StatusCallback<T>) -> Unit) { requestNextBlock = block }

    var responseBlock: SuccessCall<T> = {}
    fun PaginationConfig<T>.onResponse(block: SuccessCall<T>) { responseBlock = block }

    var extractNextUrlBlock: (T) -> String? = { null }
    fun PaginationConfig<T>.extractNextUrl(block: (response: T) -> String?) { extractNextUrlBlock = block }

    var preRequestBlock: () -> Unit = {}
    fun PaginationConfig<T>.preRequest(block: () -> Unit) { preRequestBlock = block }

    var completeBlock: () -> Unit = {}
    fun PaginationConfig<T>.onComplete(block: () -> Unit) { completeBlock = block }

    var errorBlock: ErrorCall? = null
    fun PaginationConfig<T>.onError(block: ErrorCall) { errorBlock = block }

    var exhaustive: Boolean = false
}

enum class PagerType { SPLIT, UNIFIED }

/**
 * A [Stitcher] which fulfills API pagination requests
 */
class WeavePager<T>(
        private val config: PaginationConfig<T>,
        private val pageCallback: PaginationCallback<T>,
        override var continuation: CancellableContinuation<Unit>,
        private val originStackTrace: Array<StackTraceElement>
) : Stitcher {

    override var onRelease: () -> Unit = {}

    private var nextUrl: String? = null
    private var isFirstPage = true
    private var isCanceled = false

    private val type: PagerType = when {
        config.requestBlock != null -> PagerType.UNIFIED
        config.requestFirstBlock != null && config.requestNextBlock != null -> PagerType.SPLIT
        else -> throw IllegalArgumentException("Must specify either onRequest{} or BOTH onRequestFirst{} AND onRequestNext{}")
    }

    init {
        /* Listen for cancellation */
        continuation.invokeOnCancellation { cancel() }

        /* Set up response logic */
        pageCallback.responseCallback = { response, linkHeaders ->
            if (!isCanceled) {
                response.body()?.let {
                    isFirstPage = false
                    nextUrl = config.extractNextUrlBlock(it) ?: linkHeaders?.nextUrl
                    config.responseBlock(it)
                    if (nextUrl.isNullOrBlank()) {
                        config.completeBlock()
                        onRelease()
                        continuation.resumeSafely(Unit)
                    }
                }
            }
        }

        pageCallback.finishedCallback = {
            if (!isCanceled && !nextUrl.isNullOrBlank() && config.exhaustive) next()
        }

        /* Set up failure logic */
        pageCallback.errorCallback = { error ->
            if (!isCanceled) {
                if (config.errorBlock != null) {
                    config.errorBlock?.invoke(error)
                    onRelease()
                    continuation.resumeSafely(Unit)
                } else {
                    onRelease()
                    continuation.resumeSafelyWithException(error, originStackTrace)
                }
            }
        }
    }

    override fun next() {
        if (pageCallback.isCallInProgress) return
        config.preRequestBlock()
        when (type) {
            PagerType.SPLIT -> {
                pageCallback.reset()
                if (isFirstPage) {
                    config.requestFirstBlock?.invoke(pageCallback)
                } else {
                    config.requestNextBlock?.invoke(nextUrl.orEmpty(), pageCallback)
                }
            }
            PagerType.UNIFIED -> config.requestBlock?.invoke(pageCallback)
        }
    }

    fun cancel() {
        isCanceled = true
        pageCallback.cancel()
    }

}

class PaginationCallback<T> : StatusCallback<T>() {
    lateinit var responseCallback: (Response<T>, LinkHeaders?) -> Unit
    lateinit var errorCallback: ErrorCall
    lateinit var finishedCallback: (ApiType) -> Unit

    override fun onResponse(response: Response<T>, linkHeaders: LinkHeaders, type: ApiType) {
        responseCallback(response, linkHeaders)
    }

    override fun onFinished(type: ApiType) {
        finishedCallback(type)
    }

    override fun onFail(call: Call<T>?, error: Throwable, response: Response<*>?) {
        errorCallback(StatusCallbackError(call, error, response))
    }
}
