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
import android.os.Environment
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class FileSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val fileDownloadApi: FileDownloadAPI
) : CoroutineWorker(context, workerParameters) {

    private var fileExists = false

    override suspend fun doWork(): Result {
        val fileName = inputData.getString(INPUT_FILE_NAME) ?: ""
        val fileUrl = inputData.getString(INPUT_FILE_URL) ?: ""

        var result = Result.failure()

        val downloadedFile = getDownloadFile(fileName)

        fileDownloadApi.downloadFile(fileUrl)
            .saveFile(downloadedFile)
            .collect {
                when (it) {
                    is DownloadState.InProgress -> {

                    }

                    is DownloadState.Success -> {
                        if (fileExists) {
                            rewriteOriginalFile(downloadedFile, fileName)
                        }
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

    private fun rewriteOriginalFile(newFile: File, fileName: String) {
        val originalFile = File(context.filesDir, fileName)
        originalFile.delete()
        newFile.renameTo(originalFile)
    }

    companion object {
        const val INPUT_FILE_NAME = "INPUT_FILE_NAME"
        const val INPUT_FILE_URL = "INPUT_FILE_URL"

        fun createOneTimeWorkRequest(fileName: String, fileUrl: String): OneTimeWorkRequest {
            val inputData = androidx.work.Data.Builder()
                .putString(INPUT_FILE_NAME, fileName)
                .putString(INPUT_FILE_URL, fileUrl)
                .build()

            return OneTimeWorkRequest.Builder(FileSyncWorker::class.java)
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        }
    }
}