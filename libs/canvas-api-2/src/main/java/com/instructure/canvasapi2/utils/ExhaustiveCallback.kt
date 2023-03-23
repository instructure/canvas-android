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
package com.instructure.canvasapi2.utils

import com.instructure.canvasapi2.StatusCallback
import retrofit2.Call
import retrofit2.Response
import java.util.*

@JvmSuppressWildcards
abstract class ExhaustiveCallback<MODEL, out ITEM>(
        private val callback: StatusCallback<List<ITEM>>
) : StatusCallback<@JvmSuppressWildcards MODEL>() {

    private val extractedItems = ArrayList<ITEM>()
    private var finished = false

    abstract fun getNextPage(callback: StatusCallback<MODEL>, nextUrl: String, isCached: Boolean)

    abstract fun extractItems(response: MODEL) : List<ITEM>

    fun moreCallsExist(linkHeaders: LinkHeaders): Boolean = StatusCallback.moreCallsExist(linkHeaders)

    fun getNextUrl(linkHeaders: LinkHeaders): String = linkHeaders.nextUrl!!

    override fun onResponse(response: Response<MODEL>, linkHeaders: LinkHeaders, type: ApiType) {
        if (callback.isCanceled) { cancel(); return }
        response.body()?.let {
            val items = extractItems(it)
            extractedItems.addAll(items)
            if (moreCallsExist(linkHeaders)) {
                getNextPage(this, getNextUrl(linkHeaders), type.isCache)
            } else {
                finished = true
                callback.onResponse(Response.success<List<ITEM>>(extractedItems, response.raw()), linkHeaders, type)
            }
        }
    }

    override fun onFail(call: Call<MODEL>?, error: Throwable, response: Response<*>?) {
        finished = true
        callback.onFail(null, error, response)
    }

    override fun onFinished(type: ApiType) {
        if (finished) callback.onFinished(type)
    }

    override fun reset() {
        finished = false
        super.reset()
    }
}
