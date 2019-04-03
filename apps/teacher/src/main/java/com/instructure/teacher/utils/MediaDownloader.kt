/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.utils

import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.instructure.teacher.R


object MediaDownloader {
    @JvmStatic
    fun download(context: Context, url: String?, filenameForDownload: String, downloadDescription: String) {
        // Ensure the URL is valid
        if (url == null || url.isBlank()) {
            Toast.makeText(context, R.string.unexpectedErrorDownloadingFile, Toast.LENGTH_SHORT).show()
            return
        }

        // Set up the download request
        val request = Request(Uri.parse(url)).apply {
            setDescription(if (downloadDescription.isNotBlank()) downloadDescription else filenameForDownload)
            setTitle(filenameForDownload)
            allowScanningByMediaScanner()
            setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filenameForDownload)
        }

        // Enqueue request in DownloadManager
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }
}