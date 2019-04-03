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

package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Recipient

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


object RecipientAPI {

    internal interface RecipientInterface {
        @GET("search/recipients?synthetic_contexts=1")
        fun getFirstPageRecipientList(@Query("search") searchQuery: String?, @Query(value = "context", encoded = true) context: String): Call<List<Recipient>>

        @GET("search/recipients")
        fun getFirstPageRecipientListNoSyntheticContexts(@Query("search") searchQuery: String?, @Query(value = "context", encoded = true) context: String): Call<List<Recipient>>

        @GET
        fun getNextPageRecipientList(@Url url: String): Call<List<Recipient>>
    }

    fun getRecipients(searchQuery: String?, context: String, callback: StatusCallback<List<Recipient>>, adapter: RestBuilder, params: RestParams) {
        if (StatusCallback.isFirstPage(callback.linkHeaders)) {
            callback.addCall(adapter.build(RecipientInterface::class.java, params).getFirstPageRecipientList(searchQuery, context)).enqueue(callback)
        } else if (callback.linkHeaders != null && StatusCallback.moreCallsExist(callback.linkHeaders)) {
            callback.addCall(adapter.build(RecipientInterface::class.java, params).getNextPageRecipientList(callback.linkHeaders!!.nextUrl!!)).enqueue(callback)
        }
    }

    fun getFirstPageRecipients(forceNetwork: Boolean, searchQuery: String?, context: String, adapter: RestBuilder, callback: StatusCallback<List<Recipient>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(RecipientInterface::class.java, params).getFirstPageRecipientList(searchQuery, context)).enqueue(callback)
    }

    // Synthetic contexts == groups and sections, so this search will return only users
    fun getFirstPageRecipientsNoSyntheticContexts(forceNetwork: Boolean, searchQuery: String?, context: String, adapter: RestBuilder, callback: StatusCallback<List<Recipient>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        callback.addCall(adapter.build(RecipientInterface::class.java, params).getFirstPageRecipientListNoSyntheticContexts(searchQuery, context)).enqueue(callback)
    }

    fun getNextPageRecipients(forceNetwork: Boolean, nextUrl: String, adapter: RestBuilder, callback: StatusCallback<List<Recipient>>) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        adapter.build(RecipientInterface::class.java, params).getNextPageRecipientList(nextUrl).enqueue(callback)
    }
}
