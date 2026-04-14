/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.repository.CourseRepository
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import com.instructure.horizon.domain.usecase.GetUnreadCommentsCountUseCase
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.orDefault
import okhttp3.ResponseBody
import javax.inject.Inject

class ModuleItemSequenceRepository @Inject constructor(
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val courseRepository: CourseRepository,
    private val courseModuleDao: HorizonCourseModuleDao,
    private val getUnreadCommentsCountUseCase: GetUnreadCommentsCountUseCase,
    private val apiPrefs: ApiPrefs,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    suspend fun getModuleItemSequence(courseId: Long, assetType: String, assetId: String): ModuleItemSequence {
        return if (shouldFetchFromNetwork()) {
            val params = RestParams(isForceReadFromNetwork = true)
            moduleApi.getModuleItemSequence(
                CanvasContext.Type.COURSE.apiString,
                courseId,
                assetType,
                assetId,
                params
            ).dataOrThrow
        } else {
            val itemId = findItemIdLocally(courseId, assetType, assetId)
            ModuleItemSequence(
                items = arrayOf(ModuleItemWrapper(current = ModuleItem(id = itemId)))
            )
        }
    }

    private suspend fun findItemIdLocally(courseId: Long, assetType: String, assetId: String): Long {
        val items = courseModuleDao.getItemsForCourse(courseId)
        return items.find { item ->
            when (assetType) {
                "Assignment", "Quiz" -> item.url?.endsWith("/$assetId") == true
                "Page" -> item.url?.contains("/pages/$assetId") == true
                "File" -> item.url?.endsWith("/$assetId") == true
                else -> false
            }
        }?.itemId ?: -1L
    }

    suspend fun getModulesWithItems(courseId: Long, forceNetwork: Boolean): List<ModuleObject> {
        return courseRepository.getModuleItems(courseId, forceNetwork)
    }

    suspend fun getModuleItem(courseId: Long, moduleId: Long, moduleItemId: Long): ModuleItem {
        return if (shouldFetchFromNetwork()) {
            val params = RestParams(isForceReadFromNetwork = true)
            moduleApi.getModuleItem(
                CanvasContext.Type.COURSE.apiString,
                courseId,
                moduleId,
                moduleItemId,
                params
            ).dataOrThrow
        } else {
            courseModuleDao.getItemById(moduleItemId)?.toModuleItem()
                ?: throw IllegalStateException("Module item $moduleItemId not available offline")
        }
    }

    suspend fun markAsNotDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsNotDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleItem.moduleId,
            moduleItem.id,
            RestParams()
        )
    }

    suspend fun markAsDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return moduleApi.markModuleItemAsDone(
            CanvasContext.Type.COURSE.apiString,
            courseId,
            moduleItem.moduleId,
            moduleItem.id,
            RestParams()
        )
    }

    suspend fun markAsRead(courseId: Long, moduleId: Long, itemId: Long) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        moduleApi.markModuleItemRead(CanvasContext.Type.COURSE.apiString, courseId, moduleId, itemId, restParams)
    }

    suspend fun hasUnreadComments(assignmentId: Long?, forceNetwork: Boolean = false): Boolean {
        if (assignmentId == null) return false
        return getUnreadCommentsCountUseCase(
            GetUnreadCommentsCountUseCase.Params(assignmentId, apiPrefs.user?.id.orDefault(), forceNetwork)
        ) > 0
    }

    private fun HorizonCourseModuleItemEntity.toModuleItem(): ModuleItem {
        val completionRequirement = completionRequirementType?.let {
            ModuleCompletionRequirement(
                type = it,
                minScore = completionRequirementMinScore,
                completed = completionRequirementCompleted,
            )
        }
        val moduleDetails = if (
            pointsPossible != null || dueAt != null || lockedForUser || lockExplanation != null ||
            hidden != null || locked != null
        ) {
            ModuleContentDetails(
                pointsPossible = pointsPossible,
                dueAt = dueAt,
                lockedForUser = lockedForUser,
                lockExplanation = lockExplanation,
                lockAt = lockAt,
                unlockAt = unlockAt,
                hidden = hidden,
                locked = locked,
            )
        } else null
        return ModuleItem(
            id = itemId,
            moduleId = moduleId,
            position = position,
            indent = indent,
            title = title,
            type = type,
            htmlUrl = htmlUrl,
            url = url,
            contentId = contentId,
            externalUrl = externalUrl,
            pageUrl = pageUrl,
            published = published,
            unpublishable = unpublishable,
            completionRequirement = completionRequirement,
            moduleDetails = moduleDetails,
            quizLti = quizLti,
            estimatedDuration = estimatedDuration,
        )
    }

    override suspend fun sync() {
        TODO("Not yet implemented")
    }
}
