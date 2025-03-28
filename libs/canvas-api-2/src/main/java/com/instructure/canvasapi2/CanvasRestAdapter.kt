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

import android.net.http.HttpResponseCache
import com.google.gson.GsonBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.calladapter.DataResultCallAdapterFactory
import com.instructure.canvasapi2.di.CanvasAuthenticatorEntryPoint
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import dagger.hilt.android.EarlyEntryPoints
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * To use this adapter use RestBuilder
 */
abstract class CanvasRestAdapter
/**
 * Constructor for CanvasRestAdapter
 * @param statusCallback Only null when not making calls via callbacks. RestBuilder requires one
 */
protected constructor(var statusCallback: StatusCallback<*>?, private val authUser: String? = null) {

    private val okHttpClientNoRedirects: OkHttpClient
        get() {
            var client = okHttpClient
            client = client.newBuilder().followRedirects(false).build()

            return client
        }

    private val okHttpClientNoRetry: OkHttpClient
        get() {
            return okHttpClient.newBuilder()
                        .retryOnConnectionFailure(false)
                        .build()
        }

    private val okHttpClientForTest: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor(PactRequestInterceptor(authUser))
            .addNetworkInterceptor(ResponseInterceptor())
            .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            .authenticator(canvasAuthenticator)
            .dispatcher(mDispatcher)
            .build()
    }

    fun deleteCache() {
        try {
            okHttpClient.cache?.evictAll()
        } catch (e: IOException) {
            Logger.e("Failed to delete cache $e")
        }

    }

    //region Adapter Builders

    fun buildAdapterForTest(params: RestParams): Retrofit {

        return Retrofit.Builder()
                .baseUrl(params.domain!! + params.apiVersion)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClientForTest)
                .build()
    }

    fun buildAdapterNoRedirects(params: RestParams): Retrofit {
        val params = if (params.domain.isNullOrEmpty()) params.copy(domain = ApiPrefs.fullDomain) else params

        statusCallback?.onCallbackStarted()

        // Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.domain == "") {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)")
            return Retrofit.Builder().baseUrl("https://invalid.domain.com/").build()
        }

        var apiContext = ""
        if (params.canvasContext != null) {
            apiContext = when (params.canvasContext.type) {
                CanvasContext.Type.COURSE -> "courses/"
                CanvasContext.Type.GROUP -> "groups/"
                CanvasContext.Type.SECTION -> "sections/"
                else -> "users/"
            }
        }

        // Sets the auth token, user agent, and handles masquerading.
        val restParams = params
        return Retrofit.Builder()
                .baseUrl(params.domain + params.apiVersion + apiContext)
                .addConverterFactory(GsonConverterFactory.create())
                .callFactory { request ->
                    // Tag this request with the rest params so we can access them later in RequestInterceptor
                    okHttpClientNoRedirects.newCall(request.newBuilder().tag(restParams).build())
                }
                .build()
    }

    fun buildAdapterSerializeNulls(params: RestParams): Retrofit {
        val params = if (params.domain.isNullOrEmpty()) params.copy(domain = ApiPrefs.fullDomain) else params

        statusCallback?.onCallbackStarted()

        // Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.domain == "") {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)")
            return Retrofit.Builder().baseUrl("https://invalid.domain.com/").build()
        }

        var apiContext = ""
        if (params.canvasContext != null) {
            apiContext = when (params.canvasContext.type) {
                CanvasContext.Type.COURSE -> "courses/"
                CanvasContext.Type.GROUP -> "groups/"
                CanvasContext.Type.SECTION -> "sections/"
                else -> "users/"
            }
        }

        // Sets the auth token, user agent, and handles masquerading.
        val restParams = params
        return Retrofit.Builder()
                .baseUrl(params.domain + params.apiVersion + apiContext)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
                .addCallAdapterFactory(DataResultCallAdapterFactory())
                .callFactory { request ->
                    // Tag this request with the rest params so we can access them later in RequestInterceptor
                    okHttpClient.newCall(request.newBuilder().tag(restParams).build())
                }
                .build()
    }

    fun buildRollCallAdapter(url: String): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = if (DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(RollCallInterceptor())
                    .authenticator(canvasAuthenticator)
                    .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                    .dispatcher(mDispatcher)
                    .build()
            )
            .build()
    }

    fun buildAdapter(params: RestParams): Retrofit = buildAdapterHelper(
            if (params.domain.isNullOrEmpty()) params.copy(domain = ApiPrefs.fullDomain)
            else params
    )

    fun buildAdapterUpload(params: RestParams): Retrofit {
        val params = if (params.domain.isNullOrEmpty()) params.copy(domain = ApiPrefs.fullDomain) else params

        statusCallback?.onCallbackStarted()

        // Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.domain == "") {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)")
            return Retrofit.Builder().baseUrl("https://invalid.domain.com/").build()
        }

        var apiContext = ""
        if (params.canvasContext != null) {
            apiContext = when (params.canvasContext.type) {
                CanvasContext.Type.COURSE -> "courses/"
                CanvasContext.Type.GROUP -> "groups/"
                CanvasContext.Type.SECTION -> "sections/"
                else -> "users/"
            }
        }

        val restParams = params
        // Sets the auth token, user agent, and handles masquerading.
        return Retrofit.Builder()
                .baseUrl(params.domain + params.apiVersion + apiContext)
                .addConverterFactory(GsonConverterFactory.create())
                .callFactory { request ->
                    // Tag this request with the rest params so we can access them later in RequestInterceptor
                    okHttpClientNoRetry.newCall(request.newBuilder().tag(restParams).build())
                }
                .build()
    }

    private fun buildAdapterHelper(params: RestParams): Retrofit {

        statusCallback?.onCallbackStarted()

        // Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.domain == "") {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)")
            return Retrofit.Builder()
                .baseUrl("https://invalid.domain.com/")
                // Add a converter here so that unmocked tests will result in an API failure instead of a crash
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(DataResultCallAdapterFactory())
                .build()
        }

        var apiContext = ""
        if (params.canvasContext != null) {
            apiContext = when (params.canvasContext.type) {
                CanvasContext.Type.COURSE -> "courses/"
                CanvasContext.Type.GROUP -> "groups/"
                CanvasContext.Type.SECTION -> "sections/"
                else -> "users/"
            }
        }

        return finalBuildAdapter(params, apiContext).build()
    }

    /**
     * @param params RestParams
     * @param apiContext courses, groups, sections, users, or nothing
     * @return Retrofit.Builder
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun finalBuildAdapter(params: RestParams, apiContext: String): Retrofit.Builder {
        // Sets the auth token, user agent, and handles masquerading.
        return Retrofit.Builder()
            .baseUrl(params.domain + params.apiVersion + apiContext)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(DataResultCallAdapterFactory())
            .client(okHttpClient)
            .callFactory { request ->
                // Tag this request with the rest params so we can access them later in RequestInterceptor
                okHttpClient.newCall(request.newBuilder().tag(params).build())
            }
    }

    companion object {

        private const val DEBUG = true
        private const val TIMEOUT_IN_SECONDS = 60
        private const val CACHE_SIZE = (20 * 1024 * 1024).toLong()

        private var mHttpCacheDirectory: File? = null
        private val mDispatcher = Dispatcher()
        private var mCache: Cache? = null
        var client: OkHttpClient? = null

        private val canvasAuthenticator by lazy {
            EarlyEntryPoints.get(
                ContextKeeper.appContext.applicationContext,
                CanvasAuthenticatorEntryPoint::class.java
            ).canvasAuthenticator()
        }

        val cacheDirectory: File
            get() {
                if (mHttpCacheDirectory == null) {
                    mHttpCacheDirectory = File(ContextKeeper.appContext.cacheDir, "canvasCache")
                }
                return mHttpCacheDirectory!!
            }

        val okHttpClient: OkHttpClient
            get() {
                if (mCache == null) {
                    mCache = Cache(cacheDirectory, CACHE_SIZE)
                }

                try {
                    if (HttpResponseCache.getInstalled() == null) {
                        HttpResponseCache.install(cacheDirectory, CACHE_SIZE)
                    }
                } catch (e: IOException) {
                    Logger.e("Failed to install the cache directory")
                }

                if (client == null) {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.level = if (DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE

                    client = OkHttpClient.Builder()
                        .cache(mCache)
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor(RequestInterceptor())
                        .addNetworkInterceptor(ResponseInterceptor())
                        .readTimeout(TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
                        .dispatcher(mDispatcher)
                        .authenticator(canvasAuthenticator)
                        .build()
                }

                return client!!
            }
        //endregion

        fun cancelAllCalls() {
            mDispatcher.cancelAll()
        }

        /**
         * Removes cached responses for all urls containing the provided regex [pattern]
         */
        fun clearCacheUrls(pattern: String) {
            synchronized(okHttpClient) {
                val regex = Regex(pattern)
                val urls = okHttpClient.cache?.urls() ?: return
                while (urls.hasNext()) {
                    val next = urls.next()
                    if (regex.containsMatchIn(next)) {
                        urls.remove()
                        Logger.d("Clearing cached url: $next")
                    }
                }
            }
        }
    }

}
