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
import com.instructure.canvasapi2.models.AccountDomainModel

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


object AccountDomainAPI {
    private const val DEFAULT_DOMAIN = "http://192.168.100.171:8080"

    interface AccountDomainInterface {
        @GET
        fun next(@Url nextURL: String): Call<AccountDomainModel>

        @GET("abbreviation/labs")
        fun campusSearch(@Query("abbreviation") term: String): Call<AccountDomainModel>

        @GET("7f465432-7fe9-4028-a36a-6b5305af1193")
        fun campusSearch2(): Call<AccountDomainModel>
    }

    fun searchAccounts(query: String?, callback: StatusCallback<AccountDomainModel>) {
        if (query == null || query.length < 3) return

        val adapter = RestBuilder(callback)
        val params = RestParams(
                shouldIgnoreToken = true,
                usePerPageQueryParam = false,
                isForceReadFromNetwork = true,
                domain = DEFAULT_DOMAIN,
                apiVersion = "/api/schools/"

        )

        callback.addCall(adapter.build(AccountDomainInterface::class.java, params).campusSearch(query)).enqueue(callback)
    }

    fun searchAccounts2(callback: StatusCallback<AccountDomainModel>) {

        val adapter = RestBuilder(callback)
        val params = RestParams(
                shouldIgnoreToken = true,
                usePerPageQueryParam = false,
                isForceReadFromNetwork = true,
                domain = DEFAULT_DOMAIN,
                apiVersion = "/v3/"
        )

        callback.addCall(adapter.build(AccountDomainInterface::class.java, params).campusSearch2()).enqueue(callback)
    }
}