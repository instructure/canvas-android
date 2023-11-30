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

package com.instructure.teacher.features.files.details

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.License

class FileDetailsRepository(
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val featuresApi: FeaturesAPI.FeaturesInterface
) {
    suspend fun getFileFolderFromURL(url: String): FileFolder {
        val restParams = RestParams()
        return fileFolderApi.getFileFolderFromURL(url, restParams).dataOrThrow
    }

    suspend fun getCourseFeatures(courseId: Long): List<String> {
        val params = RestParams()
        return featuresApi.getEnabledFeaturesForCourse(courseId, params).dataOrThrow
    }

    suspend fun getCourseFileLicences(courseId: Long): List<License> {
        val params = RestParams()
        return fileFolderApi.getCourseFileLicenses(courseId, params).dataOrThrow
    }
}
