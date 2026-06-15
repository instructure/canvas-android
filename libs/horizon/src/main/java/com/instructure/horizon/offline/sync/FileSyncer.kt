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
import com.instructure.horizon.domain.usecase.GetDeviceStorageUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import javax.inject.Inject

class FileSyncer @Inject constructor(
    private val fileSyncRepository: HorizonFileSyncRepository,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val courseFilesNetworkDataSource: CourseFilesNetworkDataSource,
    private val getDeviceStorageUseCase: GetDeviceStorageUseCase,
) {
    suspend fun syncFiles(
        courseId: Long,
        selectedFileIds: List<Long>,
        additionalFileIds: Set<Long>,
        externalUrls: Set<String>,
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

        val planById = fileSyncPlanDao.findByCourseId(courseId).associateBy { it.fileId }

        val pendingTotalBytes = allInternalFileIds.sumOf { planById[it]?.fileSize ?: 0L }
        if (pendingTotalBytes > 0 && !hasFreeSpaceFor(pendingTotalBytes)) {
            allInternalFileIds.forEach { fileId ->
                fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.ERROR)
            }
            return
        }

        allInternalFileIds.chunked(PARALLEL_DOWNLOAD_BATCH).forEach { chunk ->
            coroutineContext.ensureActive()
            coroutineScope {
                chunk.map { fileId ->
                    async {
                        try {
                            val expectedSize = planById[fileId]?.fileSize?.takeIf { it > 0 }
                            if (expectedSize != null && !hasFreeSpaceFor(expectedSize)) {
                                fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.ERROR)
                                return@async
                            }
                            fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.IN_PROGRESS)
                            fileSyncRepository.downloadFile(fileId, courseId, expectedSize) { percent ->
                                fileSyncPlanDao.updateProgress(fileId, courseId, percent, HorizonProgressState.IN_PROGRESS)
                            }
                            fileSyncPlanDao.updateProgress(fileId, courseId, 100, HorizonProgressState.COMPLETED)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            fileSyncPlanDao.updateProgress(fileId, courseId, 0, HorizonProgressState.ERROR)
                        }
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun hasFreeSpaceFor(fileSize: Long): Boolean {
        val freeBytes = getDeviceStorageUseCase().availableBytes
        val margin = fileSize / FREE_SPACE_SAFETY_MARGIN_DIVISOR
        return freeBytes > fileSize + margin
    }

    companion object {
        private const val PARALLEL_DOWNLOAD_BATCH = 6
        private const val FREE_SPACE_SAFETY_MARGIN_DIVISOR = 10L
    }
}
