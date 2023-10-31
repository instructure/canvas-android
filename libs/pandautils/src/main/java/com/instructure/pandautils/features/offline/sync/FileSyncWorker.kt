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
import androidx.work.WorkContinuation
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.utils.toJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.lang.IllegalStateException
import java.util.Date

@HiltWorker
class FileSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val fileDownloadApi: FileDownloadAPI,
    private val localFileDao: LocalFileDao,
    private val fileSyncProgressDao: FileSyncProgressDao,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val courseFileSharedRepository: CourseFileSharedRepository,
    private val fileFolderDao: FileFolderDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val courseSyncSettingsDao: CourseSyncSettingsDao
) : CoroutineWorker(context, workerParameters) {

    private var fileExists = false

    private lateinit var progress: FileSyncProgressEntity

    override suspend fun doWork(): Result {

        val courseId = workerParameters.inputData.getLong(INPUT_COURSE_ID, -1)
        if (courseId == -1L) return Result.failure()

        fetchFiles(courseId)

        val courseSettingsWithFiles =
            courseSyncSettingsDao.findWithFilesById(courseId) ?: return Result.failure()

        syncFiles(courseSettingsWithFiles.courseSyncSettings)

        return Result.success()
    }

    private suspend fun fetchFiles(courseId: Long) {
        val fileFolders = courseFileSharedRepository.getCourseFoldersAndFiles(courseId)

        val entities = fileFolders.map { FileFolderEntity(it) }
        fileFolderDao.replaceAll(entities, courseId)
    }

    private suspend fun syncFiles(syncSettings: CourseSyncSettingsEntity): WorkContinuation? {
        val courseId = syncSettings.courseId
        val allFiles = fileFolderDao.findAllFilesByCourseId(courseId)
        val allFileIds = allFiles.map { it.id }

        cleanupSyncedFiles(courseId, allFileIds)

        val fileSyncEntities = mutableListOf<FileSyncProgressEntity>()
        val filesToSync = fileFolderDao.findFilesToSync(courseId, syncSettings.fullFileSync)
            .chunked(6)

        if (filesToSync.isEmpty()) {
            return null
        }

        coroutineScope {
            filesToSync.forEach {
                it.map {
                    async { downloadFile(it.id, it.displayName.orEmpty(), it.filesUrl.orEmpty(), courseId) }
                }.awaitAll()
            }
        }

        return null
    }

    private suspend fun cleanupSyncedFiles(courseId: Long, remoteIds: List<Long>) {
        val syncedIds = fileSyncSettingsDao.findByCourseId(courseId).map { it.id }
        val localRemovedFiles = localFileDao.findRemovedFiles(courseId, syncedIds)
        val remoteRemovedFiles = localFileDao.findRemovedFiles(courseId, remoteIds)

        (localRemovedFiles + remoteRemovedFiles).forEach {
            File(it.path).delete()
            localFileDao.delete(it)
        }

        val file = File(context.filesDir, "${ApiPrefs.user?.id.toString()}/external_$courseId")
        file.listFiles()?.forEach {
            it.delete()
        }

        fileSyncSettingsDao.deleteAllExcept(courseId, remoteIds)
    }

    private suspend fun downloadFile(fileId: Long, fileName: String, fileUrl: String, courseId: Long) {
        val inputFileName = inputData.getString(INPUT_FILE_NAME) ?: ""
        val fileName = when {
            inputFileName.isNotEmpty() && fileId != -1L -> "${fileId}_$inputFileName"
            inputFileName.isNotEmpty() -> inputFileName
            else -> fileId.toString()
        }
        val fileUrl = inputData.getString(INPUT_FILE_URL) ?: ""
        val courseId = inputData.getLong(INPUT_COURSE_ID, -1)

        val externalFile = fileId == -1L

        var downloadedFile = getDownloadFile(fileName, externalFile, courseId)


        try {
            fileDownloadApi.downloadFile(fileUrl, RestParams(shouldIgnoreToken = externalFile))
                .dataOrThrow
                .saveFile(downloadedFile)
                .collect {
                    if (isStopped) throw IllegalStateException("Worker was stopped")
                    when (it) {
                        is DownloadState.InProgress -> {
                            progress = progress.copy(progress = it.progress, progressState = ProgressState.IN_PROGRESS)
                            fileSyncProgressDao.update(progress)
                        }

                        is DownloadState.Success -> {
                            if (fileExists) {
                                downloadedFile = rewriteOriginalFile(downloadedFile, fileName, externalFile, courseId)
                            }
                            if (!externalFile) {
                                localFileDao.insert(
                                    LocalFileEntity(
                                        fileId,
                                        courseId,
                                        Date(),
                                        downloadedFile.absolutePath
                                    )
                                )
                            }
                            progress = progress.copy(
                                progress = 100,
                                progressState = ProgressState.COMPLETED,
                                fileSize = it.totalBytes
                            )
                            fileSyncProgressDao.update(progress)
                        }

                        is DownloadState.Failure -> {
                            throw it.throwable
                        }
                    }
                }
        } catch (e: Exception) {
            downloadedFile.delete()
            progress = progress.copy(progressState = ProgressState.ERROR)
            fileSyncProgressDao.update(progress)
            firebaseCrashlytics.recordException(e)
        }
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
        const val TAG = "FileSyncWorker"

        fun createOneTimeWorkRequest(
            courseId: Long,
            fileId: Long,
            fileName: String,
            fileUrl: String,
            wifiOnly: Boolean
        ): OneTimeWorkRequest {
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