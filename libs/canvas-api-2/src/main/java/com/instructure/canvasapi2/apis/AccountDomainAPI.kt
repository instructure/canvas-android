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

import com.instructure.canvasapi2.BuildConfig
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountDomain

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url


object AccountDomainAPI {
            private const val DEFAULT_DOMAIN = BuildConfig.BASE_URL
//    private const val DEFAULT_DOMAIN = "https://canvas-test.emeritus.org/"

    interface AccountDomainInterface {
        @GET
        fun next(@Url nextURL: String): Call<List<AccountDomain>>

        @GET("accounts/search")
        fun campusSearch(
            @Query("search_term") term: String,
        ): Call<List<AccountDomain>>
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

//        val cookie =
//            "pseudonym_credentials=581%3A%3Af5e6966d78f42029ae08098ed5b3bb1c890313a4641b6053ad2498c3f51dfd3d330f3727522f636ce635882519e4dcf3fc67ecc3d86449fc209c713229279e7d%3A%3A01b2b28a3b10211f29c1509afa46f3159f9407237128715340233d989333755e; _csrf_token=ANOuXlTjrOke8u9Gtrq3peCVIZVToCexKS3wGEGLcsEylON1DYHBh0qXoH%2BEzN%2FSie15%2BzvPXfZuRMV0cudK6g%3D%3D; log_session_id=e2f9095d7a02ee4d18ddbfdbf76d167d; _legacy_normandy_session=6AIMEsz9ApuaM7ZKQSDKKQ+XX7mQ4YUVL8_rJ3rHf7g6PXhif6M5ifj2mRoBSlncPpQwb7KsDbaoL9CntrABY3gRPVyc5Ph…Nwr2SbAhCGTRaldbe0HF125wWgYWA8K-UG9HiO30E1eAvNyxermuaQLQ.kaaZgfagnCWMu5dmi0ceerrclh4.Y-nFFQ; _normandy_session=6AIMEsz9ApuaM7ZKQSDKKQ+XX7mQ4YUVL8_rJ3rHf7g6PXhif6M5ifj2mRoBSlncPpQwb7KsDbaoL9CntrABY3gRPVyc5PhSj3OByzrQ_-J8s7IHzSvkFxIvPvtvPzTCv5Hrggr9wyMXSifLXnKVMbLTyzCkqWF58NhTAlySUfQk0uo5hsCj9nmx2NZFiZaXzhBaTcgDV9qvEocSkhbs_YgjToMqdGLPkCra-5PDvV9zP6hB5YJvIJcLEH5F-lb5Fw5-oH6_tgTwwJ7GZYhye1e6gvClLsgrgNnhiKo7Wcoc-xiG9PDZgNwr2SbAhCGTRaldbe0HF125wWgYWA8K-UG9HiO30E1eAvNyxermuaQLQ.kaaZgfagnCWMu5dmi0ceerrclh4.Y-nFFQ"
        callback.addCall(
            adapter.build(AccountDomainInterface::class.java, params).campusSearch(query)
        ).enqueue(callback)
    }
}