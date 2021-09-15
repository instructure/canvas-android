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

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class ResponseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        //We modify the response to be cache-able :)
        val builder = response.newBuilder()
        if (request.method == "GET") {
            builder.removeHeader("Pragma")

            /**
             * Replace any existing Cache-Control header
             *
             * Per okhttp/CacheControl.java
             * Accept cached responses that have exceeded their freshness lifetime by
             * up to `maxStale`. If unspecified, stale cache responses will not be
             * used.
             *
             * ie. Cached responses are valid to display for up-to two weeks
             */
            builder.header("Cache-Control", "public, max-stale=1209600")//2 Weeks (1209600)

            /**
             * Add a new one for max-age
             *
             * Per okhttp/CacheControl.java
             * Sets the maximum age of a cached response. If the cache response's age
             * exceeds `maxAge`, it will not be used and a network request will
             * be made.
             */
            builder.addHeader("Cache-Control", "public, max-age=3600")//1 Hour
        }
        return builder.build()
    }
}
