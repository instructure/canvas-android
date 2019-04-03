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


package api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestAdapterUtils {

    fun createHttpClient(auth: String? = null): OkHttpClient.Builder {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder().addInterceptor { chain ->
            var request = chain.request()
            val builder = request.newBuilder()

            if (auth != null) {
                builder.addHeader("Authorization", auth)
            }

            request = builder.build()

            chain.proceed(request)
        }
                .addInterceptor(RestRetryInterceptor())
//                .addInterceptor(logger) // uncomment for logging
                .retryOnConnectionFailure(true)
                .connectTimeout(0, TimeUnit.SECONDS) // 0 = no timeout
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)

    }


    val noAuthClient: OkHttpClient by lazy {
        createHttpClient(null).build()
    }

    fun createRetrofit(httpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}

