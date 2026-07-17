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
package com.instructure.pandautils.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.ApiPrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileDownloaderTest {

    private val context: Context = mockk(relaxed = true)
    private val cookieManager: CookieManager = mockk(relaxed = true)
    private val downloadNotificationHelper: DownloadNotificationHelper = mockk(relaxed = true)
    private val downloadManager: DownloadManager = mockk(relaxed = true)

    private val client: OkHttpClient = mockk()
    private val clientBuilder: OkHttpClient.Builder = mockk()
    private val noRedirectClient: OkHttpClient = mockk()
    private val call: Call = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var fileDownloader: FileDownloader

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ApiPrefs)
        mockkStatic(Uri::class)
        mockkConstructor(DownloadManager.Request::class)

        every { ApiPrefs.fullDomain } returns DOMAIN
        every { Uri.parse(DOMAIN) } returns uri(DOMAIN_HOST)
        every { context.getSystemService(DownloadManager::class.java) } returns downloadManager
        every { cookieManager.getCookie(any()) } returns null

        // Redirect-resolution client: return itself for every builder call, no-redirect client at the end.
        every { client.newBuilder() } returns clientBuilder
        every { clientBuilder.followRedirects(any()) } returns clientBuilder
        every { clientBuilder.followSslRedirects(any()) } returns clientBuilder
        every { clientBuilder.cache(any()) } returns clientBuilder
        every { clientBuilder.build() } returns noRedirectClient
        every { noRedirectClient.newCall(any()) } returns call

        // DownloadManager.Request methods are chained/self-returning; give them relaxed results.
        every { anyConstructed<DownloadManager.Request>().setNotificationVisibility(any()) } returns mockk(relaxed = true)
        every { anyConstructed<DownloadManager.Request>().setDestinationInExternalPublicDir(any(), any()) } returns mockk(relaxed = true)
        every { anyConstructed<DownloadManager.Request>().setDestinationInExternalFilesDir(any(), any(), any()) } returns mockk(relaxed = true)
        every { anyConstructed<DownloadManager.Request>().addRequestHeader(any(), any()) } returns mockk(relaxed = true)

        fileDownloader = FileDownloader(
            context,
            cookieManager,
            downloadNotificationHelper,
            client,
            CoroutineScope(testDispatcher)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Attaches token to the Canvas host when the file is served directly`() {
        // Canvas serves the file directly (no redirect) -> token is safe on this request.
        every { call.execute() } returns response(CANVAS_URL, code = 200)
        every { Uri.parse(CANVAS_URL) } returns uri(DOMAIN_HOST)
        every { ApiPrefs.getValidToken() } returns TOKEN

        fileDownloader.downloadFileToDevice(attachment(CANVAS_URL))

        verify { anyConstructed<DownloadManager.Request>().addRequestHeader("Authorization", "Bearer $TOKEN") }
    }

    @Test
    fun `Does not attach token to the off-domain URL a Canvas file redirects to`() {
        // Canvas redirects to a different (storage) host; DownloadManager must not receive the token.
        every { call.execute() } returns response(CANVAS_URL, code = 302, location = STORAGE_URL)
        every { Uri.parse(STORAGE_URL) } returns uri(STORAGE_HOST)
        every { ApiPrefs.getValidToken() } returns TOKEN

        fileDownloader.downloadFileToDevice(attachment(CANVAS_URL))

        // The self-authorizing storage URL is enqueued...
        verify { downloadManager.enqueue(any()) }
        // ...without our token.
        verify(exactly = 0) { anyConstructed<DownloadManager.Request>().addRequestHeader("Authorization", any()) }
    }

    @Test
    fun `Does not attach token and does not make a request for an off-domain URL`() {
        every { ApiPrefs.getValidToken() } returns TOKEN

        fileDownloader.downloadFileToDevice(attachment(EXTERNAL_URL, host = EXTERNAL_HOST))

        verify(exactly = 0) { call.execute() }
        verify(exactly = 0) { anyConstructed<DownloadManager.Request>().addRequestHeader("Authorization", any()) }
    }

    @Test
    fun `Does not attach an empty token even for a directly served Canvas file`() {
        every { call.execute() } returns response(CANVAS_URL, code = 200)
        every { Uri.parse(CANVAS_URL) } returns uri(DOMAIN_HOST)
        every { ApiPrefs.getValidToken() } returns ""

        fileDownloader.downloadFileToDevice(attachment(CANVAS_URL))

        verify(exactly = 0) { anyConstructed<DownloadManager.Request>().addRequestHeader("Authorization", any()) }
    }

    private fun attachment(url: String, host: String = DOMAIN_HOST): Attachment {
        every { Uri.parse(url) } returns uri(host)
        return Attachment(url = url, filename = FILE_NAME, contentType = "application/pdf")
    }

    private fun uri(host: String): Uri = mockk(relaxed = true) {
        every { this@mockk.host } returns host
    }

    private fun response(fromUrl: String, code: Int, location: String? = null): Response {
        val builder = Response.Builder()
            .request(Request.Builder().url(fromUrl).build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body("".toResponseBody(null))
        location?.let { builder.header("Location", it) }
        return builder.build()
    }

    companion object {
        private const val TOKEN = "test-token"
        private const val FILE_NAME = "file.pdf"
        private const val DOMAIN_HOST = "canvas.instructure.com"
        private const val DOMAIN = "https://$DOMAIN_HOST"
        private const val CANVAS_URL = "https://$DOMAIN_HOST/files/1/download"
        private const val STORAGE_HOST = "inst-fs-prod.inscloudgate.net"
        private const val STORAGE_URL = "https://$STORAGE_HOST/files/1/abc?token=jwt"
        private const val EXTERNAL_HOST = "third-party.example.com"
        private const val EXTERNAL_URL = "https://$EXTERNAL_HOST/file.pdf"
    }
}
