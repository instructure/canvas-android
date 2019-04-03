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
import com.instructure.canvasapi2.models.AccountDomain

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


object AccountDomainAPI {
    private const val DEFAULT_DOMAIN = "https://canvas.instructure.com/"

    interface AccountDomainInterface {
        @GET
        fun next(@Url nextURL: String): Call<List<AccountDomain>>

        @GET("accounts/search")
        fun campusSearch(@Query("search_term") term: String): Call<List<AccountDomain>>
    }

    fun searchAccounts(query: String?, callback: StatusCallback<List<AccountDomain>>) {
        if (query == null || query.length < 3) return

        val adapter = RestBuilder(callback)
        val params = RestParams(
                shouldIgnoreToken = true,
                usePerPageQueryParam = true,
                isForceReadFromNetwork = true,
                domain = DEFAULT_DOMAIN
        )

        callback.addCall(adapter.build(AccountDomainInterface::class.java, params).campusSearch(query)).enqueue(callback)
    }
}