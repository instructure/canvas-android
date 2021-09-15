//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.util

import okhttp3.Interceptor
import okhttp3.Response

object RestRetryInterceptor : Interceptor {

    private const val MAX_RETRIES = 6

    private val Response.failed
        get() = !this.isSuccessful

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var attempt = 1
        var response = chain.proceed(request)
        while (response.failed && attempt <= MAX_RETRIES) {
            RetryBackoff.wait(attempt)
            response = chain.proceed(request)
            attempt += 1
        }

        if (response.failed) {
            val code = response.code
            val body = response.body?.string()
            throw RuntimeException("status code: $code\nbody: $body")
        }

        return response
    }
}
