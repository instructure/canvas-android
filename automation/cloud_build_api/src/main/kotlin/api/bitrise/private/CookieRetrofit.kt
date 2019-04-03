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


package api.bitrise.private

import api.RestAdapterUtils
import retrofit2.Retrofit

object CookieRetrofit {
    val privateCookieRetrofit: Retrofit by lazy {
        val bitriseHttpClient = RestAdapterUtils.createHttpClient()
        bitriseHttpClient.addInterceptor { chain ->

            val builder = chain.request().newBuilder()
            for (cookie in State.cookies) {
                builder.addHeader("Cookie", cookie)
            }

            builder.addHeader("X-XSRF-TOKEN", State.xsrfToken)

            chain.proceed(builder.build())
        }

        val baseUrl = "https://app.bitrise.io/"
        RestAdapterUtils.createRetrofit(bitriseHttpClient.build(), baseUrl)
    }
}

