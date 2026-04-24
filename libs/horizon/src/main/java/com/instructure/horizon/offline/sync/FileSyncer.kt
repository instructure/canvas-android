/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.offline.sync

import com.instructure.horizon.data.datasource.CourseFilesNetworkDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class FileSyncer @Inject constructor(
    private val fileSyncRepository: HorizonFileSyncRepository,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val courseFilesNetworkDataSource: CourseFilesNetworkDataSource,
) {
    suspend fun syncFiles(
        courseId: Long,
        selectedFileIds: List<Long>,
        additionalFileIds: Set<Long>,
        externalUrls: Set<String>,
        isStopped: () -> Boolean,
    ) {
        val allInternalFileIds = (selectedFileIds + additionalFileIds).distinct()

        for (fileId in additionalFileIds) {
            if (fileSyncPlanDao.findByCourseId(courseId).none { it.fileId == fileId }) {
                val fileInfo = courseFilesNetworkDataSource.getFileInfo(courseId, fileId)
                fileSyncPlanDao.upsert(
                    HorizonFileSyncPlanEntity(
                        fileId = fileId,
                        courseId = courseId,
                        fileName = fileInfo?.displayName ?: "file_$fileId",
                        fileSize = fileInfo?.size ?: 0,
                        state = HorizonProgressState.PENDING,
                        isAdditionalFile = true,
                    )
                )
            }
        }

        allInternalFileIds.chunked(6).forEach { chunk ->
            if (isStopped()) return
            coroutineScope {
                chunk.map { fileId ->
                    async {
                        try {
                            fileSyncPlanDao.updateProgress(fileId, 0, HorizonProgressState.IN_PROGRESS)
                            fileSyncRepository.downloadFile(fileId, courseId)
                            fileSyncPlanDao.updateProgress(fileId, 100, HorizonProgressState.COMPLETED)
                        } catch (_: Exception) {
                            fileSyncPlanDao.updateProgress(fileId, 0, HorizonProgressState.ERROR)
                        }
                    }
                }.awaitAll()
            }
        }
    }
}
