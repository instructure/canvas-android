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
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isValidTerm

class OfflineContentRepository(
    private val coursesApi: CourseAPI.CoursesInterface,
    private val tabApi: TabAPI.TabsInterface,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface
) {

    suspend fun getCourse(courseId: Long, forceNetwork: Boolean): Course {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        val courseResult = coursesApi.getCourse(courseId, params)

        return courseResult.dataOrThrow
    }

    suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        val coursesResult = coursesApi.getFirstPageCourses(params).depaginate { nextUrl -> coursesApi.next(nextUrl, params) }

        return coursesResult.dataOrThrow.filter { it.isValidTerm() && it.hasActiveEnrollment() }
    }

    suspend fun getTabs(courseId: Long, forceNetwork: Boolean): List<Tab> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        val tabsResult = tabApi.getTabs(courseId, CanvasContext.Type.COURSE.apiString, params)

        return tabsResult.dataOrThrow.filter {
            it.tabId in listOf(Tab.ASSIGNMENTS_ID, Tab.PAGES_ID, Tab.FILES_ID)
        }
    }

    suspend fun getCourseFiles(courseId: Long, forceNetwork: Boolean): List<FileFolder> {
        val params = RestParams()
        val rootFolderResult = fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList()

        return getAllFiles(rootFolderResult.dataOrThrow, forceNetwork)
    }

    private suspend fun getAllFiles(folder: FileFolder, forceNetwork: Boolean): List<FileFolder> {
        val result = mutableListOf<FileFolder>()
        val subFolders = getFolders(folder, forceNetwork)

        val currentFolderFiles = getFiles(folder, forceNetwork)
        result.addAll(currentFolderFiles)

        for (subFolder in subFolders) {
            val subFolderFiles = getAllFiles(subFolder, forceNetwork)
            result.addAll(subFolderFiles)
        }

        return result.filter { !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser }
    }

    private suspend fun getFolders(folder: FileFolder, forceNetwork: Boolean): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        val foldersResult = fileFolderApi.getFirstPageFolders(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return foldersResult.dataOrNull.orEmpty()
    }

    private suspend fun getFiles(folder: FileFolder, forceNetwork: Boolean): List<FileFolder> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        val filesResult = fileFolderApi.getFirstPageFiles(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        return filesResult.dataOrNull.orEmpty()
    }
}
