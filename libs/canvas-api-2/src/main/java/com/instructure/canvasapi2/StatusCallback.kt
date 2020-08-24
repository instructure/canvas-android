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
 *
 */
package com.instructure.canvasapi2

import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

abstract class StatusCallback<DATA> : Callback<DATA> {

    private val mCalls = ArrayList<Call<DATA>>()

    @get:Synchronized
    var isCallInProgress = false
        private set

    var linkHeaders: LinkHeaders? = null

    var isCanceled = false
        private set

    val isFirstPage: Boolean
        get() = isFirstPage(linkHeaders)

    override fun onResponse(data: Call<DATA>, response: Response<DATA>) {
        isCallInProgress = true
        when {
            response.isSuccessful -> publishHeaderResponseResults(
                response,
                response.raw(),
                APIHelper.parseLinkHeaderResponse(response.headers())
            )
            else -> {
                onFail(data, Throwable("StatusCallback: " + response.code() + " Error"), response)
                // Note: 401 errors are handled in the CanvasAuthenticator

                // No response or no data
                onCallbackFinished(ApiType.API)
            }
        }
    }

    override fun onFailure(data: Call<DATA>, t: Throwable) {
        isCallInProgress = false
        if (data.isCanceled || "Canceled" == t.message) {
            Logger.d("StatusCallback: callback(s) were cancelled")
            onCancelled()
        } else {
            Logger.e("StatusCallback: Failure: " + t.message)
            onFail(data, t, null)
            onFinished(ApiType.API)
        }
    }

    fun onCallbackStarted() {
        isCallInProgress = true
        onStarted()
    }

    fun onCallbackFinished(type: ApiType) {
        isCallInProgress = false
        onFinished(type)
    }

    /**
     * Where all responses will report. Api or Cache.
     * @param response The data of the response
     * @param linkHeaders The link headers for the response, used for pagination
     * @param type The type of response, Cache or Api
     */
    open fun onResponse(response: Response<DATA>, linkHeaders: LinkHeaders, type: ApiType) {}

    /**
     * The result of a failed call
     * @param call The original call
     * @param error The error
     * @param response The data of the response, can be null
     */
    @JvmSuppressWildcards
    open fun onFail(call: Call<DATA>?, error: Throwable, response: Response<*>?) {}

    fun onCancelled() {}
    fun onStarted() {}
    open fun onFinished(type: ApiType) {}

    private fun publishHeaderResponseResults(
        response: Response<DATA>,
        okResponse: okhttp3.Response,
        linkHeaders: LinkHeaders
    ) {
        this.linkHeaders = linkHeaders
        val isCacheResponse = APIHelper.isCachedResponse(okResponse)
        Logger.d("Is Cache Response? " + if (isCacheResponse) "YES" else "NO")
        if (isCacheResponse) {
            onResponse(response, linkHeaders, ApiType.CACHE)
            onCallbackFinished(ApiType.CACHE)
        } else {
            onResponse(response, linkHeaders, ApiType.API)
            onCallbackFinished(ApiType.API)
        }
    }

    fun moreCallsExist(): Boolean {
        return moreCallsExist(linkHeaders)
    }

    private fun clearLinkHeaders() {
        linkHeaders = null
    }

    /**
     * Used to reset a callback to it's former glory
     * Clears the LinkHeaders
     * Cancels any ongoing Calls
     * Clears all calls from the ArrayList of Calls
     */
    open fun reset() {
        clearLinkHeaders()
        cancel()
        clearCalls()
        isCanceled = false
    }

    fun clearCalls() {
        mCalls.clear()
    }

    fun addCall(call: Call<DATA>): Call<DATA> {
        if (!call.isCanceled) isCanceled = true
        mCalls.add(call)
        return call
    }

    fun cancel() {
        isCanceled = true
        for (call in mCalls) {
            call.cancel()
        }
    }

    companion object {

        fun moreCallsExist(vararg headers: LinkHeaders?): Boolean = headers.getOrNull(0)?.nextUrl != null

        fun isFirstPage(vararg headers: LinkHeaders?): Boolean = headers.isEmpty() || headers[0] == null

        fun cancelAllCalls() = CanvasRestAdapter.cancelAllCalls()

    }

}
