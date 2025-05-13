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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.dataResult
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag
import java.io.IOException


object OAuthAPI {

    interface OAuthInterface {
        @DELETE("/login/oauth2/token")
        fun deleteToken(): Call<Void>

        @FormUrlEncoded
        @POST("/login/oauth2/token")
        fun getToken(
                @Field("client_id") clientId: String,
                @Field("client_secret") clientSecret: String,
                @Field("code") oAuthRequest: String,
                @Field(value = "redirect_uri", encoded = true) redirectURI: String,
                @Field("grant_type") grantType: String = "authorization_code"): Call<OAuthTokenResponse>

        @GET("/login/session_token")
        fun getAuthenticatedSession(@Query("return_to") targetUrl: String): Call<AuthenticatedSession>

        @GET("/login/session_token")
        suspend fun getAuthenticatedSession(@Query("return_to") targetUrl: String, @Tag params: RestParams): DataResult<AuthenticatedSession>

        @GET("/api/v1/login/session_token")
        fun getAuthenticatedSessionMasquerading(@Query("return_to") targetUrl: String, @Query("as_user_id") userId: Long): Call<AuthenticatedSession>

        @GET("/api/v1/login/session_token")
        suspend fun getAuthenticatedSessionMasquerading(@Query("return_to") targetUrl: String, @Query("as_user_id") userId: Long, @Tag params: RestParams): DataResult<AuthenticatedSession>

        @FormUrlEncoded
        @POST("/login/oauth2/token")
        fun refreshAccessToken(
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field(value = "redirect_uri", encoded = true) redirectURI: String,
            @Field("refresh_token") refreshToken: String,
            @Field("grant_type") grantType: String = "refresh_token"
        ): Call<OAuthTokenResponse>
    }

    fun deleteToken(adapter: RestBuilder, params: RestParams, callback: StatusCallback<Void>) {
        callback.addCall(adapter.build(OAuthInterface::class.java, params).deleteToken()).enqueue(callback)
    }

    fun getToken(adapter: RestBuilder, params: RestParams, clientID: String, clientSecret: String, oAuthRequest: String, callback: StatusCallback<OAuthTokenResponse>) {
        callback.addCall(adapter.build(OAuthInterface::class.java, params).getToken(clientID, clientSecret, oAuthRequest, "urn:ietf:wg:oauth:2.0:oob")).enqueue(callback)
    }

    fun refreshAccessToken(adapter: RestBuilder, params: RestParams): DataResult<OAuthTokenResponse> {
        return adapter.build(OAuthInterface::class.java, params).refreshAccessToken(ApiPrefs.clientId, ApiPrefs.clientSecret, "urn:ietf:wg:oauth:2.0:oob", ApiPrefs.refreshToken).dataResult()
    }

    fun getAuthenticatedSession(targetUrl: String, params: RestParams, adapter: RestBuilder, callback: StatusCallback<AuthenticatedSession>) {
        callback.addCall(adapter.build(OAuthInterface::class.java, params).getAuthenticatedSession(targetUrl)).enqueue(callback)
    }

    fun getAuthenticatedSessionMasquerading(targetUrl: String, userId: Long, params: RestParams, adapter: RestBuilder, callback: StatusCallback<AuthenticatedSession>) {
        callback.addCall(adapter.build(OAuthInterface::class.java, params).getAuthenticatedSessionMasquerading(targetUrl, userId)).enqueue(callback)
    }

    fun getAuthenticatedSessionSynchronous(targetUrl: String, params: RestParams, adapter: RestBuilder): String? {
        return try {
            adapter.build(OAuthInterface::class.java, params).getAuthenticatedSession(targetUrl).execute().body()?.sessionUrl
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun authBearer(token: String) = "Bearer $token"
}
