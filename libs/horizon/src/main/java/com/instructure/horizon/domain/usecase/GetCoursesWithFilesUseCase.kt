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

import com.instructure.canvasapi2.models.ModuleItem
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
    private val getEnrollmentsUseCase: GetEnrollmentsUseCase,
    private val getModulesWithItemsUseCase: GetModulesWithItemsUseCase,
    private val courseFilesRepository: CourseFilesRepository,
) {
    suspend operator fun invoke(): List<CourseWithFilesData> {
        val enrollments = getEnrollmentsUseCase()
        return enrollments.map { enrollment ->
            val courseId = enrollment.courseId

            // Files from the course Files tab (full recursive listing)
            val courseFiles = runCatching { courseFilesRepository.getCourseFiles(courseId) }.getOrDefault(emptyList())
            val courseFilesById = courseFiles.associateBy { it.id }

            // Files referenced as module items — keep full item to use title as display name fallback
            val moduleItemFilesByContentId = runCatching {
                getModulesWithItemsUseCase(
                    GetModulesWithItemsUseCase.Params(courseId = courseId, forceRefresh = true)
                )
            }.getOrDefault(emptyList())
                .flatMap { it.items ?: emptyList() }
                .filter { it.type == ModuleItem.Type.File.name && it.contentId != 0L }
                .associateBy { it.contentId }

            // Union of file IDs from both sources
            val allFileIds = courseFilesById.keys + moduleItemFilesByContentId.keys
            val syncedIds = courseFilesRepository.getSyncedFileIds(courseId)

            val files = allFileIds.mapNotNull { fileId ->
                val apiFile = courseFilesById[fileId]
                    ?: runCatching { courseFilesRepository.getFileInfo(courseId, fileId) }.getOrNull()
                val moduleItem = moduleItemFilesByContentId[fileId]
                // Prefer display name from Files API; fall back to module item title
                val displayName = apiFile?.displayName?.takeIf { it.isNotEmpty() }
                    ?: moduleItem?.title?.takeIf { it.isNotEmpty() }
                    ?: return@mapNotNull null
                CourseFileData(
                    fileId = fileId,
                    displayName = displayName,
                    size = apiFile?.size ?: 0L,
                    isSynced = fileId in syncedIds,
                )
            }

            CourseWithFilesData(
                courseId = courseId,
                courseName = enrollment.courseName,
                files = files,
            )
        }
    }
}
