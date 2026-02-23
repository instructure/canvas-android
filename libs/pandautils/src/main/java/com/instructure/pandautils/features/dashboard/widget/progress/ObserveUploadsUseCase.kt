/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import com.instructure.pandautils.utils.orDefault
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject

class ObserveUploadsUseCase @Inject constructor(
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val workManager: WorkManager,
    private val apiPrefs: ApiPrefs
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<UploadProgressItem>> {
        return dashboardFileUploadDao.getAllForUserAsFlow(apiPrefs.user?.id.orDefault())
            .flatMapLatest { entities ->
                if (entities.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val validEntities = entities.mapNotNull { entity ->
                        try {
                            UUID.fromString(entity.workerId) to entity
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                    if (validEntities.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val workInfoFlows = validEntities.map { (workerId, entity) ->
                            workManager.getWorkInfoByIdFlow(workerId)
                                .combine(flowOf(workerId to entity)) { workInfo, pair -> Triple(workInfo, pair.first, pair.second) }
                        }
                        combine(workInfoFlows) { triples ->
                            triples.mapNotNull { (workInfo, workerId, entity) ->
                                workInfo?.let {
                                    createUploadProgressItem(entity, it, workerId)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun createUploadProgressItem(
        entity: DashboardFileUploadEntity,
        workInfo: WorkInfo,
        workerId: UUID
    ): UploadProgressItem {
        val (icon, background, state) = when (workInfo.state) {
            WorkInfo.State.FAILED -> Triple(
                R.drawable.ic_exclamation_mark,
                R.color.backgroundDanger,
                UploadState.FAILED
            )
            WorkInfo.State.SUCCEEDED -> Triple(
                R.drawable.ic_check_white_24dp,
                R.color.backgroundSuccess,
                UploadState.SUCCEEDED
            )
            else -> Triple(
                R.drawable.ic_upload,
                R.color.backgroundInfo,
                UploadState.UPLOADING
            )
        }

        val uploadedSize = workInfo.progress.getLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
        val fullSize = workInfo.progress.getLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
        val progress = if (fullSize > 0) {
            ((uploadedSize.toDouble() / fullSize.toDouble()) * 100.0).toInt().coerceIn(0, 100)
        } else {
            0
        }

        return UploadProgressItem(
            workerId = workerId,
            title = entity.title.orEmpty(),
            subtitle = entity.subtitle.orEmpty(),
            progress = progress,
            state = state,
            icon = icon,
            iconBackground = background,
            courseId = entity.courseId,
            assignmentId = entity.assignmentId,
            attemptId = entity.attemptId,
            folderId = entity.folderId
        )
    }
}