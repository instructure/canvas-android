/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.features.offline.offlinecontent

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles

class OfflineContentRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val courseFileSharedRepository: CourseFileSharedRepository,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val localFileDao: LocalFileDao,
    private val fileSyncProgressDao: FileSyncProgressDao
) {
    suspend fun getCourse(courseId: Long): Course {
        val params = RestParams(isForceReadFromNetwork = true)
        val courseResult = coursesApi.getCourse(courseId, params)

        return courseResult.dataOrThrow
    }

    suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val coursesResult = coursesApi.getFirstPageCourses(params).depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        return coursesResult.dataOrThrow.filter { it.isValidTerm() && it.hasActiveEnrollment() }
    }

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        return courseFileSharedRepository.getCourseFiles(courseId)
    }

    suspend fun findCourseSyncSettings(course: Course): CourseSyncSettingsWithFiles {
        var courseSettingsWithFiles = courseSyncSettingsDao.findWithFilesById(course.id)
        if (courseSettingsWithFiles == null) {
            val default = CourseSyncSettingsEntity(course.id, course.name, false)
            courseSyncSettingsDao.insert(default)

            courseSettingsWithFiles = CourseSyncSettingsWithFiles(
                default,
                emptyList()
            )

        }
        return courseSettingsWithFiles
    }

    suspend fun updateCourseSyncSettings(
        courseId: Long,
        courseSyncSettings: CourseSyncSettingsEntity,
        fileSyncSettings: List<FileSyncSettingsEntity>
    ) {
        courseSyncSettingsDao.update(courseSyncSettings)
        fileSyncSettingsDao.updateCourseFiles(courseId, fileSyncSettings)
    }

    suspend fun saveFileSettings(fileSyncSettingsEntity: FileSyncSettingsEntity) {
        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
    }

    suspend fun deleteFileSettings(fileId: Long) {
        fileSyncSettingsDao.deleteById(fileId)
    }

    suspend fun deleteFileSettings(fileIds: List<Long>) {
        fileSyncSettingsDao.deleteByIds(fileIds)
    }

    suspend fun getSyncSettings(): SyncSettingsEntity {
        return syncSettingsFacade.getSyncSettings()
    }

    suspend fun isFileSynced(fileId: Long): Boolean {
        return localFileDao.existsById(fileId)
    }

    suspend fun getInProgressFileSize(fileId: Long): Long {
        val file = fileSyncProgressDao.findByFileId(fileId) ?: return 0
        return (file.fileSize * (file.progress / 100.0)).toLong()
    }
}
