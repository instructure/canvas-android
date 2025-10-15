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

package com.instructure.pandautils.unit

import android.net.Uri
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.RouteUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RouteUtilsTest : Assert() {

    lateinit var route: Route
    lateinit var user: User

    private val requestUri: Uri = mockk(relaxed = true)
    private val responseUri: Uri = mockk(relaxed = true)
    private val dashUri: Uri = mockk(relaxed = true)
    private val requestUrl = "https://domain.com/mediaUrl"
    private val responseUrl = "https://domain.com/file"
    private val dashUrl = "https://domain.com/file.mpd"
    private val okHttpClient: OkHttpClient = mockk(relaxed = true)
    private val call = mockk<okhttp3.Call>()
    private val response = mockk<Response>()
    private val request = mockk<Request>()
    private val httpUrl = mockk<HttpUrl>()

    @Before
    fun setup() {
        user = User()
        route = Route()

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user
        every { ApiPrefs.fullDomain } returns "https://domain.instructure.com"

        mockkStatic(Uri::class)
        every { requestUri.toString() } returns requestUrl
        every { responseUri.toString() } returns responseUrl
        every { dashUri.toString() } returns dashUrl

        every { Uri.parse(requestUrl) } returns requestUri
        every { Uri.parse(responseUrl) } returns responseUri
        every { Uri.parse(dashUrl) } returns dashUri

        every { okHttpClient.newCall(any()) } returns call
        every { okHttpClient.newBuilder().followRedirects(true)
            .cache(null).build() } returns okHttpClient
        every { httpUrl.toString() } returns responseUrl
        every { request.url } returns httpUrl
        every { response.request } returns request
        every { response.header("content-type") } returns "application/dash+xml"
        every { response.close() } just Runs
        coEvery { call.execute() } returns response



        mockkObject(com.instructure.canvasapi2.CanvasRestAdapter)
        every { com.instructure.canvasapi2.CanvasRestAdapter.okHttpClient } returns okHttpClient
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a file id`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/files/$fileId/preview", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a course and a file id`() {
        val fileId = "6"
        val courseId = "12"
        route.paramsHash = hashMapOf(Pair(RouterParams.COURSE_ID, courseId))

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/courses/$courseId/files/$fileId/preview", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a verifier`() {
        val fileId = "6"
        val verifier = "thisisaverifier"
        route.queryParamsHash = hashMapOf(Pair(RouterParams.VERIFIER, verifier))

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/files/$fileId/preview?verifier=$verifier", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns that auth is NOT needed with a verifier`() {
        val fileId = "6"
        val verifier = "thisisaverifier"
        route.queryParamsHash = hashMapOf(Pair(RouterParams.VERIFIER, verifier))

        RouteUtils.retrieveFileUrl(route, fileId) { _, _, needsAuth ->
            assertFalse(needsAuth)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns that auth is needed without a verifier`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { _, _, needsAuth ->
            assertTrue(needsAuth)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a course context when a course id is provided`() {
        val fileId = "6"
        val courseId = "12"
        route.paramsHash = hashMapOf(Pair(RouterParams.COURSE_ID, courseId))

        RouteUtils.retrieveFileUrl(route, fileId) { _, context, _ ->
            assertEquals(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 12), context)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a user context when no course id is provided`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { _, context, _ ->
            assertEquals(CanvasContext.currentUserContext(user), context)
        }
    }

    @Test
    fun `getMediaUri returns proper dash url if content-type is dash`() = runBlocking {
        val result = RouteUtils.getMediaUri(requestUri)
        assertEquals(dashUri, result)
    }

    @Test
    fun `getMediaUri returns responseUri if if content-type is not dash`() = runBlocking {
        every { response.header("content-type") } returns "application/mp4"

        val result = RouteUtils.getMediaUri(responseUri)
        assertEquals(responseUri, result)
    }

    @Test
    fun `getMediaUri returns responseUri if if content-type is null`() = runBlocking {
        every { response.header("content-type") } returns null

        val result = RouteUtils.getMediaUri(responseUri)
        assertEquals(responseUri, result)
    }

    @Test
    fun `getMediaUri returns original uri on exception`() = runBlocking {
        coEvery { call.execute() } throws Exception("Network error")

        val result = RouteUtils.getMediaUri(requestUri)
        assertEquals(requestUri, result)
    }
}
