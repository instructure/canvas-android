package com.instructure.pandautils.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.instructure.canvasapi2.models.Attachment

class FileDownloader(private val context: Context) {
    fun downloadFileToDevice(attachment: Attachment) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)

        val request = DownloadManager.Request(Uri.parse(attachment.url))
        request
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(attachment.filename)
            .setMimeType(attachment.contentType)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${attachment.filename}")

        downloadManager.enqueue(request)
    }
}