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
import com.instructure.horizon.database.dao.HorizonAssignmentCommentDao
import com.instructure.horizon.database.dao.HorizonAssignmentDetailsDao
import com.instructure.horizon.database.dao.HorizonCourseDao
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonCourseScoreDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.dao.HorizonEntitySyncMetadataDao
import com.instructure.horizon.database.dao.HorizonFileFolderDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.horizon.database.dao.HorizonNoteDao
import com.instructure.horizon.database.dao.HorizonPageDao
import com.instructure.horizon.database.dao.HorizonSubmissionDao
import com.instructure.horizon.database.entity.EntitySyncType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CourseCleanupHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiPrefs: ApiPrefs,
    private val courseDao: HorizonCourseDao,
    private val localFileDao: HorizonLocalFileDao,
    private val fileFolderDao: HorizonFileFolderDao,
    private val pageDao: HorizonPageDao,
    private val assignmentDetailsDao: HorizonAssignmentDetailsDao,
    private val assignmentCommentDao: HorizonAssignmentCommentDao,
    private val submissionDao: HorizonSubmissionDao,
    private val courseModuleDao: HorizonCourseModuleDao,
    private val courseScoreDao: HorizonCourseScoreDao,
    private val dashboardModuleItemDao: HorizonDashboardModuleItemDao,
    private val entitySyncMetadataDao: HorizonEntitySyncMetadataDao,
    private val noteDao: HorizonNoteDao,
) {
    suspend fun cleanupCourseContent(courseId: Long) {
        assignmentCommentDao.deleteAttachmentsByCourseId(courseId)
        assignmentCommentDao.deleteByCourseId(courseId)
        submissionDao.deleteAttachmentsByCourseId(courseId)
        submissionDao.deleteByCourseId(courseId)

        assignmentDetailsDao.deleteByCourseId(courseId)
        pageDao.deleteByCourseId(courseId)
        courseModuleDao.deleteItemsForCourse(courseId)
        courseModuleDao.deleteModulesForCourse(courseId)
        courseScoreDao.deleteAssignmentsForCourse(courseId)
        courseScoreDao.deleteGroupsForCourse(courseId)
        courseScoreDao.deleteGradeForCourse(courseId)
        dashboardModuleItemDao.deleteForCourse(courseId)
        noteDao.deleteByCourseId(courseId)

        courseDao.deleteByCourseId(courseId)

        entitySyncMetadataDao.delete(EntitySyncType.COURSE, courseId)

        cleanupCourseFiles(courseId)
    }

    suspend fun cleanupCourseFiles(courseId: Long) {
        withContext(Dispatchers.IO) {
            val localFiles = localFileDao.findByCourseId(courseId)
            for (localFile in localFiles) {
                File(localFile.path).delete()
            }
            localFileDao.deleteByCourseId(courseId)
            fileFolderDao.deleteByCourseId(courseId)

            val userId = apiPrefs.user?.id ?: return@withContext
            val externalDir = File(context.filesDir, "$userId/external_$courseId")
            if (externalDir.exists()) {
                externalDir.deleteRecursively()
            }
        }
    }
}
