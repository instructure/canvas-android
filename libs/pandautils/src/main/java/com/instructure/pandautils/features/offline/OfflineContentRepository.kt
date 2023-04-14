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

    suspend fun getCourses(courseId: Long?): List<Course> {
        val params = RestParams(usePerPageQueryParam = true)

        if (courseId == null) {
            val coursesResult = coursesApi.getFirstPageCourses(params).depaginate { nextUrl -> coursesApi.next(nextUrl, params) }
            if (coursesResult.isFail) return emptyList() //TODO error handling
            return coursesResult.dataOrThrow.filter { it.isValidTerm() && it.hasActiveEnrollment() }
        } else {
            val courseResult = coursesApi.getCourse(courseId, params)
            if (courseResult.isFail) return emptyList() //TODO error handling
            return listOf(courseResult.dataOrThrow)
        }
    }

    suspend fun getTabs(courseId: Long): List<Tab> {
        val params = RestParams()
        val tabsResult = tabApi.getTabs(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (tabsResult.isFail) return emptyList() //TODO error handling

        return tabsResult.dataOrThrow.filter {
            it.tabId in listOf(Tab.ASSIGNMENTS_ID, Tab.PAGES_ID, Tab.FILES_ID)
        }
    }

    suspend fun getCourseFiles(courseId: Long): List<FileFolder> {
        val params = RestParams()
        val rootFolderResult = fileFolderApi.getRootFolderForContext(courseId, CanvasContext.Type.COURSE.apiString, params)

        if (rootFolderResult.isFail) return emptyList() //TODO error handling

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

        return result.filter { !it.isHidden && !it.isLocked && !it.isHiddenForUser && !it.isLockedForUser }
    }

    private suspend fun getFolders(folder: FileFolder): List<FileFolder> {
        val params = RestParams()
        val foldersResult = fileFolderApi.getFirstPageFolders(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        if (foldersResult.isFail) return emptyList() //TODO error handling

        return foldersResult.dataOrThrow
    }

    private suspend fun getFiles(folder: FileFolder): List<FileFolder> {
        val params = RestParams()
        val filesResult = fileFolderApi.getFirstPageFiles(folder.id, params).depaginate { nextUrl ->
            fileFolderApi.getNextPageFileFoldersList(nextUrl, params)
        }

        if (filesResult.isFail) return emptyList() //TODO error handling

        return filesResult.dataOrThrow
    }
}
