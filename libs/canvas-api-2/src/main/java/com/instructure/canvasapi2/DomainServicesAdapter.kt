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

import android.content.Context
import com.instructure.canvasapi2.utils.DomainServicesAuthenticator
import com.instructure.canvasapi2.utils.JourneyAuthenticator
import com.instructure.canvasapi2.utils.RedwoodAuthenticator
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class DomainServicesAdapter(
    cacheDirectory: File,
    private val authenticator: DomainServicesAuthenticator,
    private val requestInterceptor: DomainServicesRequestInterceptor
) {
    private val timeoutSeconds = TIMEOUT_SECONDS
    private val cacheSize = CACHE_SIZE
    private val cache: Cache = Cache(cacheDirectory, cacheSize)
    private val dispatcher = Dispatcher()
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }


    fun buildOHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(requestInterceptor)
            .addNetworkInterceptor(DomainServicesResponseInterceptor())
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .dispatcher(dispatcher)
            .authenticator(authenticator)
            .build()
    }

    companion object {
        const val CACHE_SIZE = (20 * 1024 * 1024).toLong()
        const val TIMEOUT_SECONDS = 10
    }
}

class RedwoodAdapter @Inject constructor(
    @ApplicationContext context: Context,
    redwoodRequestInterceptor: RedwoodRequestInterceptor,
    redwoodAuthenticator: RedwoodAuthenticator
) : DomainServicesAdapter(
    File(context.cacheDir, "redwood_cache"),
    redwoodAuthenticator,
    redwoodRequestInterceptor
)

class JourneyAdapter @Inject constructor(
    @ApplicationContext context: Context,
    journeyRequestInterceptor: JourneyRequestInterceptor,
    journeyAuthenticator: JourneyAuthenticator
) : DomainServicesAdapter(
    File(context.cacheDir, "journey_cache"),
    journeyAuthenticator,
    journeyRequestInterceptor
)