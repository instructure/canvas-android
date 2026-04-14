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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonCourseModuleEntity
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import javax.inject.Inject

class CourseProgressLocalDataSource @Inject constructor(
    private val courseModuleDao: HorizonCourseModuleDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getModuleItems(courseId: Long): List<ModuleObject> {
        val moduleEntities = courseModuleDao.getModulesForCourse(courseId)
        return moduleEntities.map { moduleEntity ->
            val itemEntities = courseModuleDao.getItemsForModule(moduleEntity.moduleId)
            moduleEntity.toModuleObject(itemEntities)
        }
    }

    suspend fun saveModuleItems(courseId: Long, modules: List<ModuleObject>) {
        val moduleEntities = modules.map { it.toModuleEntity(courseId) }
        val itemEntities = modules.flatMap { module ->
            module.items.map { it.toModuleItemEntity(module.id, courseId) }
        }
        courseModuleDao.replaceForCourse(courseId, moduleEntities, itemEntities)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.COURSE_MODULES,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun HorizonCourseModuleEntity.toModuleObject(items: List<HorizonCourseModuleItemEntity>): ModuleObject {
        val prerequisiteIds = if (prerequisiteIds.isEmpty()) null
        else prerequisiteIds.split(",").mapNotNull { it.toLongOrNull() }.toLongArray()
        return ModuleObject(
            id = moduleId,
            position = position,
            name = name,
            state = state,
            estimatedDuration = estimatedDuration,
            prerequisiteIds = prerequisiteIds,
            items = items.map { it.toModuleItem() },
        )
    }

    private fun HorizonCourseModuleItemEntity.toModuleItem(): ModuleItem {
        val completionRequirement = completionRequirementType?.let {
            ModuleCompletionRequirement(
                type = it,
                minScore = completionRequirementMinScore,
                completed = completionRequirementCompleted,
            )
        }
        val moduleDetails = if (pointsPossible != null || dueAt != null || lockedForUser || lockExplanation != null) {
            ModuleContentDetails(
                pointsPossible = pointsPossible,
                dueAt = dueAt,
                lockedForUser = lockedForUser,
                lockExplanation = lockExplanation,
                lockAt = lockAt,
                unlockAt = unlockAt,
            )
        } else null
        return ModuleItem(
            id = itemId,
            moduleId = moduleId,
            position = position,
            title = title,
            type = type,
            htmlUrl = htmlUrl,
            url = url,
            completionRequirement = completionRequirement,
            moduleDetails = moduleDetails,
            quizLti = quizLti,
            estimatedDuration = estimatedDuration,
            pageUrl = pageUrl,
        )
    }

    private fun ModuleObject.toModuleEntity(courseId: Long): HorizonCourseModuleEntity {
        return HorizonCourseModuleEntity(
            moduleId = id,
            courseId = courseId,
            name = name,
            position = position,
            state = state,
            estimatedDuration = estimatedDuration,
            prerequisiteIds = prerequisiteIds?.joinToString(",") ?: "",
        )
    }

    private fun ModuleItem.toModuleItemEntity(moduleId: Long, courseId: Long): HorizonCourseModuleItemEntity {
        return HorizonCourseModuleItemEntity(
            itemId = id,
            moduleId = moduleId,
            courseId = courseId,
            title = title,
            position = position,
            type = type,
            htmlUrl = htmlUrl,
            url = url,
            completionRequirementType = completionRequirement?.type,
            completionRequirementMinScore = completionRequirement?.minScore ?: 0.0,
            completionRequirementCompleted = completionRequirement?.completed ?: false,
            pointsPossible = moduleDetails?.pointsPossible,
            dueAt = moduleDetails?.dueAt,
            lockedForUser = moduleDetails?.lockedForUser ?: false,
            lockExplanation = moduleDetails?.lockExplanation,
            lockAt = moduleDetails?.lockAt,
            unlockAt = moduleDetails?.unlockAt,
            quizLti = quizLti,
            estimatedDuration = estimatedDuration,
            pageUrl = pageUrl,
        )
    }
}
