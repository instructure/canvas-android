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
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentState
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

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
    private val syncSettingsFacade: SyncSettingsFacade,
    private val syncProgressDao: SyncProgressDao,
    private val editDashboardItemDao: EditDashboardItemDao,
    private val courseDao: CourseDao,
    private val apiPrefs: ApiPrefs,
    private val fileFolderDao: FileFolderDao,
    private val localFileDao: LocalFileDao
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        if (!featureFlagProvider.checkEnvironmentFeatureFlag(FEATURE_FLAG_OFFLINE)) return Result.success()

        val dashboardCards = courseApi.getDashboardCourses(RestParams(isForceReadFromNetwork = true)).dataOrNull.orEmpty()
        dashboardCardDao.updateEntities(dashboardCards.map { DashboardCardEntity(it) })

        val params = RestParams(isForceReadFromNetwork = true, usePerPageQueryParam = true)
        val currentCourses = courseApi.firstPageCoursesByEnrollmentState("active", params).dataOrNull.orEmpty()
        val pastCourses = courseApi.firstPageCoursesByEnrollmentState("completed", params).dataOrNull.orEmpty()
        val futureCourses = courseApi.firstPageCoursesByEnrollmentState("invited_or_pending", params).dataOrNull.orEmpty()

        val allCourses = currentCourses.mapIndexed { index, course -> EditDashboardItemEntity(course, EnrollmentState.CURRENT, index) } +
            pastCourses.mapIndexed { index, course -> EditDashboardItemEntity(course, EnrollmentState.PAST, index) } +
            futureCourses.mapIndexed { index, course -> EditDashboardItemEntity(course, EnrollmentState.FUTURE, index) }
        editDashboardItemDao.updateEntities(allCourses)

        val courseIds = inputData.getLongArray(COURSE_IDS)
        val courses = courseIds?.let {
            courseSyncSettingsDao.findByIds(courseIds.toList())
        } ?: courseSyncSettingsDao.findAll()

        val courseIdsToRemove = courseSyncSettingsDao.findAll().filter { !it.anySyncEnabled }.map { it.courseId }
        courseDao.deleteByIds(courseIdsToRemove)
        courseIdsToRemove.forEach {
            cleanupFiles(it)
        }

        val settingsMap = courses.associateBy { it.courseId }

        val courseWorkers = courses.filter { it.anySyncEnabled }
            .map { CourseSyncWorker.createOnTimeWork(it.courseId, syncSettingsFacade.getSyncSettings().wifiOnly) }

        val syncProgress = courseWorkers.map {
            val courseId = it.workSpec.input.getLong(CourseSyncWorker.COURSE_ID, 0)
            SyncProgressEntity(
                it.id.toString(),
                courseId,
                settingsMap[courseId]?.courseName.orEmpty(),
            )
        }

        syncProgressDao.clearAndInsert(syncProgress)

        workManager.beginWith(courseWorkers)
            .enqueue()

        return Result.success()
    }

    private suspend fun cleanupFiles(courseId: Long) {
        val file = File(context.filesDir, "${apiPrefs.user?.id.toString()}/external_$courseId")
        file.deleteRecursively()

        fileFolderDao.deleteAllByCourseId(courseId)
        localFileDao.findRemovedFiles(courseId, emptyList()).forEach { localFile ->
            File(localFile.path).delete()
            localFileDao.delete(localFile)
        }
    }
}