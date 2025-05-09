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

import com.instructure.canvasapi2.utils.CedarAuthenticator
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DomainServicesAuthenticator
import com.instructure.canvasapi2.utils.PineAuthenticator
import com.instructure.canvasapi2.utils.RedwoodAuthenticator
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class DomainServicesAdapter {
    private val timeoutSeconds = 60
    protected val cacheSize = (20 * 1024 * 1024).toLong()
    private val dispatcher = Dispatcher()
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    protected abstract val cacheDirectory: File
    protected abstract val cache: Cache
    protected abstract val authenticator: DomainServicesAuthenticator
    protected abstract val requestInterceptor: DomainServicesRequestInterceptor

    fun buildOHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(requestInterceptor)
            .addNetworkInterceptor(ResponseInterceptor())
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .dispatcher(dispatcher)
            .authenticator(authenticator)
            .build()
    }
}

class PineAdapter @Inject constructor(
    pineRequestInterceptor: PineRequestInterceptor,
    pineAuthenticator: PineAuthenticator
) : DomainServicesAdapter() {
    override val cacheDirectory = File(ContextKeeper.appContext.cacheDir, "pine_cache")
    override val cache: Cache = Cache(cacheDirectory, cacheSize)
    override val authenticator = pineAuthenticator
    override val requestInterceptor = pineRequestInterceptor
}

class CedarAdapter @Inject constructor(
    cedarRequestInterceptor: CedarRequestInterceptor,
    cedarAuthenticator: CedarAuthenticator
) : DomainServicesAdapter() {
    override val cacheDirectory = File(ContextKeeper.appContext.cacheDir, "cedar_cache")
    override val cache: Cache = Cache(cacheDirectory, cacheSize)
    override val authenticator = cedarAuthenticator
    override val requestInterceptor = cedarRequestInterceptor
}

class RedwoodAdapter @Inject constructor(
    redwoodRequestInterceptor: RedwoodRequestInterceptor,
    redwoodAuthenticator: RedwoodAuthenticator
) : DomainServicesAdapter() {
    override val cacheDirectory = File(ContextKeeper.appContext.cacheDir, "redwood_cache")
    override val cache: Cache = Cache(cacheDirectory, cacheSize)
    override val authenticator = redwoodAuthenticator
    override val requestInterceptor = redwoodRequestInterceptor
}