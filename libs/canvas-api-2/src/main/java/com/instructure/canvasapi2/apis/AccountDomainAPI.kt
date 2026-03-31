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

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

interface AccountDomainInterface {

    @GET
    suspend fun next(
        @Url nextURL: String,
        @Tag restParams: RestParams
    ): DataResult<List<AccountDomain>>

    @GET("accounts/search")
    suspend fun campusSearch(
        @Query("search_term") term: String,
        @Tag restParams: RestParams
    ): DataResult<List<AccountDomain>>
}