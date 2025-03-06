/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */    package com.instructure.canvasapi2.utils.pageview

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag
import retrofit2.http.Url

object PandataManager {

    fun getToken(appKey: PandataInfo.AppKey, callback: StatusCallback<PandataInfo>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()

        PandataApi.getPandataToken(appKey, adapter, params, callback)
    }

    fun uploadPageViewEvents(url: String, token: String, events: PageViewUploadList, callback: StatusCallback<Void>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(shouldIgnoreToken = true)

        PandataApi.uploadPageViewEvents(url, token, events, adapter, params, callback)
    }
}

object PandataApi {

    interface PandataInterface {

        @POST("users/self/pandata_events_token")
        fun getPandataToken(@Query("app_key") appKey: String): Call<PandataInfo>

        @POST("users/self/pandata_events_token")
        suspend fun getPandataToken(@Query("app_key") appKey: String, @Tag params: RestParams): DataResult<PandataInfo>

        @POST
        fun uploadPageViewEvents(
                @Url url: String,
                @Header("Authorization") bearer: String,
                @Body events: PageViewUploadList
        ): Call<Void>

        @POST
        suspend fun uploadPageViewEvents(
            @Url url: String,
            @Header("Authorization") bearer: String,
            @Body events: PageViewUploadList,
            @Tag params: RestParams
        ): DataResult<Unit>
    }

    fun getPandataToken(
            appKey: PandataInfo.AppKey,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<PandataInfo>
    ) {
        callback.addCall(adapter.build(PandataInterface::class.java, params).getPandataToken(appKey.key))
                .enqueue(callback)
    }

    fun uploadPageViewEvents(
            url: String,
            token: String,
            events: PageViewUploadList,
            adapter: RestBuilder,
            params: RestParams,
            callback: StatusCallback<Void>
    ) {
        val bearer = "Bearer $token"
        callback.addCall(adapter.build(PandataInterface::class.java, params).uploadPageViewEvents(url, bearer, events))
                .enqueue(callback)
    }
}