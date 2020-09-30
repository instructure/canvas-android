/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.student.R
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.pandautils.services.FileUploadService.Companion.CHANNEL_ID
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.File


class FileDownloadJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val fileName = intent.extras?.getString(FILE_NAME) ?: ""
        val fileUrl = intent.extras?.getString(FILE_URL) ?: ""
        val fileSize = intent.extras?.getLong(FILE_SIZE) ?: 0L
        val notificationId = intent.extras?.getInt(NOTIFICATION_ID) ?: 0

        registerNotificationChannel(this)

        // Tell Android where to send the user if they click on the notification
        val viewDownloadIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        val pendingIntent = PendingIntent.getActivity(this, 0, viewDownloadIntent, 0)

        // Setup a notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.downloadingFile))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentText(fileName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification.build())

        val resultStatus = downloadFile(fileName, fileUrl) { downloaded ->
            // Only update our notification if we know the file size
            // If the file size is 0, we can't keep track of anything
            val percentage = when {
                fileSize == 0L || downloaded <= 0 -> 0F
                else -> ((downloaded.toFloat() / fileSize) * 100).coerceIn(0f..100f).toFloat()
            }

            notification.setProgress(100, percentage.toInt(), fileSize <= 0)
            notificationManager.notify(notificationId, notification.build())
        }

        when (resultStatus) {
            is DownloadFailed -> {
                // We'll want to know if download streams are failing to open
                FirebaseCrashlytics.getInstance().recordException(Throwable("The file stream failed to open when downloading a file"))
                notification.setContentText(getString(R.string.downloadFailed))
            }
            is BadFileUrl, is BadFileName -> notification.setContentText(getString(R.string.downloadFailed))
            is DownloadSuccess -> {
                notification
                        .setContentTitle(fileName)
                        .setContentText(getString(R.string.downloadSuccessful))
            }
        }

        notification
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setOngoing(false)

        notificationManager.notify(notificationId, notification.build())
    }

    private fun downloadFile(fileName: String, fileUrl: String, updateCallback: (Long) -> Unit): DownloadStatus {
        val debounce = 1000 // The time to delay sending up a notification update; Sending them too fast can cause the system to skip some updates and can cause janky UI
        // NOTE: The WRITE_EXTERNAL_STORAGE permission should have been checked by this point; This will fail if that permission is not granted
        Log.d(TAG, "downloadFile URL: $fileUrl")
        val downloadedFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        // Make sure we have a valid file url and name
        if (fileUrl.isBlank()) {
            return BadFileUrl()
        } else if (fileName.isBlank()) {
            // Set notification message error
            return BadFileName()
        }

        // Download the file
        try {
            val okHttp = OkHttpClient.Builder().build()
            val request = Request.Builder().url(fileUrl).build()
            val source = okHttp.newCall(request).execute().body()?.source() ?: return DownloadFailed()
            val sink = Okio.buffer(Okio.sink(downloadedFile))

            var startTime = System.currentTimeMillis()
            var downloaded = 0L
            var read: Long
            updateCallback(0)

            val bufferSize = 8L * 1024
            val sinkBuffer = sink.buffer()

            // Perform download.
            read = source.read(sinkBuffer, bufferSize)
            while (read != -1L) {
                downloaded += read
                sink.emit()
                // Debounce the notification
                if (System.currentTimeMillis() - startTime > debounce) {
                    // Update the notification
                    updateCallback(downloaded)
                    startTime = System.currentTimeMillis()
                }
                read = source.read(sinkBuffer, bufferSize)
            }

            // Cleanup
            sink.flush()
            sink.close()
            source.close()
            return DownloadSuccess()

        } catch (e: Exception) {
            downloadedFile.delete()
            return DownloadFailed()
        }
    }

    companion object {
        val TAG = "DownloadMedia"
        // Keys for Job Intent Extras
        val FILE_NAME = "filename"
        val FILE_URL = "url"
        val FILE_SIZE = "filesize"
        val CONTENT_TYPE = "contenttype"
        val NOTIFICATION_ID = "notificationid"
        val USE_HTTPURLCONNECTION = "usehttpurlconnection"

        // Notification ID is passed into the extras of the job, make sure to use that for any notification updates inside the job
        var notificationId = 1
            get() = ++field

        // Job ID must be unique to this Job class
        val JOB_ID = 1987

        private fun createJobIntent(fileName: String, fileUrl: String, fileSize: Long): Intent = Intent().apply {
            putExtras(Bundle().apply {
                putString(FILE_NAME, fileName)
                putString(FILE_URL, fileUrl)
                putLong(FILE_SIZE, fileSize)
                putInt(NOTIFICATION_ID, notificationId)
            })
        }

        @JvmOverloads
        fun scheduleDownloadJob(context: Context, item: FileFolder? = null, attachment: Attachment? = null) {
            val fileName = item?.displayName ?: attachment?.filename ?: ""
            val url = item?.url ?: attachment?.url ?: ""
            val fileSize = item?.size ?: attachment?.size ?: 0L

            scheduleDownloadJob(context, fileName, url, fileSize)
        }

        fun scheduleDownloadJob(context: Context, fileName: String, fileUrl: String, fileSize: Long = 0) {
            val intent = FileDownloadJobIntentService.createJobIntent(fileName, fileUrl, fileSize)
            enqueueWork(context, FileDownloadJobIntentService::class.java, JOB_ID, intent)
        }

        fun registerNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Prevents recreation of notification channel if it exists.
                if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return

                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                val name = context.getString(R.string.notificationChannelNameFileUploadsName)
                val description = context.getString(R.string.notificationChannelNameFileUploadsDescription)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.description = description

                // Register the channel with the system
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}

sealed class DownloadStatus
class BadFileUrl : DownloadStatus()
class BadFileName : DownloadStatus()
class DownloadSuccess : DownloadStatus()
class DownloadFailed : DownloadStatus()
