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
import android.net.Uri
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.DownloadState
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.saveFile
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.Date

class FileSync(
    private val context: Context,
    private val fileDownloadApi: FileDownloadAPI,
    private val localFileDao: LocalFileDao,
    private val fileFolderDao: FileFolderDao,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val fileSyncProgressDao: FileSyncProgressDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
) {

    var isStopped = false

    suspend fun syncFiles(syncSettings: CourseSyncSettingsEntity) {
        val courseId = syncSettings.courseId
        val allFiles = getAllFiles(courseId)
        val allFileIds = allFiles.map { it.id }

        cleanupSyncedFiles(courseId, allFileIds)

        val fileFolders = fileFolderDao.findFilesToSync(courseId, syncSettings.fullFileSync)
            .map { it.toApiModel() }

        val syncData = mutableListOf<FileSyncData>()

        fileFolders.forEach {
            val progressId = createAndInsertProgress(it, courseId, false)
            syncData.add(FileSyncData(progressId, it.id, it.displayName.orEmpty(), it.url.orEmpty(), courseId, false))
        }

        courseSyncProgressDao.findByCourseId(courseId)?.copy(progressState = ProgressState.IN_PROGRESS)?.let {
            courseSyncProgressDao.update(it)
        }

        val chunks = syncData.chunked(6)

        coroutineScope {
            chunks.forEach {
                if (isStopped) return@coroutineScope
                it.map {
                    async { downloadFile(it) }
                }.awaitAll()
            }
        }
    }

    suspend fun syncAdditionalFiles(
        syncSettings: CourseSyncSettingsEntity,
        additionalFileIdsToSync: Set<Long>,
        externalFilesToSync: Set<String>
    ) {
        val courseId = syncSettings.courseId

        val localIds = localFileDao.findByCourseId(courseId).map { it.id }.toSet()
        val internalFileIdsToSync = additionalFileIdsToSync.filterNot { localIds.contains(it) }.toSet()

        val additionalPublicFilesToSync = fileFolderDao.findByIds(internalFileIdsToSync).map { it.toApiModel() }

        val nonPublicFileIds = internalFileIdsToSync.minus(additionalPublicFilesToSync.map { it.id }.toSet())
        val nonPublicFiles = nonPublicFileIds.map {
            fileFolderApi.getCourseFile(courseId, it, RestParams(isForceReadFromNetwork = false, shouldLoginOnTokenError = false)).dataOrNull
        }.filterNotNull()

        fileFolderDao.insertAll(nonPublicFiles.map { FileFolderEntity(it.copy(isHidden = true)) })

        val additionalFiles = (additionalPublicFilesToSync + nonPublicFiles).filter { !it.url.isNullOrEmpty() }

        val syncData = mutableListOf<FileSyncData>()

        additionalFiles.forEach {
            val progressId = createAndInsertProgress(it, courseId, true)
            syncData.add(FileSyncData(progressId, it.id, it.displayName.orEmpty(), it.url.orEmpty(), courseId, false))
        }

        externalFilesToSync.forEach {
            val progressId = createAndInsertExternalProgress(it, courseId)
            syncData.add(FileSyncData(progressId, -1, Uri.parse(it).lastPathSegment.orEmpty(), it, courseId, true))
        }

        courseSyncProgressDao.findByCourseId(courseId)?.copy(additionalFilesStarted = true)
            ?.let {
                courseSyncProgressDao.update(it)
            }

        val chunks = syncData.chunked(6)

        chunks.forEach {
            coroutineScope {
                it.map {
                    async { downloadFile(it) }
                }.awaitAll()
            }
        }
    }

    private suspend fun downloadFile(fileSyncData: FileSyncData) {
        val fileName = when {
            fileSyncData.inputFileName.isNotEmpty() && !fileSyncData.externalFile -> "${fileSyncData.fileId}_${fileSyncData.inputFileName}"
            fileSyncData.inputFileName.isNotEmpty() -> fileSyncData.inputFileName
            else -> fileSyncData.fileId.toString()
        }

        var downloadedFile = getDownloadFile(fileName, fileSyncData.externalFile, fileSyncData.courseId)

        try {
            val downloadResult = fileDownloadApi.downloadFile(
                fileSyncData.fileUrl,
                RestParams(shouldIgnoreToken = fileSyncData.externalFile, shouldLoginOnTokenError = false)
            )

            // External images can fail for various reasons (for example the file is no longer available),
            // so we just mark them as completed in this case. The user wouldn't see those in online mode anyway.
            if (fileSyncData.externalFile && downloadResult is DataResult.Fail) {
                updateProgress(fileSyncData.progressId, 100, ProgressState.COMPLETED, fileSyncData.externalFile)
                return
            }

            downloadResult
                .dataOrThrow
                .saveFile(downloadedFile)
                .collect {
                    if (isStopped) throw IllegalStateException("Worker was stopped")
                    when (it) {
                        is DownloadState.InProgress -> {
                            updateProgress(fileSyncData.progressId, it.progress, ProgressState.IN_PROGRESS, fileSyncData.externalFile, it.totalBytes)
                        }

                        is DownloadState.Success -> {
                            if (downloadedFile.name.startsWith("temp_")) {
                                downloadedFile = rewriteOriginalFile(
                                    downloadedFile,
                                    fileName,
                                    fileSyncData.externalFile,
                                    fileSyncData.courseId
                                )
                            }
                            if (!fileSyncData.externalFile) {
                                localFileDao.insert(
                                    LocalFileEntity(
                                        fileSyncData.fileId,
                                        fileSyncData.courseId,
                                        Date(),
                                        downloadedFile.absolutePath
                                    )
                                )
                            }
                            updateProgress(fileSyncData.progressId, 100, ProgressState.COMPLETED, fileSyncData.externalFile, it.totalBytes)
                        }

                        is DownloadState.Failure -> {
                            throw it.throwable
                        }
                    }
                }
        } catch (e: Exception) {
            downloadedFile.delete()
            updateProgress(fileSyncData.progressId, 0, ProgressState.ERROR, fileSyncData.externalFile)
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

    private suspend fun getAllFiles(courseId: Long): List<FileFolderEntity> {
        return fileFolderDao.findAllFilesByCourseId(courseId)
    }

    private suspend fun createAndInsertProgress(fileFolder: FileFolder, courseId: Long, additionalFile: Boolean): Long {
        val progress = FileSyncProgressEntity(
            fileFolder.id,
            courseId,
            fileFolder.displayName.orEmpty(),
            0,
            fileFolder.size,
            additionalFile,
            ProgressState.IN_PROGRESS
        )

        val rowId = fileSyncProgressDao.insert(progress)
        return fileSyncProgressDao.findByRowId(rowId)?.id ?: -1L
    }

    private suspend fun createAndInsertExternalProgress(url: String, courseId: Long): Long {
        val progress = FileSyncProgressEntity(
            0,
            courseId,
            Uri.parse(url).lastPathSegment.orEmpty(),
            0,
            0,
            true,
            ProgressState.IN_PROGRESS
        )
        val rowId = fileSyncProgressDao.insert(progress)
        return fileSyncProgressDao.findByRowId(rowId)?.id ?: -1L
    }

    private suspend fun updateProgress(progressId: Long, progress: Int, progressState: ProgressState, externalFile: Boolean, size: Long? = null) {
        var newProgress = fileSyncProgressDao.findById(progressId)?.copy(progress = progress, progressState = progressState)
        if (externalFile && size != null) {
            newProgress = newProgress?.copy(fileSize = size)
        }

        newProgress?.let { fileSyncProgressDao.update(it) }
    }
}

data class FileSyncData(
    val progressId: Long,
    val fileId: Long,
    val inputFileName: String,
    val fileUrl: String,
    val courseId: Long,
    val externalFile: Boolean = false
)