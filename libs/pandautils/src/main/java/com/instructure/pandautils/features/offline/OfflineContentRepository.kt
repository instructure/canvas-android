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

package com.instructure.pandautils.features.offline

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles

class OfflineContentRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao
) {
    suspend fun getCourse(courseId: Long): Course {
        val params = RestParams(isForceReadFromNetwork = true)
        val courseResult = coursesApi.getCourse(courseId, params)

        return courseResult.dataOrThrow
    }

    suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val coursesResult =
            coursesApi.getFirstPageCourses(params).depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        return coursesResult.dataOrThrow.filter { it.isValidTerm() && it.hasActiveEnrollment() }
    }

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true)
        val rootFolderResult =
            fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList()

        return getAllFiles(rootFolderResult.dataOrThrow)
    }

    private suspend fun getAllFiles(folder: FileFolder): List<FileFolder> {
        val result = mutableListOf<FileFolder>()
        val subFolders = getFolders(folder)

        val currentFolderFiles = getFiles(folder)
        result.addAll(currentFolderFiles)

        for (subFolder in subFolders) {
            val subFolderFiles = getAllFiles(subFolder)
            result.addAll(subFolderFiles)
        }

        return result
    }

    private suspend fun getFolders(folder: FileFolder): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true)
        val foldersResult = fileFolderApi.getFirstPageFolders(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return foldersResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private suspend fun getFiles(folder: FileFolder): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = true)
        val filesResult = fileFolderApi.getFirstPageFiles(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return filesResult.dataOrNull.orEmpty().filterValidFileFolders()
    }

    private fun List<FileFolder>.filterValidFileFolders() = this.filter {
        !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser
    }

    suspend fun findCourseSyncSettings(courseId: Long): CourseSyncSettingsWithFiles {
        var courseSettingsWithFiles = courseSyncSettingsDao.findWithFilesById(courseId)
        if (courseSettingsWithFiles == null) {
            val default = CourseSyncSettingsEntity(
                courseId,
                false,
                false,
                false,
                false,
                false
            )
            courseSyncSettingsDao.insert(default)

            courseSettingsWithFiles = CourseSyncSettingsWithFiles(
                default,
                emptyList()
            )

        }
        return courseSettingsWithFiles
    }

    suspend fun updateCourseSyncSettings(courseSyncSettingsEntity: CourseSyncSettingsEntity) {
        courseSyncSettingsDao.update(courseSyncSettingsEntity)
    }

    suspend fun saveFileSettings(fileSyncSettingsEntity: FileSyncSettingsEntity) {
        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
    }
}
