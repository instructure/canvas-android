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
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
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

    private val progresses = mutableMapOf<Long, FileSyncProgressEntity>()

    var isStopped = false

    suspend fun syncFiles(syncSettings: CourseSyncSettingsEntity) {
        val courseId = syncSettings.courseId
        val allFiles = getAllFiles(courseId)
        val allFileIds = allFiles.map { it.id }

        cleanupSyncedFiles(courseId, allFileIds)

        val fileFolders = mutableListOf<FileFolder>()
        fileFolderDao.findFilesToSync(courseId, syncSettings.fullFileSync).forEach {
            val progress = FileSyncProgressEntity(it.id, courseId, it.displayName.orEmpty(), 0, it.size, false, ProgressState.IN_PROGRESS)
            fileSyncProgressDao.insert(progress)
            progresses[it.id] = progress
            fileFolders.add(it.toApiModel())
        }

        courseSyncProgressDao.findByCourseId(courseId)?.copy(progressState = ProgressState.IN_PROGRESS)?.let {
            courseSyncProgressDao.update(it)
        }

        val chunks = fileFolders.chunked(6)

        coroutineScope {
            chunks.forEach {
                if (isStopped) return@coroutineScope
                it.map {
                    async { downloadFile(it.id, it.displayName.orEmpty(), it.url.orEmpty(), courseId) }
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

        val additionalPublicFilesToSync = fileFolderDao.findByIds(additionalFileIdsToSync).map { it.toApiModel() }

        val nonPublicFileIds = additionalFileIdsToSync.minus(additionalPublicFilesToSync.map { it.id }.toSet())
        val nonPublicFiles = nonPublicFileIds.map {
            fileFolderApi.getCourseFile(courseId, it, RestParams(isForceReadFromNetwork = false)).dataOrNull
        }.filterNotNull()

        val chunks = (additionalPublicFilesToSync + nonPublicFiles).chunked(6)

        chunks.forEach {
            coroutineScope {
                it.map {
                    async { downloadFile(it.id, it.displayName.orEmpty(), it.url.orEmpty(), courseId) }
                }.awaitAll()
            }
        }

        externalFilesToSync.chunked(6).forEach {
            coroutineScope {
                it.map {
                    async { downloadFile(-1, Uri.parse(it).lastPathSegment.orEmpty(), it, courseId) }
                }.awaitAll()
            }
        }
    }

    private suspend fun downloadFile(fileId: Long, inputFileName: String, fileUrl: String, courseId: Long) {
        val fileName = when {
            inputFileName.isNotEmpty() && fileId != -1L -> "${fileId}_$inputFileName"
            inputFileName.isNotEmpty() -> inputFileName
            else -> fileId.toString()
        }

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
                            val newProgress = progresses.get(fileId)?.copy(progress = it.progress)
                            newProgress?.let {
                                fileSyncProgressDao.update(it)
                                progresses[fileId] = it
                            }
                        }

                        is DownloadState.Success -> {
                            if (downloadedFile.name.startsWith("temp_")) {
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
                            val newProgress = progresses.get(fileId)?.copy(progress = 100, progressState = ProgressState.COMPLETED)
                            newProgress?.let {
                                fileSyncProgressDao.update(it)
                                progresses[fileId] = it
                            }
                        }

                        is DownloadState.Failure -> {
                            throw it.throwable
                        }
                    }
                }
        } catch (e: Exception) {
            downloadedFile.delete()
            val newProgress = progresses.get(fileId)?.copy(progress = 0, progressState = ProgressState.ERROR)
            newProgress?.let {
                fileSyncProgressDao.update(it)
                progresses[fileId] = it
            }
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

    private fun createProgress(fileFolder: FileFolder, courseId: Long): FileSyncProgressEntity {
        return FileSyncProgressEntity(
            fileFolder.id,
            courseId,
            fileFolder.displayName.orEmpty(),
            0,
            fileFolder.size,
            false,
            ProgressState.IN_PROGRESS
        )
    }
}