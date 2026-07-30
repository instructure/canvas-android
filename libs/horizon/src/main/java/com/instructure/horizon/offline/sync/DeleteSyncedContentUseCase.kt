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

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonAnnouncementDao
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.dao.HorizonLocalImageDao
import com.instructure.horizon.database.dao.HorizonNotificationStreamItemDao
import com.instructure.horizon.offline.FileSyncPaths
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteSyncedContentUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val syncHelper: HorizonOfflineSyncHelper,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val localImageDao: HorizonLocalImageDao,
    private val notificationStreamItemDao: HorizonNotificationStreamItemDao,
    private val announcementDao: HorizonAnnouncementDao,
    private val courseCleanupHelper: CourseCleanupHelper,
) {
    suspend operator fun invoke() {
        syncHelper.fullStop()

        val plannedCourseIds = courseSyncPlanDao.findAll().map { it.courseId }
        for (courseId in plannedCourseIds) {
            courseCleanupHelper.cleanupCourseContent(courseId)
        }

        courseSyncPlanDao.deleteAll()
        fileSyncPlanDao.deleteAll()
        localImageDao.deleteAll()
        notificationStreamItemDao.deleteAll()
        announcementDao.deleteAll()
        announcementDao.deleteAllComments()

        withContext(Dispatchers.IO) {
            val userId = apiPrefs.user?.id ?: return@withContext
            val userDir = FileSyncPaths.userDir(context.filesDir, userId)
            if (userDir.exists()) {
                userDir.deleteRecursively()
            }
        }
    }
}
