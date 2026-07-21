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
package com.instructure.horizon.data.repository

import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.ModuleItemWrapper
import com.instructure.canvasapi2.models.ModuleObject
import com.google.gson.Gson
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.data.datasource.ModuleItemNetworkDataSource
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import com.instructure.horizon.domain.usecase.GetUnreadCommentsCountUseCase
import com.instructure.horizon.offline.HorizonOfflineActionQueue
import com.instructure.horizon.offline.OfflineSyncRepository
import com.instructure.horizon.offline.sync.MARK_AS_DONE_ACTION
import com.instructure.horizon.offline.sync.MARK_AS_READ_ACTION
import com.instructure.horizon.offline.sync.MarkAsDonePayload
import com.instructure.horizon.offline.sync.MarkAsReadPayload
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.orDefault
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class ModuleItemSequenceRepository @Inject constructor(
    private val networkDataSource: ModuleItemNetworkDataSource,
    private val courseRepository: CourseRepository,
    private val courseModuleDao: HorizonCourseModuleDao,
    private val getUnreadCommentsCountUseCase: GetUnreadCommentsCountUseCase,
    private val apiPrefs: ApiPrefs,
    private val offlineActionQueue: HorizonOfflineActionQueue,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncRepository(networkStateProvider, featureFlagProvider) {

    private val gson = Gson()

    suspend fun getModuleItemSequence(courseId: Long, assetType: String, assetId: String): ModuleItemSequence {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getModuleItemSequence(courseId, assetType, assetId)
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
            networkDataSource.getModuleItem(courseId, moduleId, moduleItemId)
        } else {
            courseModuleDao.getItemById(moduleItemId)?.toModuleItem()
                ?: throw IllegalStateException("Module item $moduleItemId not available offline")
        }
    }

    suspend fun markAsNotDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return setDone(courseId, moduleItem, done = false)
    }

    suspend fun markAsDone(courseId: Long, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return setDone(courseId, moduleItem, done = true)
    }

    private suspend fun setDone(courseId: Long, moduleItem: ModuleItem, done: Boolean): DataResult<ResponseBody> {
        if (shouldFetchFromNetwork()) {
            return if (done) {
                networkDataSource.markAsDone(courseId, moduleItem)
            } else {
                networkDataSource.markAsNotDone(courseId, moduleItem)
            }
        }

        courseModuleDao.updateItemCompletion(moduleItem.id, done)
        offlineActionQueue.enqueue(
            actionType = MARK_AS_DONE_ACTION,
            payload = gson.toJson(MarkAsDonePayload(courseId, moduleItem.moduleId, moduleItem.id, done)),
            courseId = courseId,
        )
        return DataResult.Success("".toResponseBody(null))
    }

    suspend fun markAsRead(courseId: Long, moduleId: Long, itemId: Long) {
        if (shouldFetchFromNetwork()) {
            networkDataSource.markAsRead(courseId, moduleId, itemId)
            return
        }

        courseModuleDao.updateItemCompletion(itemId, done = true)
        offlineActionQueue.enqueue(
            actionType = MARK_AS_READ_ACTION,
            payload = gson.toJson(MarkAsReadPayload(courseId, moduleId, itemId)),
            courseId = courseId,
        )
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
}
