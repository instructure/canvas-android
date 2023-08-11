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
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class CourseFileWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val workManager: WorkManager,
    private val localFileDao: LocalFileDao,
    private val fileFolderDao: FileFolderDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val courseSyncSettingsDao: CourseSyncSettingsDao
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val courseId = inputData.getLong(INPUT_COURSE_ID, -1)

        val syncSettings = courseSyncSettingsDao.findById(courseId) ?: return Result.failure()

        val allFiles = getAllFiles(courseId)
        val allFileIds = allFiles.map { it.id }

        cleanupSyncedFiles(courseId, allFileIds)

        val fileWorkers = if (syncSettings.fullFileSync) {
            allFiles.map { FileDownloadObject(it.id, it.url.orEmpty(), it.displayName.orEmpty()) }
        } else {
            fileSyncSettingsDao.findByCourseId(courseId)
                .map { FileDownloadObject(it.id, it.url.orEmpty(), it.fileName.orEmpty()) }
        }.map { FileSyncWorker.createOneTimeWorkRequest(courseId, it.id, it.name, it.url) }.chunked(6)

        if (fileWorkers.isEmpty()) return Result.success()

        var continuation = workManager
            .beginWith(fileWorkers.first())

        fileWorkers.drop(1).forEach {
            continuation = continuation.then(it)
        }

        continuation.enqueue()

        return Result.success()
    }

    private suspend fun cleanupSyncedFiles(courseId: Long, remoteIds: List<Long>) {
        val syncedIds = fileSyncSettingsDao.findByCourseId(courseId).map { it.id }
        val localRemovedFiles = localFileDao.findRemovedFiles(courseId, syncedIds)
        val remoteRemovedFiles = localFileDao.findRemovedFiles(courseId, remoteIds)

        (localRemovedFiles + remoteRemovedFiles).forEach {
            File(it.path).delete()
            localFileDao.delete(it)
        }

        fileSyncSettingsDao.deleteAllExcept(courseId, remoteIds)
    }

    private suspend fun getAllFiles(courseId: Long): List<FileFolderEntity> {
        return fileFolderDao.findAllFilesByCourseId(courseId)
    }

    companion object {
        const val INPUT_COURSE_ID = "courseId"

        fun createOneTimeWorkRequest(courseId: Long) =
            OneTimeWorkRequest.Builder(CourseFileWorker::class.java)
                .setInputData(workDataOf(INPUT_COURSE_ID to courseId))
                .build()
    }
}

data class FileDownloadObject(
    val id: Long,
    val url: String,
    val name: String
)