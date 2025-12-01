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
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.R

class FileDownloader(
    private val context: Context,
    private val cookieManager: CookieManager,
    private val downloadNotificationHelper: DownloadNotificationHelper
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
        downloadFileToDevice(Uri.parse(downloadURL), filename, contentType)
    }

    private fun downloadFileToDevice(
        downloadURI: Uri,
        filename: String?,
        contentType: String?
    ) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val cookie = cookieManager.getCookie(downloadURI.toString())

        val request = DownloadManager.Request(downloadURI)
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

        val downloadId = downloadManager.enqueue(request)

        downloadNotificationHelper.monitorDownload(downloadId, filename)

        showNotificationPermissionToastIfNeeded()
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
}