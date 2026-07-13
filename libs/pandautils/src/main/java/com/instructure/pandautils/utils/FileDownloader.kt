/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.CookieManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class FileDownloader(
    private val context: Context,
    private val cookieManager: CookieManager,
    private val downloadNotificationHelper: DownloadNotificationHelper,
    private val client: OkHttpClient = CanvasRestAdapter.okHttpClient,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    fun downloadFileToDevice(attachment: Attachment) {
        downloadFileToDevice(attachment.url, attachment.filename, attachment.contentType)
    }

    fun downloadFileToDevice(
        downloadURL: String?,
        filename: String?,
        contentType: String?
    ) {
        if (downloadURL == null) {
            Toast.makeText(
                context,
                context.getString(R.string.errorOccurred),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        // DownloadManager forwards request headers across redirects and can't strip them on a
        // cross-host hop, so we can't safely hand it a Canvas URL + Bearer token (the token would
        // leak to the inst-fs/S3 storage host). Instead resolve the redirect ourselves with the
        // authenticated OkHttp client (which strips the token on cross-host redirects) and enqueue
        // the resulting self-authorizing storage URL without any token.
        coroutineScope.launch {
            val resolved = resolveDownloadUrl(downloadURL)
            withContext(Dispatchers.Main) {
                enqueue(resolved, filename, contentType)
            }
        }
    }

    private fun enqueue(resolved: ResolvedDownload, filename: String?, contentType: String?) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val downloadUri = Uri.parse(resolved.url)
        val cookie = cookieManager.getCookie(resolved.url)

        val request = DownloadManager.Request(downloadUri)
        request
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setTitle(filename)
            .setMimeType(contentType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        } else {
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename)
        }

        if (cookie?.isNotEmpty().orDefault()) {
            request.addRequestHeader("Cookie", cookie)
        }

        // Only the Canvas host itself is served directly (no further redirect), so the token is
        // safe to attach there. Off-domain storage URLs are self-authorizing and get no token.
        if (resolved.needsToken) {
            val token = ApiPrefs.getValidToken()
            if (token.isNotEmpty()) {
                request.addRequestHeader("Authorization", "Bearer $token")
            }
        }

        val downloadId = downloadManager.enqueue(request)

        downloadNotificationHelper.monitorDownload(downloadId, filename)

        showNotificationPermissionToastIfNeeded()
    }

    /**
     * Follows redirects on the authenticated OkHttp client (which attaches the Bearer token to
     * Canvas requests and strips it on cross-host redirects) only while we stay on the Canvas host.
     * As soon as the redirect leaves the Canvas domain we stop and return that self-authorizing URL
     * so it can be downloaded without a token. If Canvas serves the file directly (no redirect), the
     * Canvas URL is returned with [ResolvedDownload.needsToken] set.
     */
    private fun resolveDownloadUrl(startUrl: String): ResolvedDownload {
        val canvasHost = Uri.parse(ApiPrefs.fullDomain).host
        return try {
            val noRedirectClient = client.newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .cache(null)
                .build()

            var url = startUrl
            var hops = 0
            while (hops < MAX_REDIRECTS) {
                if (Uri.parse(url).host != canvasHost) {
                    // Left the Canvas domain: the target authorizes itself, so no token.
                    return ResolvedDownload(url, needsToken = false)
                }
                val request = Request.Builder().url(url).header("Range", "bytes=0-0").build()
                val next = noRedirectClient.newCall(request).execute().use { response ->
                    if (response.isRedirect) {
                        response.header("Location")?.let { response.request.url.resolve(it)?.toString() }
                    } else {
                        null
                    }
                }
                // No redirect: Canvas is serving the file directly and needs the token.
                if (next == null) return ResolvedDownload(url, needsToken = true)
                url = next
                hops++
            }
            ResolvedDownload(url, needsToken = Uri.parse(url).host == canvasHost)
        } catch (e: Exception) {
            // Resolution failed; enqueue the original URL without the token so it can never leak.
            ResolvedDownload(startUrl, needsToken = false)
        }
    }

    private fun showNotificationPermissionToastIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Toast.makeText(
                    context,
                    context.getString(R.string.fileDownloadNotificationPermissionDenied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private data class ResolvedDownload(val url: String, val needsToken: Boolean)

    companion object {
        private const val MAX_REDIRECTS = 5
    }
}
