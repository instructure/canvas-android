package com.instructure.pandautils.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.instructure.canvasapi2.models.Attachment

class FileDownloader(private val context: Context) {
    fun downloadFileToDevice(attachment: Attachment) {
        downloadFileToDevice(attachment.url, attachment.filename, attachment.contentType)
    }

    fun downloadFileToDevice(
        downloadURL: String?,
        filename: String?,
        contentType: String?
    ) {
        downloadFileToDevice(Uri.parse(downloadURL), filename, contentType)
    }

    fun downloadFileToDevice(
        downloadURI: Uri,
        filename: String?,
        contentType: String?
    ) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)

        val request = DownloadManager.Request(downloadURI)
        request
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(filename)
            .setMimeType(contentType)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$filename")

        downloadManager.enqueue(request)
    }
}