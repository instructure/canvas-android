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
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
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

    override suspend fun doWork(): Result {
        val fileId = inputData.getLong(INPUT_FILE_ID, -1)
        val fileName = "${fileId}_${inputData.getString(INPUT_FILE_NAME) ?: ""}"
        val fileUrl = inputData.getString(INPUT_FILE_URL) ?: ""
        val courseId = inputData.getLong(INPUT_COURSE_ID, -1)

        var result = Result.failure()

        var downloadedFile = getDownloadFile(fileName)

        fileDownloadApi.downloadFile(fileUrl)
            .saveFile(downloadedFile)
            .collect {
                when (it) {
                    is DownloadState.InProgress -> {

                    }

                    is DownloadState.Success -> {
                        if (fileExists) {
                            downloadedFile = rewriteOriginalFile(downloadedFile, fileName)
                        }
                        localFileDao.insert(LocalFileEntity(fileId, courseId, Date(), downloadedFile.absolutePath))
                        result = Result.success()
                    }

                    is DownloadState.Failure -> {
                        downloadedFile.delete()
                        result = Result.failure()
                    }
                }
            }


        return result
    }

    private fun getDownloadFile(fileName: String): File {
        var downloadFile = File(context.filesDir, fileName)
        if (downloadFile.exists()) {
            downloadFile = File(context.filesDir, "temp_${fileName}")
            fileExists = true
        }
        return downloadFile
    }

    private fun rewriteOriginalFile(newFile: File, fileName: String): File {
        val originalFile = File(context.filesDir, fileName)
        originalFile.delete()
        newFile.renameTo(originalFile)
        return originalFile
    }

    companion object {
        const val INPUT_FILE_ID = "INPUT_FILE_ID"
        const val INPUT_FILE_NAME = "INPUT_FILE_NAME"
        const val INPUT_FILE_URL = "INPUT_FILE_URL"
        const val INPUT_COURSE_ID = "INPUT_COURSE_ID"

        fun createOneTimeWorkRequest(courseId: Long, fileId: Long, fileName: String, fileUrl: String): OneTimeWorkRequest {
            val inputData = androidx.work.Data.Builder()
                .putString(INPUT_FILE_NAME, fileName)
                .putString(INPUT_FILE_URL, fileUrl)
                .putLong(INPUT_FILE_ID, fileId)
                .putLong(INPUT_COURSE_ID, courseId)
                .build()

            return OneTimeWorkRequest.Builder(FileSyncWorker::class.java)
                .setInputData(inputData)
                .build()
        }
    }
}