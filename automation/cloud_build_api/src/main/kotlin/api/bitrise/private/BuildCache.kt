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

import api.bitrise.private.CookieRetrofit.privateCookieRetrofit
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

object BuildCache {
    interface ApiService {
        // Delete all build caches for an app.
        // https://www.bitrise.io/app/58159e359f1ecda7/build-caches.json
        @DELETE("app/{appSlug}/build-caches.json")
        fun delete(@Path("appSlug") appSlug: String): Call<Status>

        @GET("app/{appSlug}/build-caches.json")
        fun get(@Path("appSlug") appSlug: String): Call<List<CacheItem>>
    }

    private val apiService: ApiService by lazy {
        privateCookieRetrofit.create(ApiService::class.java)
    }

    fun delete(appSlug: String): Status {
        return apiService.delete(appSlug).execute().body() ?: Status(null, null)
    }

    fun get(appSlug: String): List<CacheItem> {
        return apiService.get(appSlug).execute().body() ?: emptyList()
    }
}
