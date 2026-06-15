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
package com.instructure.horizon.domain.usecase

import com.instructure.canvasapi2.managers.graphql.horizon.HorizonManageOfflineContentManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.data.repository.CourseFilesRepository
import javax.inject.Inject

data class CourseFileData(
    val fileId: Long,
    val displayName: String,
    val size: Long,
    val isSynced: Boolean,
)

data class CourseWithFilesData(
    val courseId: Long,
    val courseName: String,
    val files: List<CourseFileData>,
)

class GetCoursesWithFilesUseCase @Inject constructor(
    private val manageOfflineContentManager: HorizonManageOfflineContentManager,
    private val courseFilesRepository: CourseFilesRepository,
    private val apiPrefs: ApiPrefs,
) {
    suspend operator fun invoke(): List<CourseWithFilesData> {
        val userId = apiPrefs.user?.id ?: return emptyList()
        val courses = manageOfflineContentManager.getCoursesWithFiles(userId, forceNetwork = false).dataOrThrow

        return courses.map { course ->
            val syncedIds = courseFilesRepository.getSyncedFileIds(course.courseId)
            CourseWithFilesData(
                courseId = course.courseId,
                courseName = course.courseName,
                files = course.files.map { file ->
                    CourseFileData(
                        fileId = file.fileId,
                        displayName = file.displayName,
                        size = file.sizeBytes,
                        isSynced = file.fileId in syncedIds,
                    )
                }
            )
        }
    }
}
