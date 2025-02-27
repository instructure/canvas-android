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

import android.net.Uri
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*


class RequestInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()
        val builder = request.newBuilder()

        val token = ApiPrefs.getValidToken()
        val userAgent = ApiPrefs.userAgent
        val domain = ApiPrefs.fullDomain

        // Nearly all requests are instantiated using RestBuilder and will have been tagged with
        // a RestParams instance. Here we will attempt to retrieve it, but if unsuccessful we will
        // fall back to a new RestParams instance with default values.
        val params: RestParams
        params = when {
            request.tag(RestParams::class.java) != null -> {
                request.tag(RestParams::class.java) ?: RestParams()
            }
            request.tag() != null && request.tag() is RestParams -> {
                request.tag() as RestParams
            }
            else -> {
                RestParams()
            }
        }

        // Set the UserAgent
        if (userAgent != "") {
            builder.addHeader("User-Agent", userAgent)
        }

        // Authenticate if possible
        if (!params.shouldIgnoreToken && token != "") {
            builder.addHeader("Authorization", "Bearer $token")
        }

        // Add Accept-Language header for a11y
        builder.addHeader("accept-language", params.acceptLanguageOverride ?: acceptedLanguageString)

        if (params.isForceReadFromCache) {
            // Only want cached data
            builder.cacheControl(CacheControl.FORCE_CACHE)
        } else if (APIHelper.hasNetworkConnection() && params.isForceReadFromNetwork) {
            // Typical from a pull-to-refresh. We do not use this when the network is unavailable because we want
            // requests to fall back to cached responses for a better offline experience.
            builder.cacheControl(CacheControl.FORCE_NETWORK)
        }

        // Strip out non-ascii characters, otherwise addHeader may throw an exception
        builder.addHeader("Referer", domain.replace("[^\\x20-\\x7e]".toRegex(), ""))

        request = builder.build()

        // Masquerade if necessary
        if (ApiPrefs.isMasquerading) {
            val url = request.url.newBuilder().addQueryParameter("as_user_id", java.lang.Long.toString(ApiPrefs.masqueradeId)).build()
            request = request.newBuilder().url(url).build()
        }

        if (params.usePerPageQueryParam) {
            val url = request.url.newBuilder().addQueryParameter("per_page", Integer.toString(ApiPrefs.perPageCount)).build()
            request = request.newBuilder().url(url).build()
        }

        if (params.disableFileVerifiers) {
            val url = request.url.newBuilder().addQueryParameter("no_verifiers", "1").build()
            request = request.newBuilder().url(url).build()
        }


        if (domain.isNotEmpty() && params.domain != null && domain != params.domain) {
            val uri = Uri.parse(params.domain)
            val url = request.url.newBuilder().scheme(uri.scheme ?: "https")
                .host(uri.host ?: params.domain.removePrefix("https://")).build()
            request = request.newBuilder().url(url).build()
        }

        return chain.proceed(request)
    }

    companion object {
        // This is kinda gross, but Android is terrible and doesn't use the standard for lang strings...
        val locale: String
            get() = Locale.getDefault().toString().replace("_", "-")

        val acceptedLanguageString: String
            get() {
                val language = Locale.getDefault().language
                return "$locale,$language"
            }

        // Canvas supports Chinese (Traditional) and Chinese (Simplified)
        // Canvas only supports 3 region tags (not including Chinese), remove any other tags
        val sessionLocaleString: String
            get() {
                var lang = locale
                if (lang.equals("zh-hk", ignoreCase = true) || lang.equals("zh-tw", ignoreCase = true) || lang.equals("zh-hant-hk", ignoreCase = true) || lang.equals("zh-hant-tw", ignoreCase = true)) {
                    lang = "zh-Hant"
                } else if (lang.equals("zh", ignoreCase = true) || lang.equals("zh-cn", ignoreCase = true) || lang.equals("zh-hans-cn", ignoreCase = true)) {
                    lang = "zh-Hans"
                } else if (!lang.equals("pt-BR", ignoreCase = true) && !lang.equals("en-AU", ignoreCase = true) && !lang.equals("en-GB", ignoreCase = true)) {
                    lang = Locale.getDefault().language
                }

                return "?session_locale=$lang"
            }
    }
}
