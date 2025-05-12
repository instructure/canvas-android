/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class DomainServicesResponseInterceptor : Interceptor {

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

        // Map 200 to 401 when body contains unathorized error
        if (response.code == 200) {
            val body = response.body
            val bodyString = body?.string()
            if (bodyString != null && bodyString.contains("UNAUTHENTICATED") && bodyString.contains("401")) {
                builder
                    .body(bodyString.toResponseBody(body.contentType())) // Body can be consumed only once, so we need to set it again
                    .code(401)
            } else {
                builder.body(bodyString?.toResponseBody(body.contentType())) // Body can be consumed only once, so we need to set it again
            }
        }

        return builder.build()
    }
}