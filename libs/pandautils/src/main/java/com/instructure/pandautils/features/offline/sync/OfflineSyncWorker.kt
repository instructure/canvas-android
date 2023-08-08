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
 */

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import com.instructure.canvasapi2.apis.*
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

const val COURSE_IDS = "course-ids"

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val workManager: WorkManager,
    private val featureFlagProvider: FeatureFlagProvider,
    private val courseApi: CourseAPI.CoursesInterface,
    private val dashboardCardDao: DashboardCardDao,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.checkEnvironmentFeatureFlag(FEATURE_FLAG_OFFLINE)) return Result.success()

        val dashboardCards =
            courseApi.getDashboardCourses(RestParams(isForceReadFromNetwork = true)).dataOrNull.orEmpty()
        dashboardCardDao.updateEntities(dashboardCards.map { DashboardCardEntity(it) })

        val courseIds = inputData.getLongArray(COURSE_IDS)
        val courses = courseIds?.let {
            courseSyncSettingsDao.findByIds(courseIds.toList())
        } ?: courseSyncSettingsDao.findAll()

        val fileWorkers = courses.map {
            fileSyncSettingsDao.findByCourseId(it.courseId)
        }
            .flatten()
            .map {
                FileDownloadWorker.createOneTimeWorkRequest(it.fileName.orEmpty(), it.url.orEmpty())
            }

        val works = courses.filter { it.anySyncEnabled }
            .map {
                val inputData = workDataOf(CourseSyncWorker.COURSE_ID to it.courseId)
                OneTimeWorkRequestBuilder<CourseSyncWorker>()
                    .setInputData(inputData)
                    .build()
            }

        workManager.beginWith(works)
            .then(fileWorkers)
            .enqueue()

        return Result.success()
    }

}