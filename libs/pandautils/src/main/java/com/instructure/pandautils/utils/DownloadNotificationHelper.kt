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

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import androidx.core.app.NotificationCompat
import com.instructure.pandautils.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

private const val CHANNEL_ID = "file_downloads"
private const val COMPLETION_OFFSET = 1000000

class DownloadNotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val activeDownloads = mutableMapOf<Long, Job>()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return

        val name = context.getString(R.string.notificationChannelNameFileDownloadsName)
        val description = context.getString(R.string.notificationChannelNameFileDownloadsDescription)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        channel.enableVibration(false)
        channel.setSound(null, null)

        notificationManager.createNotificationChannel(channel)
    }

    fun monitorDownload(downloadId: Long, fileName: String?) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                monitorDownloadProgress(downloadId, fileName ?: context.getString(R.string.downloadingFile))
            } catch (e: Exception) {
                showFailureNotification(downloadId, fileName)
            } finally {
                activeDownloads.remove(downloadId)
            }
        }
        activeDownloads[downloadId] = job
    }

    private suspend fun monitorDownloadProgress(downloadId: Long, fileName: String) {
        val notificationId = downloadId.toInt()
        var lastProgress = -1
        var isFirstNotification = true

        while (coroutineContext.isActive) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor? = downloadManager.query(query)

            cursor?.use {
                if (it.moveToFirst()) {
                    val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val bytesDownloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    when (status) {
                        DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING -> {
                            val progress = if (bytesTotal > 0) {
                                ((bytesDownloaded * 100) / bytesTotal).toInt()
                            } else {
                                0
                            }

                            if (progress != lastProgress || isFirstNotification) {
                                showProgressNotification(notificationId, fileName, progress, isFirstNotification)
                                lastProgress = progress
                                isFirstNotification = false
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            cancelProgressNotification(notificationId)
                            showSuccessNotification(downloadId, fileName)
                            return
                        }

                        DownloadManager.STATUS_FAILED -> {
                            cancelProgressNotification(notificationId)
                            showFailureNotification(downloadId, fileName)
                            return
                        }
                    }
                } else {
                    return
                }
            }

            delay(500)
        }
    }

    private fun showProgressNotification(notificationId: Int, fileName: String, progress: Int, isHeadsUp: Boolean) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.downloading))
            .setContentText(fileName)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(if (isHeadsUp) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showSuccessNotification(downloadId: Long, fileName: String?) {
        val notificationId = (downloadId.toInt() + COMPLETION_OFFSET)

        val uri = downloadManager.getUriForDownloadedFile(downloadId)
        val mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId)

        val viewDownloadIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            viewDownloadIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.downloadComplete))
            .setContentText(fileName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showFailureNotification(downloadId: Long, fileName: String?) {
        val notificationId = (downloadId.toInt() + COMPLETION_OFFSET)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(context.getString(R.string.downloadFailed))
            .setContentText(fileName)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun cancelProgressNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}