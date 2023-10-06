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

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.utils.toJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.util.Date

@HiltWorker
class FileSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val fileDownloadApi: FileDownloadAPI,
    private val localFileDao: LocalFileDao
) : CoroutineWorker(context, workerParameters) {

    private var fileExists = false

    private lateinit var progress: FileSyncProgress

    override suspend fun doWork(): Result {
        val fileId = inputData.getLong(INPUT_FILE_ID, -1)
        val inputFileName = inputData.getString(INPUT_FILE_NAME) ?: ""
        val fileName = when {
            inputFileName.isNotEmpty() && fileId != -1L -> "${fileId}_$inputFileName"
            inputFileName.isNotEmpty() -> inputFileName
            else -> fileId.toString()
        }
        val fileUrl = inputData.getString(INPUT_FILE_URL) ?: ""
        val courseId = inputData.getLong(INPUT_COURSE_ID, -1)

        var result = Result.failure()

        val externalFile = fileId == -1L

        var downloadedFile = getDownloadFile(fileName, externalFile, courseId)

        progress = FileSyncProgress(fileName, 0, externalFile = externalFile)
        setProgress(workDataOf(PROGRESS to progress.toJson()))

        fileDownloadApi.downloadFile(fileUrl, RestParams(shouldIgnoreToken = externalFile))
            .saveFile(downloadedFile)
            .collect {
                when (it) {
                    is DownloadState.InProgress -> {
                        progress = FileSyncProgress(fileName, it.progress, totalBytes = it.totalBytes, externalFile = externalFile)
                        setProgress(workDataOf(PROGRESS to progress.toJson()))
                    }

                    is DownloadState.Success -> {
                        if (fileExists) {
                            downloadedFile = rewriteOriginalFile(downloadedFile, fileName, externalFile, courseId)
                        }
                        if (!externalFile) {
                            localFileDao.insert(LocalFileEntity(fileId, courseId, Date(), downloadedFile.absolutePath))
                        }
                        progress = FileSyncProgress(fileName, 100, ProgressState.COMPLETED, totalBytes = it.totalBytes, externalFile = externalFile)
                        result = Result.success(workDataOf(OUTPUT to progress.toJson()))
                    }

                    is DownloadState.Failure -> {
                        downloadedFile.delete()
                        progress = FileSyncProgress(fileName, 100, ProgressState.ERROR, externalFile = externalFile)
                        result = Result.failure(workDataOf(OUTPUT to progress.toJson()))
                    }
                }
            }


        return result
    }

    private fun getDownloadFile(fileName: String, externalFile: Boolean, courseId: Long): File {
        var dir = File(context.filesDir, ApiPrefs.user?.id.toString())
        if (!dir.exists()) {
            dir.mkdir()
        }

        if (externalFile) {
            dir = File(dir, "external_$courseId")
            if (!dir.exists()) {
                dir.mkdir()
            }
        }

        var downloadFile = File(dir, fileName)
        if (downloadFile.exists()) {
            downloadFile = File(dir, "temp_${fileName}")
            fileExists = true
        }
        return downloadFile
    }

    private fun rewriteOriginalFile(newFile: File, fileName: String, externalFile: Boolean, courseId: Long): File {
        var dir = File(context.filesDir, ApiPrefs.user?.id.toString())
        if (externalFile) {
            dir = File(dir, "external_$courseId")
        }
        val originalFile = File(dir, fileName)
        originalFile.delete()
        newFile.renameTo(originalFile)
        return originalFile
    }

    companion object {
        const val INPUT_FILE_ID = "INPUT_FILE_ID"
        const val INPUT_FILE_NAME = "INPUT_FILE_NAME"
        const val INPUT_FILE_URL = "INPUT_FILE_URL"
        const val INPUT_COURSE_ID = "INPUT_COURSE_ID"
        const val PROGRESS = "fileSyncProgress"
        const val OUTPUT = "fileSyncOutput"
        const val TAG = "FileSyncWorker"

        fun createOneTimeWorkRequest(courseId: Long, fileId: Long, fileName: String, fileUrl: String, wifiOnly: Boolean): OneTimeWorkRequest {
            val inputData = androidx.work.Data.Builder()
                .putString(INPUT_FILE_NAME, fileName)
                .putString(INPUT_FILE_URL, fileUrl)
                .putLong(INPUT_FILE_ID, fileId)
                .putLong(INPUT_COURSE_ID, courseId)
                .build()

            return OneTimeWorkRequest.Builder(FileSyncWorker::class.java)
                .addTag(TAG)
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        }
    }
}