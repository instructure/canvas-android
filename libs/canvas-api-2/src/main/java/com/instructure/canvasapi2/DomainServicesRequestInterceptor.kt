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

import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.CedarApiPref
import com.instructure.canvasapi2.utils.PineApiPref
import com.instructure.canvasapi2.utils.RedwoodApiPref
import com.instructure.canvasapi2.utils.restParams
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

abstract class DomainServicesRequestInterceptor(
    val apiPrefs: ApiPrefs
): Interceptor {
    fun intercept(chain: Interceptor.Chain, token: String?): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        val userAgent = apiPrefs.userAgent
        val params = request.restParams() ?: RestParams()

        if (userAgent != "") {
            builder.addHeader("User-Agent", userAgent)
        }

        if (!params.shouldIgnoreToken && !token.isNullOrEmpty()) {
            builder.addHeader("Authorization", "Bearer $token")
        }

        if (params.isForceReadFromCache) {
            builder.cacheControl(CacheControl.FORCE_CACHE)
        } else if (APIHelper.hasNetworkConnection() && params.isForceReadFromNetwork) {
            builder.cacheControl(CacheControl.FORCE_NETWORK)
        }

        return chain.proceed(builder.build())
    }
}

class PineRequestInterceptor @Inject constructor(
    private val pineApiPref: PineApiPref,
    apiPrefs: ApiPrefs
) : DomainServicesRequestInterceptor(apiPrefs) {
    override fun intercept(chain: Interceptor.Chain): Response {
        return super.intercept(chain, pineApiPref.token)
    }
}

class CedarRequestInterceptor @Inject constructor(
    private val cedarApiPref: CedarApiPref,
    apiPrefs: ApiPrefs
) : DomainServicesRequestInterceptor(apiPrefs) {
    override fun intercept(chain: Interceptor.Chain): Response {
        return super.intercept(chain, cedarApiPref.token)
    }
}

class RedwoodRequestInterceptor @Inject constructor(
    private val redwoodApiPref: RedwoodApiPref,
    apiPrefs: ApiPrefs
) : DomainServicesRequestInterceptor(apiPrefs) {
    override fun intercept(chain: Interceptor.Chain): Response {
        return super.intercept(chain, redwoodApiPref.token)
    }
}