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
 *
 */
package com.instructure.canvasapi2

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloCanceledException
import com.apollographql.apollo.exception.ApolloException
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.Logger
import java.util.*


abstract class QLCallback<DATA> : ApolloCall.Callback<DATA>() {

    /* A list of [ApolloCall]s associated with this callback */
    private val mCalls = ArrayList<ApolloCall<DATA>>()

    /* The current execution status */
    private var status : ApolloCall.StatusEvent = ApolloCall.StatusEvent.SCHEDULED

    /**
     * Whether there is currently a call in progress
     */
    var isExecuting = false
        private set

    /**
     * Whether the request has been canceled
     */
    var isCanceled = false
        private set

    /**
     * The end cursor obtained from the page info of the most recently requested list. This is used for pagination and
     * should be set by the calling code after a request has completed successfully. The default value of a blank string
     * indicates that the first page has yet to be fetched, while a null value indicates there are no more pages.
     */
    var nextCursor: String? = ""

    override fun onStatusEvent(event: ApolloCall.StatusEvent) {
        status = event
        isExecuting = when (status) {
            ApolloCall.StatusEvent.SCHEDULED, ApolloCall.StatusEvent.FETCH_CACHE, ApolloCall.StatusEvent.FETCH_NETWORK -> true
            ApolloCall.StatusEvent.COMPLETED -> false
        }
    }

    override fun onCanceledError(e: ApolloCanceledException) {
        isCanceled = true
        isExecuting = false
    }

    override fun onResponse(response: Response<DATA>) {
        if (isCanceled) {
            Logger.d("QLCallback: callback was cancelled")
            return
        }
        onResponse(response, if (response.isFromCache) ApiType.CACHE else ApiType.API)
    }

    override fun onFailure(e: ApolloException) {
        if (isCanceled) {
            Logger.d("QLCallback: callback was cancelled")
            return
        }

        // Note: 401s are handled in CanvasAuthenticator

        Logger.e("QLCallback: Failure: ${e.message}")
        onFail(e)
    }

    /**
     * Called when a request successfully completes.
     * @param response The data of the response
     * @param type The type of response, [ApiType.CACHE] or [ApiType.API]
     */
    abstract fun onResponse(response: Response<DATA>, type: ApiType)

    /**
     * The result of a failed call
     * @param e The [ApolloException]
     */
    abstract fun onFail(e: ApolloException)

    /**
     * Resets a callback to its former glory. This cancels and forgets any tracked [ApolloCall]s, and resets the [nextCursor].
     */
    fun reset() {
        nextCursor = ""
        cancel()
        mCalls.clear()
        isCanceled = false
    }

    /**
     * Adds an [ApolloCall] to be tracked internally. When [cancel] is called, all tracked calls will be canceled as well.
     */
    fun addCall(call: ApolloCall<DATA>): ApolloCall<DATA> {
        mCalls.add(call)
        return call
    }

    /**
     * Cancels this callback and any tracked [ApolloCall]s. Neither [onResponse] nor [onFail] will be called for the
     * current request once this is called.
     */
    fun cancel() {
        isCanceled = true
        mCalls.forEach { it.cancel() }
    }

}
