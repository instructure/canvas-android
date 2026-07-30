/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */

package com.instructure.dataseeding.api

import com.google.gson.JsonParser
import com.instructure.dataseeding.model.CreateGlobalAnnouncement
import com.instructure.dataseeding.model.CreateGlobalAnnouncementWrapper
import com.instructure.dataseeding.model.GlobalAnnouncementAccountApiModel
import com.instructure.dataseeding.model.GlobalAnnouncementApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.net.URLDecoder
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object GlobalAnnouncementsApi {
    interface GlobalAnnouncementsService {

        @POST("accounts/{accountId}/account_notifications")
        fun createGlobalAnnouncement(
            @Path("accountId") accountId: String,
            @Body body: CreateGlobalAnnouncementWrapper
        ): Call<GlobalAnnouncementApiModel>

        @GET("accounts/self")
        fun getRootAccount(): Call<GlobalAnnouncementAccountApiModel>
    }

    private val adminGlobalAnnouncementsService: GlobalAnnouncementsService by lazy {
        CanvasNetworkAdapter.adminRetrofit.create(GlobalAnnouncementsService::class.java)
    }

    private val rootAccountId: Long by lazy {
        adminGlobalAnnouncementsService.getRootAccount().execute().body()!!.id
    }

    private val cookieJar = InMemoryCookieJar()

    private val sessionClient: OkHttpClient by lazy {
        OkHttpClient.Builder().cookieJar(cookieJar).build()
    }

    /**
     * Creates a global (institutional / account-level) announcement on the given account. Requires the
     * admin token used by [CanvasNetworkAdapter.adminRetrofit]. Defaults to the admin's root account
     * ("self") and to an announcement that is active from one day ago until one day from now, so it shows
     * up immediately on the dashboard. [icon] is one of: warning, information, question, error, calendar.
     */
    fun createGlobalAnnouncement(
        subject: String,
        message: String,
        startAt: String = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ISO_INSTANT),
        endAt: String = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).format(DateTimeFormatter.ISO_INSTANT),
        icon: String = "information",
        accountId: String = "self"
    ): GlobalAnnouncementApiModel {
        return adminGlobalAnnouncementsService
            .createGlobalAnnouncement(
                accountId,
                CreateGlobalAnnouncementWrapper(
                    CreateGlobalAnnouncement(
                        subject = subject,
                        message = message,
                        startAt = startAt,
                        endAt = endAt,
                        icon = icon
                    )
                )
            )
            .execute()
            .body()!!
    }

    /**
     * Destroys a global announcement from the account entirely (account_notifications#destroy), so seeded
     * announcements don't accumulate on the shared account. Canvas exposes no API for this - only the admin
     * web UI can - so this mirrors what the UI does: it exchanges the admin access token for a web session
     * via /login/session_token, then POSTs the `_method=delete` form to the (non-API) destroy route with the
     * session's CSRF token. This is different from the in-app dismiss, which only closes the announcement for
     * a single user. Throws if the server does not confirm the deletion.
     */
    fun deleteGlobalAnnouncement(announcementId: Long, accountId: Long = rootAccountId) {
        val root = "https://${CanvasNetworkAdapter.canvasDomain}"

        // 1. Exchange the admin access token for a one-time web-session URL.
        val sessionUrl = sessionClient.newCall(
            Request.Builder()
                .url("$root/login/session_token")
                .header("Authorization", "Bearer ${CanvasNetworkAdapter.adminToken}")
                .build()
        ).execute().use { response ->
            check(response.isSuccessful) { "Failed to obtain a web session token: HTTP ${response.code}" }
            val body = response.body?.string().orEmpty()
            JsonParser.parseString(body).asJsonObject.get("session_url")?.asString
                ?: error("No session_url returned when obtaining a web session")
        }

        // 2. Consume the session URL so the client picks up a logged-in browser session cookie.
        sessionClient.newCall(Request.Builder().url(sessionUrl).build()).execute().close()

        // 3. Load an authenticated HTML page to obtain the CSRF cookie.
        sessionClient.newCall(Request.Builder().url("$root/accounts/$accountId/settings").build()).execute().close()
        val csrfToken = cookieJar.value(CanvasNetworkAdapter.canvasDomain, "_csrf_token")
            ?.let { URLDecoder.decode(it, "UTF-8") }
            ?: error("No CSRF token cookie after establishing a web session")

        // 4. Destroy via the session-authenticated, non-API destroy action. On success Canvas redirects (302)
        // back to the settings page; a redirect to /login would mean the session was not established.
        val destroyClient = sessionClient.newBuilder().followRedirects(false).build()
        destroyClient.newCall(
            Request.Builder()
                .url("$root/accounts/$accountId/account_notifications/$announcementId")
                .header("X-CSRF-Token", csrfToken)
                .post(FormBody.Builder().add("_method", "delete").build())
                .build()
        ).execute().use { response ->
            val location = response.header("Location").orEmpty()
            check(response.code in 300..399 && !location.contains("/login")) {
                "Failed to delete global announcement $announcementId on account $accountId: HTTP ${response.code}, location=$location"
            }
        }
    }

    private class InMemoryCookieJar : CookieJar {
        private val cookiesByHost = mutableMapOf<String, MutableList<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val stored = cookiesByHost.getOrPut(url.host) { mutableListOf() }
            cookies.forEach { cookie ->
                stored.removeAll { it.name == cookie.name }
                stored.add(cookie)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookiesByHost[url.host].orEmpty()

        fun value(host: String, name: String): String? =
            cookiesByHost[host]?.firstOrNull { it.name == name }?.value
    }
}
