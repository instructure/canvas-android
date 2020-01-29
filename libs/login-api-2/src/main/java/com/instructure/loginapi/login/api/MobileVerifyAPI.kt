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
package com.instructure.loginapi.login.api

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.utils.APIHelper.paramIsNull
import com.instructure.canvasapi2.utils.ApiPrefs.userAgent
import com.instructure.canvasapi2.utils.Logger.d
import com.instructure.loginapi.login.model.DomainVerificationResult
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object MobileVerifyAPI {
    private val authenticationRetrofit: Retrofit
        private get() {
            val userAgent = userAgent
            val httpClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        if (userAgent != "") {
                            val request = chain.request().newBuilder()
                                    .header("User-Agent", userAgent)
                                    .cacheControl(CacheControl.FORCE_NETWORK)
                                    .build()
                            chain.proceed(request)
                        } else {
                            chain.proceed(chain.request())
                        }
                    }
                    .build()
            return Retrofit.Builder()
                    .baseUrl("https://canvas.instructure.com/api/v1/")
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }

    fun mobileVerify(domain: String?, callback: StatusCallback<DomainVerificationResult?>?) {
        if (paramIsNull(callback, domain)) {
            return
        }
        val userAgent = userAgent
        if (userAgent == "") {
            d("User agent must be set for this API to work correctly!")
            return
        }
        val oAuthInterface = authenticationRetrofit.create(OAuthInterface::class.java)
        oAuthInterface.mobileVerify(domain, userAgent).enqueue(callback)
    }

    internal interface OAuthInterface {
        @GET("mobile_verify.json")
        fun mobileVerify(@Query(value = "domain", encoded = false) domain: String?, @Query("user_agent") userAgent: String?): Call<DomainVerificationResult?>
    }
}