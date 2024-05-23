/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.file.download

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import kotlin.random.Random

@HiltWorker
class FileDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val fileDownloadApi: FileDownloadAPI
) : CoroutineWorker(context, workerParameters) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val fileName = inputData.getString(INPUT_FILE_NAME) ?: ""
    private val fileUrl = inputData.getString(INPUT_FILE_URL) ?: ""
    private val notificationId = Random.nextInt()

    private var foregroundInfo: ForegroundInfo = createForegroundInfo(notificationId, fileName, 0)

    override suspend fun doWork(): Result {
        registerNotificationChannel(context)

        val downloadFileName = createDownloadFileName(fileName)

        val downloadedFile =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadFileName)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            setForeground(foregroundInfo)
        }
        var result = Result.retry()

        try {
            fileDownloadApi.downloadFile(fileUrl, RestParams())
                .dataOrThrow
                .saveFile(downloadedFile)
                .collect { downloadState ->
                    when (downloadState) {
                        is DownloadState.InProgress -> {
                            foregroundInfo = createForegroundInfo(notificationId, fileName, downloadState.progress)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                setForeground(foregroundInfo)
                            } else {
                                updateForegroundNotification()
                            }
                        }

                        is DownloadState.Failure -> {
                            throw downloadState.throwable
                        }

                        is DownloadState.Success -> {
                            result = Result.success()
                            updateNotificationComplete(notificationId, fileName)
                        }
                    }
                }
        } catch (e: Exception) {
            result = Result.failure()
            updateNotificationFailed(notificationId, fileName)
        }


        return result
    }

    private fun createDownloadFileName(fileName: String): String {
        var downloadedFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val fileNameWithoutExtension = downloadedFile.nameWithoutExtension
        val fileExtension = downloadedFile.extension
        var counter = 1
        while (downloadedFile.exists()) {
            downloadedFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "$fileNameWithoutExtension($counter).$fileExtension"
            )
            counter++
        }

        return downloadedFile.name
    }

    private fun registerNotificationChannel(context: Context) {
        if (notificationManager.notificationChannels.any { it.id == CHANNEL_ID }) return

        val name = context.getString(R.string.notificationChannelNameFileUploadsName)
        val description = context.getString(R.string.notificationChannelNameFileUploadsDescription)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        notificationManager.createNotificationChannel(channel)
    }

    private fun createForegroundInfo(notificationId: Int, fileName: String, progress: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, FileUploadWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.downloadingFile))
            .setContentText(fileName)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .setOngoing(progress != 100)
            .build()

        return ForegroundInfo(notificationId, notification)
    }

    private fun updateNotificationComplete(notificationId: Int, fileName: String) {
        val viewDownloadIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
        val pendingIntent = PendingIntent.getActivity(context, 0, viewDownloadIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, FileUploadWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.downloadSuccessful))
            .setContentText(fileName)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notificationId + 1, notification)
    }

    private fun updateNotificationFailed(notificationId: Int, fileName: String) {
        val notification = NotificationCompat.Builder(applicationContext, FileUploadWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_canvas_logo)
            .setContentTitle(context.getString(R.string.downloadFailed))
            .setContentText(fileName)
            .build()
        notificationManager.notify(notificationId + 1, notification)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return foregroundInfo
    }

    private fun updateForegroundNotification() {
        notificationManager.notify(notificationId, foregroundInfo.notification)
    }

    companion object {
        const val INPUT_FILE_NAME = "fileName"
        const val INPUT_FILE_URL = "fileUrl"

        const val CHANNEL_ID = "uploadChannel"

        fun createOneTimeWorkRequest(fileName: String, fileUrl: String): OneTimeWorkRequest {
            val inputData = androidx.work.Data.Builder()
                .putString(INPUT_FILE_NAME, fileName)
                .putString(INPUT_FILE_URL, fileUrl)
                .build()

            return OneTimeWorkRequest.Builder(FileDownloadWorker::class.java)
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }
    }
}