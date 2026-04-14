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
 *
 */
package com.instructure.dataseeding.api

import com.google.gson.JsonObject
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CommunicationChannel
import com.instructure.dataseeding.model.CreateUser
import com.instructure.dataseeding.model.OAuthToken
import com.instructure.dataseeding.model.PairingCodeResponseModel
import com.instructure.dataseeding.model.Pseudonym
import com.instructure.dataseeding.model.TermsOfServiceApiResponseModel
import com.instructure.dataseeding.model.User
import com.instructure.dataseeding.model.UserSettingsApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contains an interface that defines APIs for User endpoints
 * as well as methods for making the Retrofit calls to those APIs
 */
object UserApi {
    interface UserService {

        @POST("accounts/self/users")
        fun createCanvasUser(@Body createUser: CreateUser): Call<CanvasUserApiModel>

        @POST("/login/oauth2/token")
        fun getToken(
                @Query("client_id") clientId: String,
                @Query("client_secret") clientSecret: String,
                @Query("code") authCode: String,
                @Query(value = "redirect_uri", encoded = true) redirectURI: String
        ): Call<OAuthToken>

        @DELETE("/login/oauth2/token?expire_sessions=1")
        fun deleteToken(@Query("access_token") accessToken: String): Call<Unit>

        @PUT("users/{userId}/settings")
        fun putSelfSettings(@Path("userId") userId: Long, @Body body: UserSettingsApiModel): Call<UserSettingsApiModel>

        @POST("users/{userId}/observer_pairing_codes")
        fun postGeneratePairingCode(@Path("userId") userId: Long): Call<PairingCodeResponseModel>

        @GET("accounts/self/terms_of_service")
        fun getTermsOfService(): Call<TermsOfServiceApiResponseModel>

        @GET("users/{userId}/profile?include[]=avatar_url")
        fun getUserProfile(@Path("userId") userId: Long): Call<CanvasUserApiModel>
    }

    private fun userService(token: String): UserService
            = CanvasNetworkAdapter.retrofitWithToken(token).create(UserService::class.java)

    private val userAdminService: UserService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(UserService::class.java)
    }

    fun putSelfSettings(userId: Long,
                        requestApiModel: UserSettingsApiModel) {
         userAdminService.putSelfSettings(userId, requestApiModel).execute()
    }

    fun postGeneratePairingCode(userId: Long): PairingCodeResponseModel {
        return userAdminService.postGeneratePairingCode(userId).execute().body()!!
    }

    fun getTermsOfService(): TermsOfServiceApiResponseModel {
        return userAdminService.getTermsOfService().execute().body()!!
    }

    fun getUserProfile(userId: Long): CanvasUserApiModel {
        return userAdminService.getUserProfile(userId).execute().body()!!
    }

    fun createCanvasUser(
            userService: UserService = userAdminService,
            userDomain: String = CanvasNetworkAdapter.canvasDomain
    ): CanvasUserApiModel {
        val teacherName = Randomizer.randomName()
        val user = User(teacherName.fullName, teacherName.firstName, teacherName.sortableName)

        val pseudonym = Pseudonym(
                Randomizer.randomEmail(),
                Randomizer.randomPassword()
        )

        val communicationChannel = CommunicationChannel(true)

        val createUser = CreateUser(user, pseudonym, communicationChannel)

        val createdUser = userService.createCanvasUser(createUser).execute().body()!!

        // Add extra data to the CanvasUserApiModel
        with(createdUser) {
            loginId = createUser.pseudonym.uniqueId
            password = createUser.pseudonym.password
            token = getToken(this, userService, userDomain)
            domain = userDomain
        }

        return createdUser
    }

    /**
     * Gets an access token for the userApiModel as described [here](https://sso.canvaslms.com/doc/api/file.oauth_endpoints.html)
     * @param[userApiModel] A [CanvasUserApiModel]
     * @return An [String] access token for the userApiModel. NOTE: the token has an expiration of 1 hour.
     */
    private fun getToken(
            userApiModel: CanvasUserApiModel,
            userService: UserService = userAdminService,
            userDomain: String = CanvasNetworkAdapter.canvasDomain
    ): String {
        val authCode = getAuthCode(userApiModel, userDomain)
        val response = userService.getToken(
                CanvasNetworkAdapter.clientId,
                CanvasNetworkAdapter.clientSecret,
                authCode,
                CanvasNetworkAdapter.redirectUri
        ).execute()
        return response.body()?.accessToken ?: ""
    }

    fun deleteToken(
        accessToken: String
    ) {
        userService(accessToken).deleteToken(accessToken).execute()
    }

    /**
     * Gets an authentication code for the userApiModel as described [here](https://sso.canvaslms.com/doc/api/file.oauth_endpoints.html)
     * @param[userApiModel] A [CanvasUserApiModel]
     * @return The [String] auth code to be used to acquire the userApiModel's access token
     */
    private fun getAuthCode(userApiModel: CanvasUserApiModel, domain: String = CanvasNetworkAdapter.canvasDomain): String {
        val cookieStore = mutableMapOf<String, MutableMap<String, Cookie>>()
        val cookieJar = object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.getOrPut(url.host) { mutableMapOf() }.apply { cookies.forEach { put(it.name, it) } }
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> =
                cookieStore[url.host]?.values?.toList() ?: emptyList()
        }
        val httpClient = OkHttpClient.Builder().followRedirects(true).cookieJar(cookieJar).build()

        val loginPageUrl = "https://$domain/login/oauth2/auth".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("client_id", CanvasNetworkAdapter.clientId)
            .addQueryParameter("response_type", "code")
            .addQueryParameter("redirect_uri", CanvasNetworkAdapter.redirectUri)
            .build()
        val loginPageResponse = httpClient.newCall(Request.Builder().url(loginPageUrl).get().build()).execute()
        val loginPageHtml = loginPageResponse.body?.string() ?: ""

        val csrfToken = loginPageResponse.header("X-Csrf-Token")
            ?: Jsoup.parse(loginPageHtml).select("meta[name=csrf-token]").attr("content").takeIf { it.isNotEmpty() }
            ?: cookieStore[domain]?.values
                ?.firstOrNull { it.name.equals("_csrf_token", ignoreCase = true) }
                ?.value?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            ?: throw RuntimeException(
                "CSRF token not found.\nResponse headers: ${loginPageResponse.headers}\n" +
                "Cookies on $domain: ${cookieStore[domain]?.keys}\nAll cookies: ${cookieStore.mapValues { it.value.keys }}"
            )

        val loginJson = JsonObject().apply {
            addProperty("authenticity_token", csrfToken)
            add("pseudonym_session", JsonObject().apply {
                addProperty("unique_id", userApiModel.loginId)
                addProperty("password", userApiModel.password)
                addProperty("remember_me", "0")
            })
        }.toString()
        val loginResponse = httpClient.newCall(
            Request.Builder()
                .url("https://$domain/login/canvas")
                .post(loginJson.toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()
        val loginStatusCode = loginResponse.code
        val loginResponseHtml = loginResponse.body?.string() ?: ""
        val loginFinalUrl = loginResponse.request.url.toString()

        // If the code is already in the URL (no consent step needed), return it directly
        loginFinalUrl.toHttpUrlOrNull()?.queryParameter("code")?.let { return it }

        val consentForm = Jsoup.parse(loginResponseHtml, "https://$domain").select("form").first()
            ?: throw RuntimeException(
                "OAuth consent form not found at: $loginFinalUrl (HTTP $loginStatusCode)\n" +
                "CSRF token used: $csrfToken\n" +
                "Cookies on $domain: ${cookieStore[domain]?.keys}\n" +
                "HTML:\n$loginResponseHtml"
            )
        val formAction = consentForm.attr("abs:action").takeIf { it.isNotEmpty() }
            ?: throw RuntimeException("Consent form action attribute missing. HTML:\n${consentForm.outerHtml()}")
        val formBodyBuilder = FormBody.Builder()
        consentForm.select("input[name], select[name], textarea[name]").forEach { el ->
            formBodyBuilder.add(el.attr("name"), el.`val`())
        }
        val consentResponse = httpClient.newCall(
            Request.Builder().url(formAction).post(formBodyBuilder.build()).build()
        ).execute()
        consentResponse.body?.close()

        val finalUrl = consentResponse.request.url.toString()
        return finalUrl.toHttpUrlOrNull()?.queryParameter("code")
            ?: throw RuntimeException("/login/oauth2/auth failed! Final URL: $finalUrl")
    }
}
